package io.transport.core.protocol.message;

import io.transport.core.protocol.message.internal.ActiveMessage;
import io.transport.core.protocol.message.internal.ActiveRespMessage;
import io.transport.core.protocol.message.internal.ClosingMessage;
import io.transport.core.protocol.message.internal.ConnectMessage;
import io.transport.core.protocol.message.internal.ConnectRespMessage;
import io.transport.core.protocol.message.internal.DeviceRegistMessage;
import io.transport.core.protocol.message.internal.DeviceRegistRespMessage;
import io.transport.core.protocol.message.internal.ResultRespMessage;
import io.transport.core.protocol.message.internal.TransportAckRespMessage;
import io.transport.core.protocol.message.internal.TransportMessage;

/**
 * 消息类型枚举
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月12日
 * @since
 */
public enum MsgType {

	/**
	 * 建立连接结果输出
	 */
	CONNECT((byte) 1, ConnectMessage.class),
	/**
	 * 请求建立连接输入
	 */
	CONNECT_RESP((byte) 2, ConnectRespMessage.class),
	/**
	 * 链路检测请求
	 */
	ACTIVE((byte) 3, ActiveMessage.class),
	/**
	 * 链路检测响应
	 */
	ACTIVE_RESP((byte) 4, ActiveRespMessage.class),
	/**
	 * 消息推送
	 */
	TRANSPORT((byte) 5, TransportMessage.class),
	/**
	 * 消息推送结果返回
	 */
	TRANSPORT_RESP((byte) 6, TransportAckRespMessage.class),
	/**
	 * 拆除/关闭连接输入
	 */
	CLOSE((byte) 7, ClosingMessage.class),
	/**
	 * 处理结果消息输出
	 */
	RET_RESP((byte) 8, ResultRespMessage.class),
	/**
	 * 新注册(Web端/前端)设备请求消息（EG：用于Web端ws连接认证）
	 */
	DEVICE_REGIS((byte) 9, DeviceRegistMessage.class),
	/**
	 * 新注册(Web端/前端)设备返回消息（EG：用于Web端ws连接认证）
	 */
	DEVICE_REGIS_RESP((byte) 10, DeviceRegistRespMessage.class);

	byte actionId; // action_Id值
	Class<? extends Message> msgClass; // 应答消息封装对象Class

	private MsgType() {
	}

	private MsgType(byte actionId) {
		this.actionId = actionId;
	}

	private MsgType(byte actionId, Class<? extends Message> respMsgClass) {
		this.actionId = actionId;
		this.msgClass = respMsgClass;
	}

	public byte getActionId() {
		return actionId;
	}

	public Class<? extends Message> getMsgClass() {
		return msgClass;
	}

	public static MsgType ofType(byte actionId) {
		for (MsgType mt : values()) {
			if (mt.getActionId() == actionId) {
				return mt;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		// byte i = 10;
		// System.out.println(ofType(i));
	}

}