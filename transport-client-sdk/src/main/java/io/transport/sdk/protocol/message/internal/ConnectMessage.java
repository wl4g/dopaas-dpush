package io.transport.sdk.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.transport.sdk.exception.TransportException;
import io.transport.sdk.protocol.message.DeviceInfo;
import io.transport.sdk.protocol.message.Head;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.MsgType;
import io.transport.sdk.utils.ByteBufs;

/**
 * 建立连接结果输入消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class ConnectMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;
	final public static int ID_LEN = 16;
	final public static int SECRET_LEN = 32;

	private String appId;
	private String appSecret;
	private DeviceInfo deviceInfo = new DeviceInfo(); // 当前本机设备信息

	public ConnectMessage() {
	}

	public ConnectMessage(String appId, String appSecret) {
		this.getHead().setActionId(MsgType.CONNECT.getActionId());
		if (appId != null && appSecret != null) {
			this.setAppId(appId);
			this.setAppSecret(appSecret);
		}
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		if (appId.getBytes(CharsetUtil.UTF_8).length != ID_LEN)
			throw new TransportException("Protocol error, parameter 'appId' length can only be " + ID_LEN);
		this.appId = appId;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		if (appSecret.getBytes(CharsetUtil.UTF_8).length != SECRET_LEN)
			throw new TransportException("Protocol error, parameter 'appSecret' length can only be " + SECRET_LEN);
		this.appSecret = appSecret;
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(DeviceInfo deviceInfo) {
		if (deviceInfo != null)
			this.deviceInfo = deviceInfo;
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		byte[] appIdBuf = ByteBufs.coverFixedBuf(this.getAppId(), ID_LEN);
		byte[] appSecretBuf = ByteBufs.coverFixedBuf(this.getAppSecret(), SECRET_LEN);
		byte[] deviceIdBuf = ByteBufs.coverFixedBuf(this.getDeviceInfo().getDeviceId(), DeviceInfo.ID_LEN);
		byte[] groupIdBuf = ByteBufs.coverFixedBuf(this.getDeviceInfo().getGroupId(), DeviceInfo.GROUP_ID_LEN);
		byte[] deviceTypeBuf = ByteBufs.coverFixedBuf(this.getDeviceInfo().getDeviceType(), DeviceInfo.TYPE_LEN);
		int totalLen = Head.HEAD_LEN + ID_LEN + SECRET_LEN + DeviceInfo.ID_LEN + DeviceInfo.GROUP_ID_LEN
				+ DeviceInfo.TYPE_LEN;
		out.writeInt(totalLen);
		out.writeShort(this.getHead().getVersion());
		out.writeByte(this.getHead().getActionId());
		out.writeByte(this.getHead().getReserve());
		out.writeBytes(appIdBuf);
		out.writeBytes(appSecretBuf);
		out.writeBytes(groupIdBuf);
		out.writeBytes(deviceIdBuf);
		out.writeBytes(deviceTypeBuf);
	}

	@Override
	public String toString() {
		return "ConnectReqMessage [appId=" + appId + ", appSecret=" + appSecret + ", deviceInfo=" + deviceInfo + "]";
	}

}
