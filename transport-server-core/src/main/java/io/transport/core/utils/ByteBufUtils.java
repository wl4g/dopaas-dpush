package io.transport.core.utils;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.transport.common.utils.exception.TransportException;

/**
 * Byte buffer related operating tools.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年3月14日
 * @since
 */
public class ByteBufUtils {

	/**
	 * Turn a string into a network byte buffering content
	 * 
	 * @param s
	 *            string
	 * @return Network byte buffering content(ByteBuf)
	 */
	public static ByteBuf wrapBuffer(String s) {
		return Unpooled.copiedBuffer(s.getBytes());
	}

	/**
	 * Turn ByteBuf into string. Note that it is not secure and requires
	 * external release.
	 * 
	 * @param buf
	 * @param readBytes
	 * @return
	 */
	public static String readString(ByteBuf buf, int readBytes) {
		/*
		 * Reference:io.netty.buffer.ByteBufUtil.readBytes()<br/> Must be
		 * released, otherwise the memory leaks.
		 */
		ByteBuf dst = buf.alloc().buffer(readBytes);
		try {
			buf.readBytes(dst);
			return dst.toString(CharsetUtil.UTF_8);
		} finally {
			dst.release();
		}
	}

	/**
	 * Convert ByteBuf to designated byte[], note that it is not secure and
	 * requires external release.
	 * 
	 * @param buf
	 * @return
	 */
	public static byte[] readBytes(ByteBuf buf, int readBytes) {
		if (buf == null)
			return null;
		/*
		 * Reference:io.netty.buffer.ByteBufUtil.readBytes()<br/> Must be
		 * released, otherwise the memory leaks.
		 */
		byte[] arr = new byte[readBytes];
		ByteBuf dst = buf.alloc().buffer(readBytes);
		try {
			buf.readBytes(arr);
		} finally {
			dst.release();
		}
		return arr;
	}

	/**
	 * Turn byte[] to string
	 * 
	 * @param buf
	 * @return
	 */
	public static String toString(byte[] buf) {
		return new String(buf, CharsetUtil.UTF_8);
	}

	/**
	 * Turn the string to the specified encoding byte[]
	 * 
	 * @param buf
	 * @return
	 */
	public static byte[] toBytes(String buf) {
		if (buf == null)
			return new byte[] {};
		else
			return buf.getBytes(CharsetUtil.UTF_8);
	}

	/**
	 * Merge spliced byte array
	 * 
	 * @param bys
	 * @return
	 */
	public static byte[] merge(byte[]... bys) {
		if (bys == null)
			return null;

		int len = 0;
		for (byte[] bs : bys)
			len += bs.length;

		byte[] total = new byte[len];
		for (int i = 0, j = 0; i < bys.length; i++) {
			System.arraycopy(bys[i], 0, total, j, bys[i].length);
			j += bys[i].length;
		}

		return total;
	}

	/**
	 * Intercepting bytes
	 * 
	 * @param msg
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] subBytes(byte[] msg, int start, int end) {
		byte[] msgByte = new byte[end - start];
		int j = 0;
		for (int i = start; i < end; i++) {
			msgByte[j] = msg[i];
			j++;
		}
		return msgByte;
	}

	/**
	 * UCS2 decoding
	 * 
	 * @param src
	 *            UCS2 source string
	 * @return The decoded UTF-16BE string
	 */
	public static String decodeUCS2(String src) {
		byte[] bytes = new byte[src.length() / 2];
		for (int i = 0; i < src.length(); i += 2) {
			bytes[i / 2] = (byte) (Integer.parseInt(src.substring(i, i + 2), 16));
		}
		String reValue = "";
		try {
			reValue = new String(bytes, "UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			reValue = "";
		}
		return reValue;

	}

	/**
	 * UCS2 coding
	 * 
	 * @param src
	 *            Source string encoded by UTF-16BE
	 * @return Encoded UCS2 strings
	 */
	public static String encodeUCS2(String src) {
		byte[] bytes;
		try {
			bytes = src.getBytes("UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			bytes = new byte[0];
		}
		StringBuffer reValue = new StringBuffer();
		StringBuffer tem = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			tem.delete(0, tem.length());
			tem.append(Integer.toHexString(bytes[i] & 0xFF));
			if (tem.length() == 1) {
				tem.insert(0, '0');
			}
			reValue.append(tem);
		}
		return reValue.toString().toUpperCase();
	}

	/**
	 * Extend the fixed length byte array buffer
	 * 
	 * @param orgin
	 *            参数
	 * @param totalLen
	 *            总长度
	 * @return
	 * @sine
	 */
	public static byte[] coverFixedBuf(byte[] orgin, int totalLen) {
		if (totalLen < orgin.length)
			throw new TransportException("The buffer array 'orgin' is over long and can only be 'totalLen'.");

		byte[] buf = new byte[totalLen];
		System.arraycopy(orgin, 0, buf, 0, orgin.length);
		return buf;
	}

	/**
	 * Extend the fixed length string buffer
	 * 
	 * @param orgin
	 * @param totalLen
	 * @return
	 */
	public static byte[] coverFixedBuf(String orgin, int totalLen) {
		byte[] src = empty(orgin).getBytes(CharsetUtil.UTF_8);
		return coverFixedBuf(src, totalLen);
	}

	/**
	 * Extend the fixed length string buffer
	 * 
	 * @param orgin
	 * @param totalLen
	 * @return
	 */
	public static String coverFixedString(String orgin, int totalLen) {
		return new String(coverFixedBuf(orgin, totalLen), CharsetUtil.UTF_8);
	}

	/**
	 * Emptiness
	 * 
	 * @param obj
	 * @return
	 * @sine
	 */
	public static boolean checkEmpty(Object obj) {
		if (obj == null)
			return true;
		if (obj instanceof CharSequence) {
			String tempStr = String.valueOf(obj);
			if ("".equals(tempStr))
				return true;
		}
		if (obj.getClass().isArray()) {
			Object[] objs = (Object[]) obj;
			if (objs == null || objs.length == 0)
				return true;
		}
		return false;
	}

	/**
	 * Package empty string parameter
	 * 
	 * @param str
	 * @return
	 */
	public static String empty(String str) {
		return checkEmpty(str) ? "" : str.trim();
	}

}
