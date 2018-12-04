package io.transport.core.protocol.message.internal;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.transport.common.utils.exception.TransportException;
import io.transport.core.protocol.message.Head;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;
import io.transport.core.utils.ByteBufUtils;

/**
 * 新注册(Web端/前端)设备输入消息（EG：用于Web端ws连接认证）
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
	private Set<Object> clientDeviceIds = new HashSet<>();

	public DeviceRegistMessage() {
		this.getHead().setActionId(MsgType.DEVICE_REGIS.getActionId());
	}

	public DeviceRegistMessage(String appId, Set<Object> clientDeviceIds, Integer expired) {
		super();
		this.setAppId(appId);
		this.setClientDeviceIds(clientDeviceIds);
		this.setExpired(expired);
	}

	public Integer getExpired() {
		return expired;
	}

	public void setExpired(Integer expired) {
		if (expired <= 0)
			throw new TransportException("Protocol error, parameter 'expired' must greater than 0.");
		this.expired = expired;
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

	public Set<Object> getClientDeviceIds() {
		return clientDeviceIds;
	}

	public void setClientDeviceIds(Set<Object> clientDeviceIds) {
		this.clientDeviceIds = clientDeviceIds;
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		this.setAppId(ByteBufUtils.readString(in, ConnectMessage.ID_LEN));
		this.setExpired(in.readInt());

		int len = this.getHead().getTotalLen() - Head.HEAD_LEN - ConnectMessage.ID_LEN - 4;
		String deviceIdsTxt = ByteBufUtils.readString(in, len);
		if (deviceIdsTxt != null) {
			Set<Object> deviceIdsSet = Sets.newHashSet();
			for (String id : deviceIdsTxt.split(",")) {
				deviceIdsSet.add(id);
			}
			this.setClientDeviceIds(deviceIdsSet);
		}
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

}
