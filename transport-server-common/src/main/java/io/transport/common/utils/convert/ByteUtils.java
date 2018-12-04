package io.transport.common.utils.convert;

import io.transport.common.Constants;

/**
 * 字符Buffer工具类
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月25日
 * @since
 */
public class ByteUtils {

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
			return buf.getBytes(Constants.UTF_8);
	}

	/**
	 * 合并拼接字节数组
	 * 
	 * @param bys
	 * @return
	 */
	public static byte[] joinBytes(byte[]... bys) {
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

	public static void main(String[] args) {
		// int len = 17;
		// int action = 0x00000001;
		// int seq = 123456;
		// int re = 0;
		// byte[] len_b = TypeConvert.int2byte(len);
		// byte[] action_b = TypeConvert.int2byte(action);
		// byte[] seq_b = TypeConvert.int2byte(seq);
		// byte[] re_b = TypeConvert.int2byte(re);
		// System.out.println(joinBytes(len_b, action_b, seq_b, re_b));
	}

}
