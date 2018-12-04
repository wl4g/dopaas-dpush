package io.transport.core.protocol.handler;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import io.netty.channel.ChannelHandlerContext;
import io.transport.core.exception.TransportAuthenticationException;
import io.transport.common.utils.exception.TransportException;
import io.transport.core.protocol.message.DeviceInfo;
import io.transport.core.protocol.message.MsgType;
import io.transport.core.protocol.message.internal.ActiveMessage;
import io.transport.core.protocol.message.internal.ActiveRespMessage;
import io.transport.core.protocol.message.internal.ClosingMessage;
import io.transport.core.protocol.message.internal.ConnectMessage;
import io.transport.core.protocol.message.internal.DeviceRegistMessage;
import io.transport.core.protocol.message.internal.DeviceRegistRespMessage;
import io.transport.core.protocol.message.internal.ResultRespMessage;
import io.transport.core.protocol.message.internal.TransportMessage;
import io.transport.core.protocol.message.internal.TransportAckRespMessage;
import io.transport.core.protocol.message.internal.ResultRespMessage.RetCode;
import io.transport.core.utils.ByteBufUtils;

/**
 * Data transfer Handler.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月12日
 * @since
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TransportMessageHandler extends AbstractChannelMessageHandler {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (logger.isDebugEnabled())
			logger.debug("Read消息: {}", JSON.toJSONString(msg));

		// 1.1 转发给具体业务方法
		if (msg == null)
			logger.warn("On channelRead(msg) is 'msg' null.");
		else {
			RetCode retCode = RetCode.OK; // 结果码
			String type = null; // 处理消息类型
			String err = null;// 异常信息
			boolean afterClose = false; // 输出后是否关闭
			try {
				this.dispatch(ctx, msg);
			} catch (Throwable t) {
				err = ExceptionUtils.getRootCauseMessage(t).split("\\:")[1];
				// 1.1 认证异常.
				if (t instanceof TransportAuthenticationException) {
					afterClose = true;
					retCode = RetCode.AUTH_FAIL; // 认证失败消息
					type = String.valueOf(MsgType.CONNECT.getActionId());
					logger.warn("认证失败: {}", err);
				} else {
					retCode = RetCode.SYS_ERR; // 系统异常结果码
					logger.error("处理失败.", t);
				}
				// Echo write.
				super.echoWrite(ctx, new ResultRespMessage(retCode, type, err), afterClose);
			}
		}
	}

	/**
	 * 转发给具体业务方法
	 * 
	 * 【方法描述】
	 * 
	 * @param msg
	 * @see: 【参考链接】
	 */
	@Override
	protected void dispatch(ChannelHandlerContext ctx, Object msg) {
		// 1.1 请求连接
		if (msg instanceof ConnectMessage) {
			if (logger.isDebugEnabled())
				logger.debug("On connect. msg={}", msg);
			super.processConnect(ctx, (ConnectMessage) msg);
			return;
		}

		// 1.2 连接认证检查
		super.authentication();

		// 1.3 连接关闭
		if (msg instanceof ClosingMessage) {
			if (logger.isDebugEnabled())
				logger.debug("On close. msg={}", msg);
			super.closeClient(); // Close处理
			return;
		}
		// 1.4 链路检测
		if (msg instanceof ActiveMessage) {
			if (logger.isDebugEnabled())
				logger.debug("On Active. msg={}", msg);
			// Reply client link detection.
			super.echoWrite(ctx, new ActiveRespMessage());
			return;
		}
		// 1.5 注册前端设备
		else if (msg instanceof DeviceRegistMessage) {
			if (logger.isDebugEnabled())
				logger.debug("On frontend device register. msg={}", msg);
			// 注册前端设备连接认证信息处理
			this.processDeviceRegister(ctx, (DeviceRegistMessage) msg);
			return;
		}
		// 1.6 消息传送
		else if (msg instanceof TransportMessage) {
			if (logger.isDebugEnabled())
				logger.debug("On transport. msg={}", msg);
			this.processTransport(ctx, (TransportMessage) msg); // 接收客户端发送消息处理
			return;
		}
		// 1.7 推送消息结果返回
		else if (msg instanceof TransportAckRespMessage) {
			if (logger.isDebugEnabled())
				logger.debug("On transportResp. msg={}", msg);
			this.processTransportAckResp(ctx, (TransportAckRespMessage) msg); // 推送到客户端结果返回
			return;
		}

		// 2.1 非法连接client close.
		super.closeClient();
		logger.warn("未知类型的消息.{}", JSON.toJSONString(msg));
		return;
	}

	/**
	 * 注册前端设备连接认证信息处理
	 * 
	 * @param ctx
	 * @param msg
	 */
	private void processDeviceRegister(ChannelHandlerContext ctx, DeviceRegistMessage msg) {
		// 1.1 Check deviceId.
		for (Object deviceId : msg.getClientDeviceIds()) {
			if (ByteBufUtils.toBytes(String.valueOf(deviceId)).length != DeviceInfo.ID_LEN)
				throw new TransportException(
						"Protocol error, parameter 'deviceId' length can only be " + DeviceInfo.ID_LEN);
		}

		// 1.2 保存注册前端设备连接认证信息Redis
		for (Object deviceId : msg.getClientDeviceIds())
			this.jedisService.set(String.valueOf(deviceId), msg.getAppId(), msg.getExpired());

		if (logger.isInfoEnabled())
			logger.info("新注册认证设备: msg={}", JSON.toJSONString(msg));

		// 1.3 Echo客户端
		DeviceRegistRespMessage resp = new DeviceRegistRespMessage();
		// Echo write.
		super.echoWrite(ctx, resp);

		if (logger.isDebugEnabled())
			logger.debug("Echo message. {}", resp);
	}

	/**
	 * 传送处理
	 * 
	 * @param ctx
	 * @param msg
	 */
	private void processTransport(ChannelHandlerContext ctx, TransportMessage msg) {
		// 1.1 Send to MQ broker.
		this.messageService.publish(msg);
		if (logger.isInfoEnabled())
			logger.info("Publish message. {}", msg);
	}

	/**
	 * 推送消息，客户端返回接收结果
	 * 
	 * @param ctx
	 * @param ack
	 */
	private void processTransportAckResp(ChannelHandlerContext ctx, TransportAckRespMessage ack) {
		// 1.1 Confirm the received message.
		this.messageService.receivedAck(ack.getMsgId());
		if (logger.isInfoEnabled())
			logger.info("Publish ackMessage. {}", JSON.toJSONString(ack));

		// 1.2 Echo write.
		super.echoWrite(ctx, new ResultRespMessage(RetCode.OK, MsgType.TRANSPORT_RESP.name(), null));
	}

}
