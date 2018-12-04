package io.transport.sdk.protocol.message.internal;

import java.util.HashSet;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.transport.sdk.protocol.message.Head;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.MsgType;
import io.transport.sdk.utils.ByteBufs;

/**
 * 请求建立连接输出消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class ConnectRespMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;

	private Set<String> hostAndPorts = new HashSet<String>();

	public ConnectRespMessage() {
		this.getHead().setActionId(MsgType.CONNECT_RESP.getActionId());
	}

	public Set<String> getHostAndPorts() {
		return hostAndPorts;
	}

	public void setHostAndPorts(Set<String> hostAndPorts) {
		this.hostAndPorts = hostAndPorts;
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		int len = this.getHead().getTotalLen() - Head.HEAD_LEN;
		if (len > 0) {
			// 返回集群节点列表
			String haps = ByteBufs.toString(in, len);
			if (haps != null && haps.trim().length() != 0) {
				for (String hap : haps.split(","))
					this.hostAndPorts.add(hap);
			}
		}
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

}
