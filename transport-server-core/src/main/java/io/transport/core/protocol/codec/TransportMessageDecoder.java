package io.transport.core.protocol.codec;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.transport.common.utils.exception.TransportException;
import io.transport.core.config.Configuration.RpcConfig;
import io.transport.core.exception.TransportDDOSAmbiguousException;
import io.transport.core.protocol.message.Head;
import io.transport.core.protocol.message.Message;
import io.transport.core.protocol.message.MsgType;

/**
 * Receiving a message decoder that has processed the DDoS attack stream.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @time 2016年9月22日
 * @since
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // 多例
public class TransportMessageDecoder extends ByteToMessageDecoder {
	final private static Logger logger = LoggerFactory.getLogger(TransportMessageDecoder.class);
	@Resource
	private RpcConfig conf;

	private State state = State.Head;
	private int totalLen = 0; // Total message length
	private short version = (short) 0; // Version number
	private byte actionId = (byte) 0; // Action type
	private byte reserve = (byte) 0; // Reserved word
	private Message msg = null; // Decode message

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try {
			int len = in.readableBytes();
			if (logger.isDebugEnabled()) {
				logger.debug("Begin decode, hashCode={}, readableBytes={}, rIndex={}, wIndex={}", in.hashCode(), len,
						in.readerIndex(), in.writerIndex());
			}

			switch (this.state) {
			// 1.0 Message header parsing
			case Head:
				if (len < Head.HEAD_LEN)
					return; // Less than message header length returns.

				this.totalLen = in.readInt(); // Total message length.
				// DDoS attack check.
				if (Math.max(len, totalLen) > this.conf.getMaxContentLength()) {
					// Mandatory settings have been read and all the data of
					// this package is ignored.
					this.skipPacketsAll(in);

					throw new TransportDDOSAmbiguousException(
							"Suspected DDoS attack packets, readableBytes is " + len + ", totalLen=" + totalLen);
				}
				this.version = in.readShort(); // version number(100/101...).
				this.actionId = in.readByte(); // Message action.
				this.reserve = in.readByte(); // Reserved field.

				// 1.1 Match message.
				this.msg = this.determineMsgMatching(this.actionId);

				// 1.2 Setting the message head.
				this.msg.setHead(new Head(totalLen, version, actionId, reserve));
				this.state = State.Body;
				if (logger.isDebugEnabled())
					logger.debug("Begin decode, hashCode={}, readableBytes={}, rIndex={}, wIndex={}, head={}",
							in.hashCode(), in.readableBytes(), in.readerIndex(), in.writerIndex(), msg.getHead());

			case Body: // 2.1 Message body parsing
				if (len < (this.totalLen - Head.HEAD_LEN))
					return;

				this.msg.readByteBufDecoder(in);
				this.state = State.Completed;
				if (logger.isDebugEnabled())
					logger.debug("Begin decode, hashCode={}, readableBytes={}, rIndex={}, wIndex={}", in.hashCode(),
							in.readableBytes(), in.readerIndex(), in.writerIndex());
				break;
			default:
				throw new IllegalArgumentException("Illegal decoded state. " + state);
			}
			out.add(this.msg);
			if (logger.isDebugEnabled())
				logger.debug("解码完成.{}", msg);

		} catch (Throwable t) {
			// All packets are ignored when decoding exceptions.
			this.skipPacketsAll(in);
			logger.error("decode()解码失败. {}, msg={}", t.getMessage(), this.msg);
			throw t;
		} finally {
			// It has been released in
			// super.io.netty.handler.codec.ByteToMessageDecoder.channelRead().
			if (this.state == State.Completed) {
				this.state = State.Head;
				// Other follow-up processing.
				// ...
			}
		}
	}

	/**
	 * Determining an instance of the corresponding response message class based
	 * on actionId
	 * 
	 * @param actionId
	 * @return
	 * @throws Exception
	 * @sine
	 */
	private Message determineMsgMatching(byte actionId) throws Exception {
		MsgType mt = MsgType.ofType(actionId);
		if (mt != null)
			return mt.getMsgClass().newInstance();
		throw new TransportException("Unknown message type.");
	}

	/**
	 * Skip over and ignore all.<br/>
	 * For example, when a DDoS attack is found, it needs to be called
	 * 
	 * @param in
	 */
	private void skipPacketsAll(ByteBuf in) {
		in.readerIndex(in.writerIndex());
	}

	/**
	 * Decode state stick state control
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年1月5日
	 * @since
	 */
	static enum State {
		Head, Body, Completed;
	}

}
