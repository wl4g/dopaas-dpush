package io.transport.sdk.protocol.handler;

import java.util.Set;

import com.alibaba.fastjson.JSON;

import io.netty.channel.ChannelHandlerContext;
import io.transport.sdk.Configuration.HostAndPort;
import io.transport.sdk.protocol.message.MsgType;
import io.transport.sdk.protocol.message.internal.ActiveRespMessage;
import io.transport.sdk.protocol.message.internal.ConnectRespMessage;
import io.transport.sdk.protocol.message.internal.DeviceRegistRespMessage;
import io.transport.sdk.protocol.message.internal.ResultRespMessage;
import io.transport.sdk.protocol.message.internal.TransportMessage;
import io.transport.sdk.protocol.message.internal.TransportAckRespMessage;
import io.transport.sdk.protocol.message.internal.ResultRespMessage.RetCode;

/**
 * 文本数据传送/推送 Handler
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月12日
 * @since
 */
public abstract class ReceiveTextHandler extends AbstractMessageHandler {
	final public static String REC_TEXT_KEY = "REC_TEXT_" + ReceiveTextHandler.class.getSimpleName();
	final public static String REC_TEXT_MSGID_SEP_KEY = "@";

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		getLoggerImpl().debug("Read消息:" + msg);
		try {
			// 2.1 转发给具体业务方法
			if (msg != null)
				this.dispatch(ctx, msg);
		} catch (Throwable t) {
			getLoggerImpl().error("Channl处理消息失败.", t);
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
	private void dispatch(ChannelHandlerContext ctx, Object msg) {
		// 1.1 新建连接返回
		if (msg instanceof ConnectRespMessage) {
			getLoggerImpl().info("Login successfully." + ctx.channel().remoteAddress());

			// 1.1.1 更新登录认证状态
			this.client.setAuthState(true);

			// 1.1.2 更新集群列表信息
			Set<String> hapTxts = ((ConnectRespMessage) msg).getHostAndPorts();
			this.client.getConfig().getLoggerImpl().info("Update cluster nodes: " + hapTxts);
			for (String hapTxt : hapTxts)
				this.client.getConfig().getHostAndPorts().add(HostAndPort.of(hapTxt));

			// 1.1.3 外部回调.
			this.onConnected(this.client.getConfig().getDeviceId());
			return;
		}
		// 1.2 服务处理结果返回
		else if (msg instanceof ResultRespMessage) {
			ResultRespMessage ret = (ResultRespMessage) msg;
			getLoggerImpl().info("Server handles the results." + ctx.channel().remoteAddress() + ", ret=" + ret);
			// 1.2.1 服务端返回结果客户端内部预先处理
			if (!this.processResultMessage(ctx, ret))
				this.onResult(ret);
			return;
		}
		// 1.3 客户端向服务端发送消息，服务端返回Ack消息.
		else if (msg instanceof TransportAckRespMessage) {
			this.processTransportAckResp(ctx, (TransportAckRespMessage) msg);
			return;
		}
		// 1.4 设备(Web端)注册结果返回
		else if (msg instanceof DeviceRegistRespMessage) {
			getLoggerImpl().info("Registered client(ws) device server successful. " + ctx.channel().remoteAddress()
					+ ", msg=" + msg);
			return;
		}
		// 1.5 链路检测
		if (msg instanceof ActiveRespMessage) {
			getLoggerImpl().info("On Active. msg=" + msg);
			return;
		}
		// 1.6 推送消息
		else if (msg instanceof TransportMessage) {
			TransportMessage msg0 = (TransportMessage) msg;
			/*
			 * 当客户端收到服务端推送来的数据后，立即回复已接收状态.
			 */
			this.echoWrite(ctx, new TransportAckRespMessage(msg0.getMsgId()));
			this.onMessage(msg0);
			return;
		}
		getLoggerImpl().warn("未知类型的消息." + JSON.toJSONString(msg));
		return;
	}

	/**
	 * 处理服务器返回的结果消息
	 * 
	 * @param msg
	 * @return 若处理完成返回TRUE，否则返回FALSE
	 */
	private boolean processResultMessage(ChannelHandlerContext ctx, ResultRespMessage msg) {
		// 1.1 连接认证类结果消息
		if (msg != null) {
			if (String.valueOf(msg.getType()).equals(MsgType.CONNECT.getActionId())) {
				if (String.valueOf(msg.getCode()).equals(RetCode.AUTH_FAIL)) { // 认证错误
					// 1.1.1 更新登录状态
					this.client.setAuthState(false);
					// 1.1.2 重连
					super.reconnect(ctx);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 处理服务端返回的Ack消息.
	 * 
	 * @param msg
	 */
	private void processTransportAckResp(ChannelHandlerContext ctx, TransportAckRespMessage msg) {
		System.out.println("推送返回"+msg);
		
		this.getConfig().getStoreImpl().get(String.valueOf(msg.getMsgId()));

	}

	/**
	 * 连接成功回调
	 * 
	 * @param deviceIdToken
	 */
	protected abstract void onConnected(String deviceIdToken);

	/**
	 * 收到推送消息回调
	 * 
	 * @param ctx
	 * @param msg
	 */
	protected abstract void onMessage(TransportMessage msg);

	/**
	 * 收到结果消息回调
	 * 
	 * @param msg
	 */
	protected abstract void onResult(ResultRespMessage msg);

	/**
	 * Ack message confirm scheduler handler.
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年4月25日
	 * @since
	 */
	static class AckMessageScheduler {

	}
}
