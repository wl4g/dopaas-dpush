package io.transport.core.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.transport.core.protocol.message.Head;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;
import io.transport.core.utils.ByteBufUtils;

/**
 * 处理结果输出消息
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
public class ResultRespMessage extends Message {
	private static final long serialVersionUID = 5283116454529585401L;

	private String code;
	private String type; // 对应具体处理的actionId
	private String message;

	public ResultRespMessage() {
		this(RetCode.OK, null);
	}

	public ResultRespMessage(RetCode retCode, String type) {
		this(retCode, type, null);
	}

	public ResultRespMessage(RetCode retCode, String type, String message) {
		this.getHead().setActionId(MsgType.RET_RESP.getActionId());
		this.setCode(retCode.getCode());
		this.setType(type);
		if (message == null)
			this.setMessage(retCode.getMsg());
		else
			this.setMessage(message);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if (type != null)
			this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		if (code != null)
			this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if (message != null)
			this.message = message;
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		byte[] codeBuf = ByteBufUtils.toBytes(this.getCode());
		byte[] typeBuf = ByteBufUtils.toBytes(this.getType());
		byte[] msgBuf = ByteBufUtils.toBytes(this.getMessage());
		out.writeInt(Head.HEAD_LEN + codeBuf.length + typeBuf.length + msgBuf.length);
		out.writeShort(this.getHead().getVersion());
		out.writeByte(this.getHead().getActionId());
		out.writeByte(this.getHead().getReserve());
		out.writeInt(codeBuf.length);
		out.writeBytes(codeBuf);
		out.writeInt(typeBuf.length);
		out.writeBytes(typeBuf);
		out.writeInt(msgBuf.length);
		out.writeBytes(msgBuf);
	}

	@Override
	public String toString() {
		return "ErrOutMessage [message=" + message + ", getHead()=" + getHead() + "]";
	}

	public static enum RetCode {

		/**
		 * 成功
		 */
		OK("0", "ok."),
		/**
		 * 认证失败.
		 */
		AUTH_FAIL("-401", "Authentication fail."),
		/**
		 * 业务操作失败
		 */
		BIZ_FAIL("-1", "Business restrictions failed."),
		/**
		 * 系统异常.
		 */
		SYS_ERR("500", "Network exception, please try again later.");

		private String code;
		private String msg;

		private RetCode(String code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public String getCode() {
			return code;
		}

		public String getMsg() {
			return msg;
		}

	}

}
