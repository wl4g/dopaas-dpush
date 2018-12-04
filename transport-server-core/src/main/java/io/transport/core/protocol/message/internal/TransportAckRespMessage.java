package io.transport.core.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.transport.core.protocol.message.Head;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;

/**
 * 传送消息接收结果返回<br/>
 * ACK报文
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class TransportAckRespMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;
	final transient public static int STATUS_LEN = 1;

	private int msgId;
	private byte status = 1; // 状态(用于扩展)

	public TransportAckRespMessage() {
		this(0);
	}

	public TransportAckRespMessage(int msgId) {
		this.getHead().setActionId(MsgType.TRANSPORT_RESP.getActionId());
		if (msgId != 0)
			this.msgId = msgId;
	}

	public TransportAckRespMessage(int msgId, byte status) {
		this.getHead().setActionId(MsgType.TRANSPORT_RESP.getActionId());
		this.setMsgId(msgId);
		this.setStatus(status);
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		this.setMsgId(in.readInt());
		this.setStatus(in.readByte());
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		int totalLen = Head.HEAD_LEN + TransportMessage.MSGID_LEN + STATUS_LEN;
		out.writeInt(totalLen);
		out.writeShort(this.getHead().getVersion());
		out.writeByte(this.getHead().getActionId());
		out.writeByte(this.getHead().getReserve());
		out.writeInt(this.getMsgId()); // 消息ID
		out.writeByte(this.getStatus());
	}

}
