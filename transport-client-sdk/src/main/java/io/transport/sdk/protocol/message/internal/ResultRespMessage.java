package io.transport.sdk.protocol.message.internal;

import io.netty.buffer.ByteBuf;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.MsgType;
import io.transport.sdk.utils.ByteBufs;

/**
 * 处理结果消息服务器返回消息
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
		this.getHead().setActionId(MsgType.RET_RESP.getActionId());
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public void readByteBufDecoder(ByteBuf in) {
		int codeLen = in.readInt();
		this.setCode(ByteBufs.toString(in, codeLen));
		int typeLen = in.readInt();
		this.setType(ByteBufs.toString(in, typeLen));
		int msgLen = in.readInt();
		this.setMessage(ByteBufs.toString(in, msgLen));
	}

	@Override
	public void writeBodyBufEncoder(ByteBuf out) {
		throw new UnsupportedOperationException("不支持的操作.");
	}

	@Override
	public String toString() {
		return "ErrRespMessage [message=" + message + "]";
	}

	public static enum RetCode {

		/**
		 * 成功
		 */
		OK("0", "ok."),
		/**
		 * 认证失败.
		 */
		AUTH_FAIL("-401", "认证失败."),
		/**
		 * 业务操作失败
		 */
		BIZ_FAIL("-1", "操作失败."),
		/**
		 * 系统异常.
		 */
		SYS_ERR("500", "网络好像有点问题，请稍后再试.");

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
