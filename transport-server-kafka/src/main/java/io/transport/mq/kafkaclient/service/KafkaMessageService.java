package io.transport.mq.kafkaclient.service;

import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaOperations.ProducerCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import io.transport.common.cache.JedisService;
import io.transport.common.utils.exception.Exceptions;
import io.transport.common.utils.exception.TransportException;
import io.transport.core.config.Configuration;
import io.transport.core.protocol.message.internal.TransportAckRespMessage;
import io.transport.core.protocol.message.internal.TransportMessage;
import io.transport.core.utils.TransportProcessors;
import io.transport.mq.MessageService;
import io.transport.mq.kafkaclient.config.KafkaConfiguration;
import io.transport.mq.kafkaclient.config.TopicType;
import io.transport.mq.kafkaclient.exception.KafkaProducerException;
import io.transport.persistent.PersistentService;

/**
 * Kafka message service implement <br/>
 * 参考：<a href=
 * "https://blog.csdn.net/b6ecl1k7BS8O/article/details/79636270">`华泰证券`
 * 最终一致性kafka实现 https://blog.csdn.net/b6ecl1k7BS8O/article/details/79636270</a>
 * 
 * Reference to the official webSite link: <br/>
 * <br/>
 * https://docs.spring.io/spring-kafka/reference/htmlsingle/ <br/>
 * https://docs.spring.io/spring-kafka/reference/htmlsingle/#message-listeners<br/>
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年12月19日
 * @since
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class KafkaMessageService implements MessageService {
	final private static Logger logger = LoggerFactory.getLogger(KafkaMessageService.class);

	@Resource
	private Configuration conf;
	@Resource
	private KafkaConfiguration kafkaConf;
	@Resource
	private JedisService jedisService;
	@Resource
	private KafkaTemplate<String, String> kafkaTemplate;
	@Resource
	private PersistentService persistentService;

	@Override
	public void publish(Object msg) {
		if (this.kafkaTemplate == null)
			throw new TransportException("It shouldn't be here, because kafkaTemplate is not running.");

		if (msg != null && msg instanceof TransportMessage) {
			final TransportMessage tmsg = (TransportMessage) msg;
			final String tmsg0 = JSON.toJSONString(msg);
			if (logger.isDebugEnabled())
				logger.debug("New message enqueued. {}", tmsg0);

			// 1.1 Save rowKey prefix temporary to redis.
			this.persistentService.saveTmpRowkey(tmsg.getMsgId(), tmsg.getFromDeviceId(), tmsg.getToDeviceId(),
					tmsg.getToGroupId());

			// 1.2 Define.
			final String topic = TopicType.t_push;
			// Random partition.
			final int partition = this.kafkaConf.getProducerConf().randomPartition();
			// 1.3 Asynchronous to broker message.
			if (this.kafkaConf.getProducerConf().isBrokerAsync()) {
				// 1.4 Direct reply send ack.
				this.sentAck((TransportMessage) msg);
				// 1.5 Send to kafka broker, no callback.
				this.kafkaTemplate.send(topic, partition, tmsg0);
			}
			// 2.1 Synchronous to broker and sent ACK.
			else {
				this.kafkaTemplate.execute(new ProducerCallback<String, String, Future<RecordMetadata>>() {
					@Override
					public Future<RecordMetadata> doInKafka(Producer<String, String> producer) {
						// Define producer record.
						ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, partition,
								null, tmsg0);
						// Send to broker.
						return producer.send(record, (RecordMetadata meta, Exception e) -> {
							// Confirm that after broker has been
							// received, send
							// the ACK message to the client.
							try {
								// 3.3 Reply send ack.
								if (e == null)
									sentAck(tmsg);
								else
									throw e;
							} catch (Exception e1) {
								Exceptions.defaultPrintErr(logger, e1);
							}
						});
					}
				});
			}
		} else
			throw new KafkaProducerException("Illegal type message. " + msg);
	}

	@Override
	public void receivedAck(int msgId) {
		if (logger.isInfoEnabled())
			logger.info("Received ack. msgId={}", msgId);

		// Ignoring the client to return to the ACK message.
		//
		// Because Kafka only supports commit(kafkaConsumer.commitSync())
		// offset to respond to broker, but offset can't exactly indicate
		// that message, So this is also an inadequacy of the Kafka
		// implementation, But fortunately, Kafka
		// broker is very powerful, and by default, it can accommodate 7
		// days of data to ensure that the message is
		// not lost.

		final String topic = TopicType.t_push_ack;
		// Random partition.
		final int partition = this.kafkaConf.getProducerConf().randomPartition();
		final String msg = String.valueOf(msgId);

		this.kafkaTemplate.execute(new ProducerCallback<String, String, Future<RecordMetadata>>() {
			@Override
			public Future<RecordMetadata> doInKafka(Producer<String, String> producer) {
				// Define producer record.
				ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, partition, null, msg);
				// Send to broker.
				return producer.send(record, (RecordMetadata meta, Exception e) -> {
					// Sending failure.
					if (e != null)
						logger.error("Producer to Kafka broker failure. " + meta, e);
				});
			}
		});

	}

	@Override
	public void sentAck(Object msg) {
		if (msg != null && msg instanceof TransportMessage) {
			final TransportMessage tmsg = (TransportMessage) msg;
			// 1.2 Echo客户端
			TransportAckRespMessage ack = new TransportAckRespMessage(tmsg.getMsgId());
			// Echo ack.
			TransportProcessors.sentMsg(tmsg.getFromDeviceId(), ack);

			if (logger.isDebugEnabled())
				logger.debug("Echo ack message. {}", ack);
		} else
			throw new KafkaProducerException("Illegal type message. " + msg);
	}

}
