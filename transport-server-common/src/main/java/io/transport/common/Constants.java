package io.transport.common;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 自定义协议消息相关常量
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
final public class Constants {

	/**
	 * 全局UTF-8编码
	 */
	final public static Charset UTF_8 = Charset.forName("UTF-8");

	/**
	 * 心跳PING数据包
	 */
	final public static String H_PING = "H";

	/**
	 * 系统名
	 */
	public static String osName;

	/**
	 * 系统唯一标识
	 */
	public static String ethSerial;

	/**
	 * 系统唯一标识
	 */
	public static String hostSerial;

	/**
	 * 系统应用唯一标识
	 */
	public static String appSerial;

	/**
	 * 系统应用程序进程唯一ID
	 */
	public static String processId;

	/**
	 * 系统应用进程唯一标识
	 */
	public static String processSerial;

	static {
		try {
			// 系统名
			osName = String.valueOf(System.getProperty("os.name")).toLowerCase();
			//
			// 系统网卡唯一标识
			ethSerial = DigestUtils.md5Hex(osName + getLocalMac()).substring(8, 24);
			//
			// 系统应用程序唯一标识
			String packagePath = Constants.class.getProtectionDomain().getCodeSource().getLocation().toString();
			appSerial = DigestUtils.md5Hex(ethSerial + packagePath);
			//
			// 系统应用程序进程唯一标识
			// get pid running Java virtual machine.
			processId = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
			processSerial = appSerial.substring(0, appSerial.length() - processId.length()) + processId;
		} catch (Exception e) {
			throw new RuntimeException("Transport设备资源初始化失败.", e);
		}
	}

	private static String getLocalMac() throws IOException {
		// 获取网卡，获取地址
		byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();

		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mac.length; i++) {
			if (i != 0)
				sb.append("-");

			// 字节转换为整数
			int temp = mac[i] & 0xff;
			String str = Integer.toHexString(temp);
			if (str.length() == 1)
				sb.append("0" + str);
			else
				sb.append(str);
		}

		return sb.toString().toLowerCase().replaceAll("-", "");
	}

	public static void main(String[] args) throws IOException {
		System.out.println("osName=" + osName);
		System.out.println("ethSerial=" + ethSerial + ", length=" + ethSerial.length());
		System.out.println("appSerial=" + appSerial + ", length=" + appSerial.length());
		System.out.println("processId=" + processId + ", length=" + processId.length());
		System.out.println("processSerial=" + processSerial + ", length=" + processSerial.length());
	}
}
