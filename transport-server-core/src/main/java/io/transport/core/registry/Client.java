package io.transport.core.registry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import io.transport.core.protocol.message.DeviceInfo;

/**
 * 客户端(终端) socket连接通道相关信息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月11日
 * @since
 */
public class Client {
	private String appId;
	private DeviceInfo deviceInfo = new DeviceInfo(); // 当前连接客户端设备信息
	@JSONField(deserialize = false)
	transient private SocketChannel channel; // TCP连接通道

	public Client() {
		super();
	}

	public Client(SocketChannel channel) {
		this(null, null, channel);
	}

	public Client(DeviceInfo deviceInfo, SocketChannel channel) {
		this(null, deviceInfo, channel);
	}

	public Client(String appId, DeviceInfo deviceInfo, SocketChannel channel) {
		this.setDeviceInfo(deviceInfo);
		this.setChannel(channel);
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(DeviceInfo deviceInfo) {
		if (deviceInfo != null)
			this.deviceInfo = deviceInfo;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public void setChannel(SocketChannel channel) {
		if (channel != null)
			this.channel = channel;
	}

	public ChannelFuture write(Object msg) {
		return this.channel.writeAndFlush(msg);
	}

	public ChannelFuture close() {
		return this.channel.close();
	}

	public String asText() {
		return JSON.toJSONString(this);
	}

	@Override
	public String toString() {
		return this.asText();
	}

	public static enum ClientType {
		Android, iOS;

		public static boolean is(String type) {
			if (type == null || "".equals(type))
				return false;

			for (ClientType ct : values()) {
				if (type.equalsIgnoreCase(ct.name()))
					return true;
			}

			return false;
		}

	}

}
