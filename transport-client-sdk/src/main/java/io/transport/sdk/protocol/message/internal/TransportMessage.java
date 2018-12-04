package io.transport.sdk.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.transport.sdk.exception.TransportException;
import io.transport.sdk.protocol.message.DeviceInfo;
import io.transport.sdk.protocol.message.Head;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.MsgType;
import io.transport.sdk.utils.ByteBufs;
import io.transport.sdk.utils.CRC16;

/**
 * 传送消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class TransportMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;
	final transient public static int MSGID_LEN = 4;
	final transient public static int CLASSIFIER_LEN = 50;

	/*
	 * 消息ID CRC(fromDeviceId+toDeviceId+toGroupId+timestamp)
	 */
	private int msgId;
	private String fromDeviceId;
	private String toDeviceId;
	private String toGroupId; // 不为空时表示使用组播推送（优先级高于toDeviceId）
	// Message classifier, which can be used for classification of message
	// statistics.
	private String classifier;
	private String payload;

	public TransportMessage() {
		this(null, null, null);
	}

	public TransportMessage(String fromDeviceId, String toDeviceId, String payload) {
		this(fromDeviceId, toDeviceId, null, payload);
	}

	public TransportMessage(String fromDeviceId, String toDeviceId, String toGroupId, String payload) {
		this.getHead().setActionId(MsgType.TRANSPORT.getActionId());
		this.setFromDeviceId(fromDeviceId);
		this.setToDeviceId(toDeviceId);
		this.setToGroupId(toGroupId);
		this.setPayload(payload);
	}

	public int getMsgId() {
		if (this.msgId != 0)
			return msgId;
		String unique = getFromDeviceId() + getToDeviceId() + getToGroupId() + System.currentTimeMillis();
		return (this.msgId = CRC16.crc16Modbus(ByteBufs.toBytes(unique)));
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		if (payload != null)
			this.payload = payload;
	}

	public String getFromDeviceId() {
		return fromDeviceId;
	}

	public void setFromDeviceId(String fromDeviceId) {
		if (fromDeviceId != null && fromDeviceId.getBytes(CharsetUtil.UTF_8).length > DeviceInfo.ID_LEN)
			throw new TransportException(
					"Protocol error, parameter 'fromDeviceId' length cannot be greater than " + DeviceInfo.ID_LEN);
		this.fromDeviceId = fromDeviceId;
	}

	public String getToDeviceId() {
		return (this.toDeviceId != null) ? toDeviceId.trim() : null;
	}

	public void setToDeviceId(String toDeviceId) {
		if (toDeviceId != null && toDeviceId.getBytes(CharsetUtil.UTF_8).length > DeviceInfo.ID_LEN)
			throw new TransportException(
					"Protocol error, parameter 'toDeviceId' length cannot be greater than " + DeviceInfo.ID_LEN);
		this.toDeviceId = toDeviceId;
	}

	public String getToGroupId() {
		return (this.toGroupId != null) ? toGroupId.trim() : null;
	}

	public void setToGroupId(String toGroupId) {
		if (toGroupId != null && toGroupId.getBytes(CharsetUtil.UTF_8).length > DeviceInfo.GROUP_ID_LEN)
			throw new TransportException(
					"Protocol error, parameter 'toGroupId' length cannot be greater than " + DeviceInfo.GROUP_ID_LEN);
		this.toGroupId = toGroupId;
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		this.setMsgId(in.readInt());
		this.setFromDeviceId(ByteBufs.toString(in, DeviceInfo.ID_LEN));
		this.setToDeviceId(ByteBufs.toString(in, DeviceInfo.ID_LEN));
		this.setToGroupId(ByteBufs.toString(in, DeviceInfo.GROUP_ID_LEN));
		this.setClassifier(ByteBufs.toString(in, CLASSIFIER_LEN));
		int len = this.getHead().getTotalLen()
				- (Head.HEAD_LEN + MSGID_LEN + DeviceInfo.ID_LEN * 2 + DeviceInfo.GROUP_ID_LEN + CLASSIFIER_LEN);

		this.setPayload(ByteBufs.toString(in, len));
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		byte[] fromDeviceIdBuf = ByteBufs.coverFixedBuf(this.getFromDeviceId(), DeviceInfo.ID_LEN);
		byte[] toDeviceIdBuf = ByteBufs.coverFixedBuf(this.getToDeviceId(), DeviceInfo.ID_LEN);
		byte[] toGroupIdBuf = ByteBufs.coverFixedBuf(this.getToGroupId(), DeviceInfo.GROUP_ID_LEN);
		byte[] classifierBuf = ByteBufs.coverFixedBuf(this.getClassifier(), CLASSIFIER_LEN);
		byte[] payloadBuf = ByteBufs.toBytes(this.getPayload());
		int totalLen = Head.HEAD_LEN + MSGID_LEN + DeviceInfo.ID_LEN * 2 + DeviceInfo.GROUP_ID_LEN + CLASSIFIER_LEN
				+ payloadBuf.length;

		out.writeInt(totalLen);
		out.writeShort(this.getHead().getVersion());
		out.writeByte(this.getHead().getActionId());
		out.writeByte(this.getHead().getReserve());
		out.writeInt(this.getMsgId()); // 消息ID
		out.writeBytes(fromDeviceIdBuf);
		out.writeBytes(toDeviceIdBuf);
		out.writeBytes(toGroupIdBuf);
		out.writeBytes(classifierBuf);
		out.writeBytes(payloadBuf); // 负载消息
	}

	@Override
	public String toString() {
		return "TransportMessage [msgId=" + getMsgId() + ", fromDeviceId=" + fromDeviceId + ", toDeviceId=" + toDeviceId
				+ ", toGroupId=" + toGroupId + ", payload=" + payload + "]";
	}

}
