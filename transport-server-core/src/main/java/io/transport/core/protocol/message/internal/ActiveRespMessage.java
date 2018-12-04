package io.transport.core.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.transport.core.protocol.message.Head;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;

/**
 * 链路检测输出消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class ActiveRespMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;

	public ActiveRespMessage() {
		this.getHead().setActionId(MsgType.ACTIVE_RESP.getActionId());
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

	@Override
	public String toString() {
		return "ConnectOutMessage [getHead()=" + getHead() + "]";
	}

}
