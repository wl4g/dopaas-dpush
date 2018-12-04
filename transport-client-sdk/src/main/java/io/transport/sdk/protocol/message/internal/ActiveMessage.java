package io.transport.sdk.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.transport.sdk.protocol.message.Head;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.MsgType;

/**
 * 链路检测请求消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class ActiveMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;

	public ActiveMessage() {
		this.getHead().setActionId(MsgType.ACTIVE.getActionId());
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		out.writeInt(Head.HEAD_LEN + 0);
		out.writeShort(this.getHead().getVersion());
		out.writeByte(this.getHead().getActionId());
		out.writeByte(this.getHead().getReserve());
	}

}
