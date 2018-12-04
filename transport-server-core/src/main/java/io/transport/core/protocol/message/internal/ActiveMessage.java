package io.transport.core.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;

/**
 * 链路检测输入消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class ActiveMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;

	public ActiveMessage() {
		this.getHead().setActionId(MsgType.ACTIVE.getActionId());
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

}
