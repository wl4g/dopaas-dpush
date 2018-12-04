package io.transport.data.config;

import java.lang.reflect.Field;

import javax.annotation.Resource;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.util.ReflectionUtils;

import io.transport.mq.kafkaclient.config.KafkaConfiguration;

/**
 * Spring data for hbase configuration.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年11月16日
 * @since
 */
@org.springframework.context.annotation.Configuration
public class HbaseInitialConfigure {
	final private static Logger logger = LoggerFactory.getLogger(HbaseInitialConfigure.class);

	@Resource
	private HbaseConfiguration hbaseConf;
	@Resource
	protected KafkaConfiguration kafkaConf;

	/**
	 * Create an HbaseTemplate instance.
	 * 
	 * @return
	 */
	@Bean
	public HbaseTemplate hbaseTemplate() {
		/*
		 * When Kafka consumer is not started, the HbaseTemplate object will not
		 * be created. In this case, HbaseTemplate is also not allowed to be
		 * used.
		 */
		if (!this.kafkaConf.getConsumerConf().isStartup()) {
			logger.info("/-/-/-/-/-/-/-/-/-/-/-/-/-/");
			logger.warn("When Kafka consumer is not started, the HbaseTemplate object will not be created.");
			logger.info("/-/-/-/-/-/-/-/-/-/-/-/-/-/");
			return null;
		}

		HbaseTemplate hbaseTemplate = new HbaseTemplate();
		hbaseTemplate.setConfiguration(this.configure());
		hbaseTemplate.setEncoding("UTF-8");
		hbaseTemplate.setAutoFlush(true);
		// Return.
		return hbaseTemplate;
	}

	/**
	 * Building HbaseConfiguration.
	 * 
	 * @return
	 */
	private Configuration configure() {
		/*
		 * Create hbase configuration.<br/>
		 * conf.set(HConstants.ZOOKEEPER_QUORUM,
		 * hbaseConf.get(HConstants.ZOOKEEPER_QUORUM));
		 */
		Configuration conf = HBaseConfiguration.create();

		// Get all valid configuration properties fields of HBase.
		Field[] fields = HConstants.class.getDeclaredFields();
		// ForEach matching.
		this.hbaseConf.getProperties().forEach((k, v) -> {
			String confKey = null;
			// Find hbase conf key of HConstants fields.
			for (Field f : fields) {
				Object fvalue = ReflectionUtils.getField(f, null);
				if (String.valueOf(fvalue).toLowerCase().equals(String.valueOf(k).toLowerCase())) {
					confKey = String.valueOf(fvalue);
					break;
				}
			}
			if (confKey != null) {
				conf.set(confKey, hbaseConf.get(confKey));
			} else
				logger.warn("Invalid HBase configuration `{}`={}", k, v);
		});

		return conf;
	}

}