package io.transport.core.utils;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.transport.common.SpringContextHolder;
import io.transport.core.exception.TransportOfflineException;
import io.transport.core.protocol.message.MsgType;
import io.transport.core.protocol.message.internal.TransportMessage;
import io.transport.core.registry.ChannelRegistry;
import io.transport.core.registry.Client;
import io.transport.core.registry.DefaultChannelClientRegistry;

/**
 * Transmission processing tool.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年12月18日
 * @since
 */
public class TransportProcessors {
	final private static Logger logger = LoggerFactory.getLogger(TransportMessage.class);
	private static ChannelRegistry repository;

	static {
		// Initial repository.
		TransportProcessors.repository = SpringContextHolder.getBean(DefaultChannelClientRegistry.class);
	}

	/**
	 * Sending messages to the channel channel.
	 * 
	 * @param toDeviceId
	 *            to device client id.
	 * @param msg
	 *            Push message object.
	 */
	public static void sentMsg(String toDeviceId, Object msg) {
		// 1.1 Get the local channel client.
		Client c = getRepository().getLocalClient(toDeviceId);
		if (c == null)
			throw new TransportOfflineException("Device '" + toDeviceId + "' offline.");

		// 1.2 Building messages and sending.
		sent(c, msg);
	}

	/**
	 * Sending messages to multiple channels in group.channel
	 * 
	 * @param toGroupId
	 *            to device group id.
	 * @param msg
	 *            Push message object.
	 */
	public static void sentGroupMsg(String toGroupId, Object msg) {
		// 1.1 Get the local channel client list.
		Collection<Client> clients = getRepository().getLocalClients(toGroupId);
		if (clients == null || clients.isEmpty())
			throw new TransportOfflineException("'" + toGroupId + "' without online devices.");

		// 1.2 Loop sent.
		for (Client c : clients)
			sent(c, msg);
	}

	/**
	 * Build push messages
	 * 
	 * @param c
	 *            client object.
	 * @param msg
	 *            Target push message.
	 */
	public static TransportMessage build(String msg) {
		return build(JSON.parseObject(msg, TransportMessage.class));
	}

	/**
	 * Build push messages
	 * 
	 * @param c
	 *            client object.
	 * @param msg
	 *            Target push message.
	 */
	public static TransportMessage build(TransportMessage tmsg) {
		if (tmsg != null) {
			// Correction output action type.
			tmsg.getHead().setActionId(MsgType.TRANSPORT.getActionId());
		}
		return tmsg;
	}

	/**
	 * Sending messages to the client.channel channel
	 * 
	 * @param c
	 *            client object.
	 * @param msg
	 *            Target push message.
	 */
	public static void sent(final Client c, final Object msg) {
		try {
			// 1.1 The target client is a browser (WebSocket).
			if (isWSChannel(c)) {
				TextWebSocketFrame frame = new TextWebSocketFrame(JSON.toJSONString(msg));
				c.write(frame);
				if (logger.isDebugEnabled())
					logger.debug("Sent msg to device(Browser). client={}, msg={}", c.asText(), JSON.toJSONString(msg));
			}
			// 1.2 The target client is the background service
			// (Provider/Android, etc., using custom protocol).
			else {
				c.write(msg);
				if (logger.isDebugEnabled())
					logger.debug("Sent msg to device(Non-Browser). client={}, msg={}", c.asText(),
							JSON.toJSONString(msg));
			}

		} catch (Exception e) {
			logger.error("The execution of the Push task failed. client=" + c, e);
		}
	}

	/**
	 * Check the WebSocket channel.
	 * 
	 * @param client
	 *            client object.
	 * @return
	 */
	public static boolean isWSChannel(Client client) {
		return (client != null && isWSChannel(client.getChannel()));
	}

	/**
	 * Check the WebSocket channel.
	 * 
	 * @param channel
	 * @return
	 */
	public static boolean isWSChannel(Channel channel) {
		return (channel != null && channel.pipeline().context(WebSocketServerProtocolHandler.class) != null);
	}

	/**
	 * Get the channel repository instance.
	 * 
	 * @return
	 */
	private static ChannelRegistry getRepository() {
		return TransportProcessors.repository;
	}

}
