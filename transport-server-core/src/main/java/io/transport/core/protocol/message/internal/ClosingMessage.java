package io.transport.core.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;

/**
 * 拆除/关闭连接输入消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class ClosingMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;

	public ClosingMessage() {
		this.getHead().setActionId(MsgType.CLOSE.getActionId());
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

}
