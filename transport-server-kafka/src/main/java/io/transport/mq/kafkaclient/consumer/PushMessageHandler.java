package io.transport.mq.kafkaclient.consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import io.transport.cluster.ActorService;
import io.transport.common.bean.PushMessageBean;
import io.transport.common.cache.JedisService;
import io.transport.common.executor.SafeThreadPoolExecutors;
import io.transport.common.utils.exception.Exceptions;
import io.transport.common.utils.exception.TransportException;
import io.transport.core.config.Configuration;
import io.transport.core.protocol.message.internal.TransportMessage;
import io.transport.core.registry.ChannelRegistry;
import io.transport.core.utils.TransportProcessors;
import io.transport.mq.kafkaclient.config.KafkaConfiguration;
import io.transport.mq.kafkaclient.config.TopicType;
import io.transport.persistent.PersistentService;

/**
 * Push topic message processor. <br/>
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
public class PushMessageHandler {
	final private static Logger logger = LoggerFactory.getLogger(PushMessageHandler.class);

	@Resource
	private Configuration conf;
	@Resource
	private KafkaConfiguration kafkaConf;
	@Resource
	private PersistentService persistService;
	@Resource
	private ActorService actorService;
	@Resource
	private ChannelRegistry registry;
	@Resource
	private JedisService jedisService;

	@KafkaListener(id = TopicType.t_push, topics = TopicType.t_push, containerFactory = "batchFactory")
	public void onReceive(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
		try {
			if (logger.isInfoEnabled())
				logger.info("Consumer msg records: {}", records);

			// 1.1 Execution submit and push process.
			this.execute(records);

			// 1.2 When the persistence is successful, manual submission of
			// Kafka's ACK message.
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

		// 1.1 Assembly parameter list.
		List<PushMessageBean> beans = new ArrayList<>();
		for (ConsumerRecord<String, String> record : records) {
			try {
				TransportMessage tmsg = TransportProcessors.build(record.value());
				if (tmsg == null)
					throw new TransportException("Consumer fail, record.value() is null.");
				else {
					beans.add(this.convertMessageBean(tmsg));
					// 1.4 Message distribute to cluster nodes.
					this.distributeInternalCluster(tmsg);
				}
			} catch (Exception e) {
				Exceptions.defaultPrintErr(logger, e);
			}
		}
		// 2.1 Batch persistence message.
		SafeThreadPoolExecutors.hbasePersisPool.getExecutor().submit(() -> this.persistService.batchSavePushMessage(beans));
	}

	/**
	 * Actor cluster internal distribution. <br/>
	 * 
	 * @param tmsg
	 */
	private void distributeInternalCluster(TransportMessage tmsg) {
		// 1.1 Get all client of toDeviceId and toGroupId.
		Set<String> deviceIds = this.getTargetDeviceIds(tmsg);

		// 1.2 Loop distribute push.
		for (String deviceId : deviceIds) {
			// distribution to cluster nodes actor(deviceId is actorAlias).
			this.actorService.tell(deviceId, tmsg);
			if (logger.isDebugEnabled())
				logger.debug("Consumer to actor. actorAlias={}, msg=", deviceId, tmsg);
		}

	}

	/**
	 * Get the list of target devices that need to be pushed, ID list
	 * 
	 * @param tmsg
	 * @return
	 */
	private Set<String> getTargetDeviceIds(TransportMessage tmsg) {
		// 1.1 Get all client of toDeviceId and toGroupId.
		Set<String> deviceIds = this.registry.getAllDeviceIds(tmsg.getToGroupId());
		// 1.2 Add deviceId.
		if (!StringUtils.isEmpty(tmsg.getToDeviceId())) {
			deviceIds = (deviceIds == null) ? deviceIds = Sets.newHashSet() : deviceIds;
			deviceIds.add(tmsg.getToDeviceId());
		}
		if (deviceIds == null || deviceIds.isEmpty()) {
			logger.warn("Consumer push failure. toGroupId={}, toDeviceId={}", tmsg.getToGroupId(),
					tmsg.getToDeviceId() + " clients is null.");
			deviceIds = Collections.emptySet();
		}

		return deviceIds;
	}

	/**
	 * Wrap convert to PushMessageBean.
	 * 
	 * @param tmsg
	 *            TransportMessage
	 * 
	 * @return PushMessageBean
	 */
	private PushMessageBean convertMessageBean(TransportMessage tmsg) {
		PushMessageBean bean = new PushMessageBean();
		// Get rowKey.
		bean.setRowKey(this.persistService.getTmpRowkey(tmsg.getMsgId(), false));
		// Other field.
		bean.setPayload(tmsg.getPayload());
		// Message classifier.
		bean.setClassifier(String.valueOf(tmsg.getClassifier()).trim());
		bean.setSrcDevice(null);
		bean.setDstDevice(null);
		bean.setRecTime(null); // No ACK confirmation.
		return bean;
	}

}
