package io.transport.sdk.utils;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.transport.sdk.exception.TransportException;

public class ByteBufs {

	/**
	 * 将字符串转为网络字节缓冲内容
	 * 
	 * @param s
	 *            字符串
	 * @return 网络字节缓冲内容
	 */
	public static ByteBuf toByteBuf(String s) {
		return Unpooled.copiedBuffer(s.getBytes());
	}

	/**
	 * 将ByteBuf转为字符串
	 * 
	 * @param buf
	 * @param readBytes
	 * @return
	 */
	public static String toString(ByteBuf buf, int readBytes) {
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
	 * 将byte[]转为字符串
	 * 
	 * @param buf
	 * @return
	 */
	public static String toString(byte[] buf) {
		return new String(buf, CharsetUtil.UTF_8);
	}

	/**
	 * 将ByteBuf转为指定编码byte[]
	 * 
	 * @param buf
	 * @return
	 */
	public static byte[] toBytes(ByteBuf buf, int readBytes) {
		if (buf == null)
			return null;

		byte[] arr = new byte[readBytes];
		buf.readBytes(arr);
		return arr;
	}

	/**
	 * 将字符串转为指定编码byte[]
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
	 * 合并拼接字节数组
	 * 
	 * @param bys
	 * @return
	 */
	public static byte[] joins(byte[]... bys) {
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
	 * 截取字节
	 * 
	 * @param msg
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] subMsgBytes(byte[] msg, int start, int end) {
		byte[] msgByte = new byte[end - start];
		int j = 0;
		for (int i = start; i < end; i++) {
			msgByte[j] = msg[i];
			j++;
		}
		return msgByte;
	}

	/**
	 * UCS2解码
	 * 
	 * @param src
	 *            UCS2 源串
	 * @return 解码后的UTF-16BE字符串
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
	 * UCS2编码
	 * 
	 * @param src
	 *            UTF-16BE编码的源串
	 * @return 编码后的UCS2串
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
	 * 扩充定长字节数组缓冲区
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
	 * 扩充定长字符串缓冲区
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
	 * 扩充定长字符串缓冲区
	 * 
	 * @param orgin
	 * @param totalLen
	 * @return
	 */
	public static String coverFixedString(String orgin, int totalLen) {
		return new String(coverFixedBuf(orgin, totalLen), CharsetUtil.UTF_8);
	}

	/**
	 * 判空
	 * 
	 * @param obj
	 * @return
	 * @sine
	 */
	public static boolean isEmpty(Object obj) {
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
	 * 包裝空字符串参数
	 * 
	 * @param str
	 * @return
	 */
	public static String empty(String str) {
		return isEmpty(str) ? "" : str.trim();
	}

	public static void main(String[] args) {
		// int len = 17;
		// int action = 0x00000001;
		// int seq = 123456;
		// int re = 0;
		//
		// byte[] len_b = TypeConvert.int2byte(len);
		// byte[] action_b = TypeConvert.int2byte(action);
		// byte[] seq_b = TypeConvert.int2byte(seq);
		// byte[] re_b = TypeConvert.int2byte(re);
		//
		// System.out.println(joins(len_b, action_b, seq_b, re_b));

		Integer[] ss = new Integer[] { 9 };
		System.out.println(isEmpty(ss));
		System.out.println('0');
		System.out.println("-=-=-=");
		System.out.println("\0");
		System.out.println("-----");
		System.out.println('\0');
		System.out.println("=====");
		System.out.println("--------->>");
		System.out.println(String.format("%02d", 2));

		System.out.println("---00000000");
		byte[] orgin = new byte[] { -1, 2, 23, 4 };
		for (byte b : coverFixedBuf(orgin, 6)) {
			System.out.print(b + ",");
		}
		System.out.println("----------=====");
		System.out.println(coverFixedString("abcd", 6));
	}

}
