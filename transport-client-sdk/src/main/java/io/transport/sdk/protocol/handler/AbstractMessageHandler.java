package io.transport.sdk.protocol.handler;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.ScheduledFuture;
import io.transport.sdk.Configuration;
import io.transport.sdk.TransportClient;
import io.transport.sdk.logger.Logger;
import io.transport.sdk.protocol.message.internal.ActiveMessage;

/**
 * 基础业务抽象 Hanlder
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月11日
 * @since
 */
abstract class AbstractMessageHandler extends ChannelDuplexHandler {
	protected TransportClient client;

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		getLoggerImpl().error("连接中断." + ctx.channel().remoteAddress());
		// 重连
		this.reconnect(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		getLoggerImpl().error("Channel传输异常." + cause.getMessage());
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			getLoggerImpl().warn("连接空闲. " + ctx.channel() + ", " + event.state());

			switch (event.state()) {
			case READER_IDLE: // 读超时事件:可能此通道无业务或断线, 发送心跳检查一下.
				ctx.channel().writeAndFlush(new ActiveMessage());
				break;
			case WRITER_IDLE: // 写超时事件:异常情况(连心跳都未发送，不易触发), 直接close重连.
				// 重连
				this.reconnect(ctx);
				break;
			case ALL_IDLE: // 读&写事件:异常情况(连心跳都未发送，不易触发), 直接close重连.
				// 重连
				this.reconnect(ctx);
				break;
			default:
				throw new UnsupportedOperationException("不支持的空闲检测类型.");
			}
		}

		super.userEventTriggered(ctx, evt);
	}

	/**
	 * ChannelHandlerContext 回写到客户端
	 * 
	 * @param ctx
	 *            通道上下文
	 * @param object
	 *            消息对象
	 */
	protected void echoWrite(ChannelHandlerContext ctx, Object msg) {
		ctx.writeAndFlush(msg);
	}

	/**
	 * 关闭/注销Client
	 */
	protected ChannelFuture close(ChannelHandlerContext ctx) {
		try {
			if (ctx != null) {
				// Close client channel.
				ctx.channel().close();
				return ctx.close();
			}
		} catch (Exception e) {
			getLoggerImpl().error("主动断开无效连接失败.", e);
		}
		return null;
	}

	/**
	 * 重连通道
	 * 
	 * @param ctx
	 * @return
	 */
	protected ScheduledFuture<?> reconnect(final ChannelHandlerContext ctx) {
		long delay = this.client.getConfig().getReconnectDelay();
		return ctx.channel().eventLoop().schedule(new Runnable() {

			@Override
			public void run() {
				// 关闭channel
				close(ctx);
				// 重新连接、认证
				client.join();
			}
		}, delay, TimeUnit.SECONDS);
	}

	/**
	 * 获取配置对象
	 * 
	 * @return
	 */
	protected Configuration getConfig() {
		return this.client.getConfig();
	}

	/**
	 * 获取日志对象
	 * 
	 * @return
	 */
	protected Logger getLoggerImpl() {
		return this.getConfig().getLoggerImpl();
	}

	@SuppressWarnings("unchecked")
	public <T> T setClient(TransportClient client) {
		this.client = client;
		return (T) this;
	}

}
