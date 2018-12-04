package io.transport.core.protocol.message.internal;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.annotation.JSONField;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.transport.core.protocol.message.Head;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;

/**
 * 建立连接结果输出消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class ConnectRespMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;

	private Set<String> hostAndPorts = new HashSet<>();

	public ConnectRespMessage() {
		this.getHead().setActionId(MsgType.CONNECT_RESP.getActionId());
	}

	@JSONField(serialize = false)
	public byte[] getHostAndPortsBytes() {
		String haps = StringUtils.join(this.getHostAndPorts(), ",");
		if (!StringUtils.isEmpty(haps))
			return haps.getBytes(CharsetUtil.UTF_8);
		return new byte[] {};
	}

	public Set<String> getHostAndPorts() {
		return hostAndPorts;
	}

	public void setHostAndPorts(Set<String> hostAndPorts) {
		this.hostAndPorts = hostAndPorts;
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		byte[] bys = this.getHostAndPortsBytes();
		// 返回集群节点列表
		out.writeInt(Head.HEAD_LEN + bys.length);
		out.writeShort(this.getHead().getVersion());
		out.writeByte(this.getHead().getActionId());
		out.writeByte(this.getHead().getReserve());
		out.writeBytes(bys);
	}

	@Override
	public String toString() {
		return "ConnectRespMessage [getHead()=" + getHead() + "]";
	}

}
