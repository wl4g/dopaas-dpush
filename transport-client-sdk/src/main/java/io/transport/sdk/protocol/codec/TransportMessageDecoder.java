package io.transport.sdk.protocol.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.transport.sdk.Configuration;
import io.transport.sdk.protocol.message.Head;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.MsgType;

/**
 * 接收消息解码器
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @time 2016年9月22日
 * @since
 */
public class TransportMessageDecoder extends ByteToMessageDecoder {
	private Configuration config;

	private State state = State.Head;
	private int totalLen = 0; // 消息总长度字段
	private short version = (short) 0; // 版本号
	private byte actionId = (byte) 0; // action类型
	private byte reserve = (byte) 0; // 保留字
	private Message msg = null; // 解码消息

	public TransportMessageDecoder(Configuration config) {
		super();
		this.config = config;
	}

	/*
	 * (non-Javadoc) 解码
	 * 
	 * @see io.netty.handler.codec.ByteToMessageDecoder#decode(io.netty.channel.
	 * ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try {
			int len = in.readableBytes();
			switch (this.state) {
			// 1.0 解析消息头
			case Head:
				if (len < Head.HEAD_LEN)
					return; // response header is HEAD_LEN bytes.

				this.totalLen = in.readInt(); // Total message length.
				this.version = in.readShort(); // version number(100/101...).
				this.actionId = in.readByte(); // Message action.
				this.reserve = in.readByte(); // Reserved field.

				// 1.1 匹配应答消息
				this.msg = this.determineMsgMatching(this.actionId);

				// 1.2 设置消息head
				this.msg.setHead(new Head(totalLen, version, actionId, reserve));
				this.state = State.Body;
				this.config.getLoggerImpl().debug("解码head: " + this.msg.getHead());
				// 2.2 解析消息body
			case Body:
				if (len < this.totalLen)
					return; // response total message is totalLen bytes.

				this.msg.readByteBufDecoder(in);
				this.state = State.Head;
				break;
			default:
				throw new IllegalArgumentException("Unlawfully decoded argument state.");
			}
			out.add(this.msg);
			this.config.getLoggerImpl().debug("解码完成. " + msg);

		} catch (Exception t) {
			this.config.getLoggerImpl().error("decode()解码失败. {}, totalLen=" + totalLen + ", version=" + version
					+ ", actionId=" + actionId + ", reserve=" + reserve);
			throw t;
		}
	}

	/**
	 * 依据actionId确定对应应答消息类实例
	 * 
	 * @param actionId
	 * @return
	 * @throws Exception
	 * @sine
	 */
	private Message determineMsgMatching(byte actionId) throws Exception {
		MsgType mt = MsgType.ofType(actionId);
		if (mt != null)
			return mt.getMsgClass().newInstance();

		this.config.getLoggerImpl().warn("未知消息类型. actionId=" + actionId);
		return null;
	}

	/**
	 * 解码状态粘包状态控制
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年1月5日
	 * @since
	 */
	static enum State {
		Head, Body
	}

}
