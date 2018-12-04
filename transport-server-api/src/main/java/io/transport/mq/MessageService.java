package io.transport.mq;

/**
 * MQ发布消息Service
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年12月19日
 * @since
 */
public abstract interface MessageService {

	/**
	 * 发布消息到MQ
	 * 
	 * @param msg
	 *            负载消息
	 */
	void publish(Object msg);

	/**
	 * Confirm the received message.
	 * 
	 * @param msgId
	 *            msgId
	 */
	void receivedAck(int msgId);

	/**
	 * Sent confirm(ack) message.
	 * 
	 * @param msgId
	 *            Push TransportMmessage
	 */
	void sentAck(Object msg);

}
