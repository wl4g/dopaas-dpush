package io.transport.common.utils.convert;

import java.io.IOException;

import com.google.common.io.ByteStreams;

import io.transport.common.Constants;

/**
 * 16进制相关转换工具类
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年1月4日
 * @since
 */
public class HexConvert {

	/**
	 * byte to hex
	 * 
	 * @param bArray
	 * @return
	 */
	public static final String bytes2HexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * to byte
	 * 
	 * @param c
	 * @return
	 */
	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/**
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hexString2Bytes(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	/**
	 * hex字节流转acsii字符串
	 * 
	 * @param hex
	 * @return
	 * @throws IOException
	 */
	public static String hex2AcsiiString(String hex) throws IOException {
		return bytes2AcsiiString(hexString2Bytes(hex.replaceAll(" ", "")));
	}

	/**
	 * 字节流转acsii字符串
	 * 
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public static String bytes2AcsiiString(byte[] buf) throws IOException {
		return ByteStreams.asByteSource(buf).asCharSource(Constants.UTF_8).read();
	}

	/**
	 * 得到十六进制字符串的CRC16校验码,高位在前，低位在后
	 * 
	 * @param hexStr
	 * @return
	 */
	public static String toCrc16String(String hexStr) {
		byte[] bufData = hexString2Bytes(hexStr);
		int buflen = bufData.length;
		int CRC = 0x0000ffff;
		int POLYNOMIAL = 0x0000a001;
		int i, j;

		if (buflen == 0) {
			return "0000";
		}

		for (i = 0; i < buflen; i++) {
			CRC ^= ((int) bufData[i] & 0x000000ff);
			for (j = 0; j < 8; j++) {
				if ((CRC & 0x00000001) != 0) {
					CRC >>= 1;
					CRC ^= POLYNOMIAL;
				} else {
					CRC >>= 1;
				}
			}
		}

		String tempStr = Integer.toHexString(CRC);
		if (tempStr.length() < 4) {
			StringBuffer tempBuf = new StringBuffer(tempStr);
			for (int i1 = 0; i1 < 4 - tempStr.length(); i1++) {
				tempBuf.insert(0, "0");
			}
			return tempBuf.toString().toUpperCase();
		} else {
			return tempStr.toUpperCase();
		}
	}

	public static void main(String[] args) throws Exception {
		String hex = "E6 B5 8B E8 AF 95 E6 B5 8B E8 AF 95 E6 8A A5 E6 96 87 41 42 43 44 45 46 47 31 32 33 34 35 36";
		byte[] bs1 = hexString2Bytes(hex.replaceAll(" ", ""));
		// byte[] bs2 = Hex.decodeHex(hex.replaceAll(" ", "").toCharArray());
		for (byte b : bs1) {
			System.out.print(b + ",");
		}
		System.out.println(ByteStreams.asByteSource(bs1).asCharSource(Constants.UTF_8).read());
		System.out.println(">>>>");
		System.out.println(hex2AcsiiString(hex));
		System.out.println(">>>>");
		System.out.println(bytes2AcsiiString(bs1));
	}
}
