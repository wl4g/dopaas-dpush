package io.transport.core.protocol.message.internal;

import org.springframework.util.StringUtils;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.transport.common.utils.exception.TransportException;
import io.transport.core.protocol.message.DeviceInfo;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;
import io.transport.core.utils.ByteBufUtils;

/**
 * 请求建立连接输入消息
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
	private DeviceInfo deviceInfo = new DeviceInfo(); // 设备信息

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
		this.setAppId(ByteBufUtils.readString(in, ID_LEN));
		this.setAppSecret(ByteBufUtils.readString(in, SECRET_LEN));
		this.getDeviceInfo().setGroupId(ByteBufUtils.readString(in, DeviceInfo.GROUP_ID_LEN));
		this.getDeviceInfo().setDeviceId(ByteBufUtils.readString(in, DeviceInfo.ID_LEN));
		this.getDeviceInfo().setDeviceType(ByteBufUtils.readString(in, DeviceInfo.TYPE_LEN));
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

	public void validation() {
		boolean ret = (StringUtils.isEmpty(this.getAppId()));
		if (ret)
			throw new TransportException("'appId'不允许为空.");
		this.getDeviceInfo().validation();
	}

	@Override
	public String toString() {
		return "ConnectMessage [appId=" + appId + ", appSecret=" + appSecret + ", deviceInfo=" + deviceInfo + "]";
	}

}
