package io.transport.sdk.protocol.message.internal;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.transport.sdk.exception.TransportException;
import io.transport.sdk.protocol.message.DeviceInfo;
import io.transport.sdk.protocol.message.Head;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.MsgType;
import io.transport.sdk.utils.ByteBufs;

/**
 * 新注册(Web端/前端)设备请求消息（EG：用于Web端ws连接认证）
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class DeviceRegistMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;

	private String appId;
	private Integer expired = 0;
	private List<String> clientDeviceIds = new ArrayList<String>();

	public DeviceRegistMessage() {
		this.getHead().setActionId(MsgType.DEVICE_REGIS.getActionId());
	}

	public DeviceRegistMessage(String appId, Integer expired, List<String> clientDeviceIds) {
		super();
		this.setAppId(appId);
		this.setClientDeviceIds(clientDeviceIds);
		this.setExpired(expired);
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		if (appId.getBytes(CharsetUtil.UTF_8).length != ConnectMessage.ID_LEN)
			throw new TransportException(
					"Protocol error, parameter 'appId' length can only be " + ConnectMessage.ID_LEN);
		this.appId = appId;
	}

	public List<String> getClientDeviceIds() {
		return clientDeviceIds;
	}

	public void setClientDeviceIds(List<String> clientDeviceIds) {
		if (clientDeviceIds != null)
			this.clientDeviceIds = clientDeviceIds;
	}

	public Integer getExpired() {
		return expired;
	}

	public void setExpired(Integer expired) {
		if (expired <= 0)
			throw new TransportException("Protocol error, parameter 'expired' must greater than 0.");
		this.expired = expired;
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		byte[] appIdBuf = ByteBufs.coverFixedBuf(this.getAppId(), ConnectMessage.ID_LEN);
		StringBuffer deviceIds = new StringBuffer();
		for (String id : getClientDeviceIds()) {
			if (ByteBufs.toBytes(String.valueOf(id)).length != DeviceInfo.ID_LEN)
				throw new TransportException(
						"Protocol error, parameter 'deviceId' length can only be " + DeviceInfo.ID_LEN);
			deviceIds.append(id);
			deviceIds.append(",");
		}
		if (deviceIds.toString().endsWith(","))
			deviceIds.delete(deviceIds.length() - 1, deviceIds.length());

		byte[] deviceIdsBuf = ByteBufs.toBytes(deviceIds.toString());
		int totalLen = Head.HEAD_LEN + ConnectMessage.ID_LEN + 4 + deviceIdsBuf.length;
		out.writeInt(totalLen);
		out.writeShort(this.getHead().getVersion());
		out.writeByte(this.getHead().getActionId());
		out.writeByte(this.getHead().getReserve());
		out.writeBytes(appIdBuf);
		out.writeInt(this.getExpired());
		out.writeBytes(deviceIdsBuf);
	}

}
