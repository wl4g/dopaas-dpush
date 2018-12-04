package io.transport.mq.kafkaclient.config;

import java.util.Properties;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Kafka configuring YAML mapping configuration.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年11月16日
 * @since
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class KafkaConfiguration {

	@Resource
	private ProducerConf producerConf;
	@Resource
	private ConsumerConf consumerConf;

	public ConsumerConf getConsumerConf() {
		return consumerConf;
	}

	public ProducerConf getProducerConf() {
		return producerConf;
	}

	/**
	 * Spring boot yaml - Kafka producer config.
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0 2018年5月15日
	 * @since
	 */
	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	@ConfigurationProperties(prefix = ProducerConf.P_CONF_P)
	public static class ProducerConf {
		final public static String P_CONF_P = "kafka.producer";
		final private static Random random = new Random();

		@Value("${kafka.producer.startup:false}")
		private boolean startup = false;
		@Value("${kafka.producer.partition:10}")
		private int partition = 10;
		@Value("${kafka.producer.close-timeout:30}")
		private int closeTimeout = 30;
		/**
		 * 是否异步接收，TRUE表示使用异步，即：无需确认Kafka
		 * broker接收成功就立即返回给客户端成功.，否则仅ack确认成功才会返回给客户端成功.
		 */
		@Value("${kafka.producer.broker-async:false}")
		private boolean brokerAsync = false;
		@Value("${kafka.producer.auto-flush:true}")
		private boolean autoFlush = true;

		/**
		 * Kafka consumer properties.
		 */
		private Properties properties;

		public boolean isStartup() {
			return startup;
		}

		public void setStartup(boolean startup) {
			this.startup = startup;
		}

		public int getCloseTimeout() {
			return closeTimeout;
		}

		public void setCloseTimeout(int closeTimeout) {
			this.closeTimeout = closeTimeout;
		}

		public int randomPartition() {
			return random.nextInt(partition);
		}

		public int getPartition() {
			return partition;
		}

		public void setPartition(int partition) {
			this.partition = partition;
		}

		public boolean isBrokerAsync() {
			return brokerAsync;
		}

		public void setBrokerAsync(boolean brokerAsync) {
			this.brokerAsync = brokerAsync;
		}

		public boolean isAutoFlush() {
			return autoFlush;
		}

		public void setAutoFlush(boolean autoFlush) {
			this.autoFlush = autoFlush;
		}

		public Properties getProperties() {
			return properties;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

	}

	/**
	 * Spring boot yaml - Kafka consumer config.
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0 2018年5月15日
	 * @since
	 */
	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	@ConfigurationProperties(prefix = ConsumerConf.C_CONF_P)
	public static class ConsumerConf {
		final public static String C_CONF_P = "kafka.consumer";

		@Value("${kafka.consumer.startup:false}")
		private boolean startup = false;
		/**
		 * 已默认为异步, 详见类：io.transport.mq.kafkaclient.consumer.PushMessageHandler.
		 * execute()
		 * 是否异步归档(持久化)消息，TRUE表示使用异步归档，即：无需确认归档(hbase)成功立即推送，否则仅归档成功才会推送.
		 */
		@Deprecated
		// @Value("${kafka.consumer.archival-async:true}")
		private boolean archivalAsync = true;
		@Value("${kafka.consumer.poll-timeout:1000}")
		private int pollTimeout;
		@Value("${kafka.consumer.concurrency:3}")
		private int concurrency;
		@Value("${kafka.consumer.queueDepth:3}")
		private int queueDepth; // Bulk consumption change buffer queue size.

		/**
		 * Kafka consumer properties.
		 */
		private Properties properties;

		public boolean isStartup() {
			return startup;
		}

		public void setStartup(boolean startup) {
			this.startup = startup;
		}

		@Deprecated
		public boolean isArchivalAsync() {
			return archivalAsync;
		}

		@Deprecated
		public void setArchivalAsync(boolean archivalAsync) {
			this.archivalAsync = archivalAsync;
		}

		public int getPollTimeout() {
			return pollTimeout;
		}

		public void setPollTimeout(int pollTimeout) {
			this.pollTimeout = pollTimeout;
		}

		public int getConcurrency() {
			return concurrency;
		}

		public void setConcurrency(int concurrency) {
			this.concurrency = concurrency;
		}

		public int getQueueDepth() {
			return queueDepth;
		}

		public void setQueueDepth(int queueDepth) {
			this.queueDepth = queueDepth;
		}

		public Properties getProperties() {
			return properties;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

	}

}
