package io.transport.sdk.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.MsgType;

/**
 * 链路检测响应消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class ActiveRespMessage extends Message {
	private static final long serialVersionUID = 5283116454009585401L;

	public ActiveRespMessage() {
		this.getHead().setActionId(MsgType.ACTIVE_RESP.getActionId());
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

}
