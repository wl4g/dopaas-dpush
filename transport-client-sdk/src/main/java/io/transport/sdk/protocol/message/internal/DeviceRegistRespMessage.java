package io.transport.sdk.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.MsgType;

/**
 * 新注册(Web端/前端)设备输出消息（EG：用于Web端ws连接认证）
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class DeviceRegistRespMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;

	public DeviceRegistRespMessage() {
		this.getHead().setActionId(MsgType.DEVICE_REGIS_RESP.getActionId());
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

	@Override
	public String toString() {
		return "ConnectOutMessage [getHead()=" + getHead() + "]";
	}

}
