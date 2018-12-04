package io.transport.sdk.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.transport.sdk.Configuration;
import io.transport.sdk.protocol.message.Message;

/**
 * 输出消息编码器
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @time 2016年9月21日
 * @since
 */
public class TransportMessageEncoder extends MessageToByteEncoder<Message> {
	private Configuration config;

	public TransportMessageEncoder(Configuration config) {
		super();
		this.config = config;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
		try {
			// 1.1 编码body字节流到ByteBuf
			msg.writeBodyBufEncoder(out);
			// ctx.writeAndFlush(out);
			this.config.getLoggerImpl().debug("编码完成." + msg);

			/**
			 * http://www.oschina.net/question/1247524_250701<br/>
			 * 1. 若放在ctx.writeAndFlush()之前，则会write两次报文.<br/>
			 * 2. 若不调用out.retain()那么netty会自动release
			 * Bytebuf时报错:io.netty.util.IllegalReferenceCountException: refCnt:
			 * 0, decrement: 1<br/>
			 * 3. 因此一定要out.retain()同时只能在ctx.writeAndFlush()之后.
			 */
			// out.retain();
		} catch (Exception e) {
			this.config.getLoggerImpl().error("编码失败.", e);
			throw e;
		}
	}

}
