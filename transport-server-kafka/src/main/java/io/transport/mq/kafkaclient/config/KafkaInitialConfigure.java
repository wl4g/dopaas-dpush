package io.transport.mq.kafkaclient.config;

import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode;
import org.springframework.kafka.listener.config.ContainerProperties;

/**
 * Reference to the official webSite link: <br/>
 * <br/>
 * https://docs.spring.io/spring-kafka/reference/htmlsingle/ <br/>
 * https://docs.spring.io/spring-kafka/reference/htmlsingle/#message-listeners
 * https://blog.csdn.net/u012961566/article/details/77336296
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年5月11日
 * @since
 */
@Configuration
@EnableKafka
public class KafkaInitialConfigure {
	final private static Logger logger = LoggerFactory.getLogger(KafkaInitialConfigure.class);

	@Resource
	private KafkaConfiguration conf;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		if (!this.conf.getProducerConf().isStartup()) {
			logger.warn("/-/-/-/-/-/-/-/-/-/-/-/-/-/");
			logger.warn("Not starting KafkaTemplate producer.");
			logger.warn("/-/-/-/-/-/-/-/-/-/-/-/-/-/");
			return null;
		}

		// Create producer factory.
		Properties properties = conf.getProducerConf().getProperties();
		Map<String, Object> configs = (Map) properties;
		ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(configs);
		// Create KafkaTemplate.
		return new KafkaTemplate<String, String>(pf, conf.getProducerConf().isAutoFlush());
	}

	/**
	 * `@Bean` public
	 * KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,
	 * String>> batchFactory()
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
	public KafkaListenerContainerFactory<?> batchFactory() {
		if (!this.conf.getConsumerConf().isStartup()) {
			logger.warn("/-/-/-/-/-/-/-/-/-/-/-/-/-/");
			logger.warn("Not starting Kafka consumer containers.");
			logger.warn("/-/-/-/-/-/-/-/-/-/-/-/-/-/");
			return null;
		}

		// Create consumer factory.
		Properties properties = conf.getConsumerConf().getProperties();
		Map<String, Object> configs = (Map) properties;
		ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(configs);

		// Create concurrent consumer container factory.
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(cf);
		factory.setConcurrency(conf.getConsumerConf().getConcurrency());
		factory.setBatchListener(true);

		// Spring kafka container properties.
		ContainerProperties containerProps = factory.getContainerProperties();
		containerProps.setPollTimeout(conf.getConsumerConf().getPollTimeout());
		// Bulk consumption change buffer queue size.
		containerProps.setQueueDepth(conf.getConsumerConf().getQueueDepth());
		containerProps.setAckMode(AckMode.MANUAL_IMMEDIATE);

		return factory;
	}

}
