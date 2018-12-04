package io.transport.core.protocol.handler.ws;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.transport.common.Constants;
import io.transport.common.utils.StringUtils;
import io.transport.core.config.Configuration;
import io.transport.core.config.Configuration.WSConfig;
import io.transport.core.protocol.handler.TransportMessageHandler;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;
import io.transport.core.utils.ByteBufUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 文本WebSocket推送处理程序 <br/>
 * https://www.cnblogs.com/carl10086/p/6188808.html<br/>
 * https://my.oschina.net/tangcoffee/blog/340246
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年12月19日
 * @since
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // 多实例
public class TextWSFrameHandler extends TransportMessageHandler {

	@Autowired
	private Configuration config;
	private WebSocketServerHandshaker handshaker;

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void dispatch(ChannelHandlerContext ctx, Object msg) {
		try {
			// 处理HTTP握手请求
			if (msg instanceof FullHttpRequest) {
				this.processHttpRequest(ctx, (FullHttpRequest) msg);
			}
			// 处理WebSocket请求
			else if (msg instanceof WebSocketFrame)
				this.dispatchWebSocketFrame(ctx, (WebSocketFrame) msg);
			else {
				logger.warn("未知的WS消息类型.{}, msg={}", ctx.channel().remoteAddress(), msg);
				ctx.channel().close();
				ctx.close();
			}
		} finally {
			// After `-Dio.netty.leakDetection.level=advanced` tracking test, it
			// is found that the WebSocket decoder needs to be displayed and
			// released.
			ReferenceCountUtil.safeRelease(msg);
		}
	}

	/**
	 * 处理http的握手请求
	 * 
	 * @param ctx
	 * @param req
	 * @throws Exception
	 */
	private void processHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		// 如果HTTP解码失败则返回HTTP异常, 若不是WebSocket则直接异常返回
		if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
			this.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		// 构造握手响应返回
		String hostAndPort = ctx.channel().localAddress().toString();
		WSConfig conf = this.config.getWsConfig();
		String wspath = (conf.getSslEnable() ? "wss:/" : "ws:/") + hostAndPort + conf.getWebsocketPath();
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(wspath, null,
				conf.getAllowExtensions());
		this.handshaker = wsFactory.newHandshaker(req);
		if (this.handshaker == null)
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		else
			this.handshaker.handshake(ctx.channel(), req); // 返回握手response
	}

	/**
	 * 握手请求不成功时返回的应答
	 * 
	 * @param ctx
	 * @param req
	 * @param res
	 */
	private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// 返回应答给客户端
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}
		// 如果是非Keep-Alive，关闭连接
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200)
			f.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * WebSocket消息转发
	 * 
	 * @param ctx
	 * @param frame
	 * @throws Exception
	 */
	private void dispatchWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// 判断是否是关闭链路的指令
		if (frame instanceof CloseWebSocketFrame) {
			this.handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame);
			return;
		}
		// 判断是否是Ping消息
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content()));
			return;
		}
		// 本例程仅支持文本消息，不支持二进制消息
		// if (!(frame instanceof TextWebSocketFrame)) {
		// throw new UnsupportedOperationException(
		// String.format("%s frame types not supported",
		// frame.getClass().getName()));
		// }

		// 处理业务消息
		if (frame instanceof TextWebSocketFrame) {
			String msg = ((TextWebSocketFrame) frame).text();
			if (logger.isDebugEnabled())
				logger.debug(String.format("WS(%s) request payload=%s", ctx.channel(), msg));
			// 心跳报文
			if (msg == null || StringUtils.eqIgnCase(msg, Constants.H_PING))
				return;
		}

		// 处理业务逻辑
		this.processBizFrameMessage(ctx, frame);
	}

	/**
	 * WebSocket消息业务处理
	 * 
	 * @param ctx
	 * @param frame
	 * @throws Exception
	 */
	private void processBizFrameMessage(ChannelHandlerContext ctx, WebSocketFrame frame) {
		ByteBuf in = frame.content();
		JSONObject content = JSON.parseObject(ByteBufUtils.readString(in, in.readableBytes()));
		if (content != null) {
			String actionId = String.valueOf(content.getJSONObject("head").getOrDefault("actionId", 0));
			Class<? extends Message> msgClazz = MsgType.ofType(Byte.parseByte(actionId)).getMsgClass();
			if (msgClazz == null) {
				logger.warn("Unknown WebSocket(actionId) type message.{}", content);
				return;
			}
			Message payload = JSON.toJavaObject(content, msgClazz);
			super.dispatch(ctx, payload);
		} else
			logger.warn("WebSocket request is null.");
	}

}