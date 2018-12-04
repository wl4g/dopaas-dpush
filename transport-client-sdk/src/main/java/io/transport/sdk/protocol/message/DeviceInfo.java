package io.transport.sdk.protocol.message;

import java.io.Serializable;

import io.netty.util.CharsetUtil;
import io.transport.sdk.exception.TransportException;

/**
 * 设备信息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月24日
 * @since
 */
public class DeviceInfo implements Serializable {
	private static final long serialVersionUID = -3671517473263469475L;
	final transient public static int GROUP_ID_LEN = 32;
	final transient public static int ID_LEN = 32;
	final transient public static int TYPE_LEN = 10;

	private String groupId; // 不为空时可用于组播推送（TransportMessageWarpper:优先级高于toDeviceId）
	private String deviceId; // 终端唯一标识(仅iOS设备client生成，Android为空)
	private String deviceType; // 设备终端类型Browser/iOS/Android/Provider

	public DeviceInfo() {
		this(null, null, null);
	}

	public DeviceInfo(String deviceId, String deviceType) {
		this(null, deviceId, deviceType);
	}

	public DeviceInfo(String groupId, String deviceId, String deviceType) {
		super();
		this.setGroupId(groupId);
		this.setDeviceId(deviceId);
		this.setDeviceType(deviceType);
	}

	public String getGroupId() {
		return (this.groupId != null) ? groupId.trim() : null;
	}

	public void setGroupId(String groupId) {
		if (groupId != null && groupId.getBytes(CharsetUtil.UTF_8).length > GROUP_ID_LEN)
			throw new TransportException(
					"Protocol error, parameter 'groupId' length cannot be greater than " + GROUP_ID_LEN);
		this.groupId = groupId;
	}

	public String getDeviceId() {
		return (this.deviceId != null) ? deviceId.trim() : null;
	}

	public void setDeviceId(String deviceId) {
		if (deviceId != null && deviceId.getBytes(CharsetUtil.UTF_8).length > ID_LEN)
			throw new TransportException(
					"Protocol error, parameter 'deviceId' length cannot be greater than " + ID_LEN);
		this.deviceId = deviceId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		if (deviceType != null && deviceType.getBytes(CharsetUtil.UTF_8).length > TYPE_LEN)
			throw new TransportException(
					"Protocol error, parameter 'deviceType' length cannot be greater than " + TYPE_LEN);
		this.deviceType = deviceType;
	}

	@Override
	public String toString() {
		return "DeviceInfo [groupId=" + groupId + ", deviceId=" + deviceId + ", deviceType=" + deviceType + "]";
	}

	public static enum DeviceType {
		/*
		 * 浏览器类型应用程序
		 */
		BROWSER,
		/*
		 * Frontend客户端(Android或其他java应用程序)
		 */
		ANDROID,
		/*
		 * (第三方)后台服务应用程序
		 */
		PROVIDER;

		public static DeviceType ofType(int type) {
			return DeviceType.valueOf(String.valueOf(type));
		}

		public static boolean isType(String deviceType, DeviceType type) {
			if (type.name().toLowerCase().contains(deviceType.toLowerCase()))
				return true;
			return false;
		}

	}

}
