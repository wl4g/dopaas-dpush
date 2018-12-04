package io.transport.core.protocol.message;

import java.io.Serializable;

/**
 * 消息头
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class Head implements Serializable {
	private static final long serialVersionUID = -9002874408437050378L;
	/**
	 * 协议头长度(bytes)
	 */
	final public static int HEAD_LEN = 4 + 2 + 1 + 1;

	private int totalLen; // 消息总长度(含消息头及消息体)
	private short version = Version.Version_1_0_0.getVersion(); // 版本号
	private byte actionId; // 命令或响应类型
	private byte reserve; // 保留字

	public Head() {
		super();
	}

	public Head(int totalLen, byte actionId) {
		this.totalLen = totalLen;
		this.actionId = actionId;
	}

	public Head(int totalLen, short version, byte actionId, byte reserve) {
		super();
		this.totalLen = totalLen;
		this.version = version;
		this.actionId = actionId;
		this.reserve = reserve;
	}

	public int getTotalLen() {
		return totalLen;
	}

	public void setTotalLen(int totalLen) {
		this.totalLen = totalLen;
	}

	public byte getActionId() {
		return actionId;
	}

	public void setActionId(byte actionId) {
		this.actionId = actionId;
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public byte getReserve() {
		return reserve;
	}

	public void setReserve(byte reserve) {
		this.reserve = reserve;
	}

	@Override
	public String toString() {
		return "Head [totalLen=" + totalLen + ", version=" + version + ", actionId=" + actionId + ", reserve=" + reserve
				+ "]";
	}

	/**
	 * Transporter服务协议版本
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年1月4日
	 * @since
	 */
	public static enum Version {

		Version_1_0_0((short) 100);

		private short version;

		private Version(short version) {
			this.version = version;
		}

		public short getVersion() {
			return version;
		}

	}

}
