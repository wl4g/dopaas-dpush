package io.transport.mq.kafkaclient.consumer;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import io.transport.common.utils.exception.Exceptions;
import io.transport.mq.kafkaclient.config.TopicType;
import io.transport.persistent.PersistentService;

/**
 * Push topic message processor.<br/>
 * Reference to the official webSite link: <br/>
 * <br/>
 * https://docs.spring.io/spring-kafka/reference/htmlsingle/ <br/>
 * https://docs.spring.io/spring-kafka/reference/htmlsingle/#message-listeners<br/>
 * https://blog.csdn.net/u012961566/article/details/77336296
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月10日
 * @since
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AckMessageHandler {
	final private static Logger logger = LoggerFactory.getLogger(AckMessageHandler.class);

	@Resource
	private PersistentService persistService;

	@KafkaListener(id = TopicType.t_push_ack, topics = TopicType.t_push_ack, containerFactory = "batchFactory")
	public void onReceive(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
		try {
			if (logger.isInfoEnabled())
				logger.info("Consumer ack records: {}", records);

			// 1.1 Execution submit update process.
			this.execute(records);

			// When the persistence is successful, manual submission of Kafka's
			// ACK message.
			ack.acknowledge();

		} catch (Exception e) {
			Exceptions.defaultPrintErr(logger, e);
		}
	}

	/**
	 * Process consumer record.
	 * 
	 * @param record
	 */
	private void execute(List<ConsumerRecord<String, String>> records) {
		if (records == null || records.isEmpty())
			return;

		// 1.1 Converting the record message to the msgId list message.
		List<Integer> msgIds = new ArrayList<>(records.size());
		records.stream().forEach(record -> {
			try {
				String msgId = record.value();
				if (msgId == null) {
					logger.warn("Consumer ack message is null. {}", JSON.toJSONString(record));
					return;
				}
				msgIds.add(Integer.parseInt(msgId));

			} catch (Exception e) {
				Exceptions.defaultPrintErr(logger, e);
			}
		});

		// 1.2 Batch update received ACK status.
		this.persistService.batchUpdateRecAckMessage(msgIds);
	}

}
