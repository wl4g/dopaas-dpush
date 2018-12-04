package io.transport.mq.kafkaclient.config;

/**
 * Kafka topic protocol definition
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年11月1日
 * @since
 */
public final class TopicType {

	/**
	 * Push (unicast and multicast) message Kafka topic.
	 */
	final public static String t_push = "transporter_push_message";
	/**
	 * Push to confirm the ACK message Kafka topic.
	 */
	final public static String t_push_ack = "transporter_push_ack";

}
