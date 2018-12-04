package io.transport.sdk.utils;

import java.io.IOException;
import java.lang.reflect.Method;
//import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;

import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;
import io.transport.sdk.exception.TransportException;

/**
 * 自定义协议消息相关常量
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月13日
 * @since
 */
final public class PlatformConstants {

	/**
	 * 全局UTF-8编码
	 */
	final public static Charset CHAR_UTF_8 = CharsetUtil.UTF_8;

	/**
	 * 系统名(Android不兼容J2SE SPI获取)
	 */
	@Deprecated
	public static String osName = "";

	/**
	 * 系统唯一标识
	 */
	public static String ethSerial = "";

	/**
	 * 系统应用程序唯一标识
	 */
	public static String appSerial = "";

	/**
	 * 系统应用程序进程唯一ID(Android不兼容J2SE SPI获取)
	 */
	@Deprecated
	public static String processId = "";

	/**
	 * 系统应用程序进程唯一标识(Android不兼容J2SE SPI获取)
	 */
	@Deprecated
	public static String processSerial = "";

	/**
	 * Activity class
	 */
	private static Class<?> activityClass;

	/**
	 * Initialization generation
	 */
	public static void initial(Object activity) {
		try {
			// 获取系统名
			// osName =
			// String.valueOf(System.getProperty("os.name")).toLowerCase();

			// 系统网卡唯一标识
			ethSerial = DigestUtils.md5Hex(localMac0()).substring(8, 24);

			// 获取应用包名(Android JDK不支持ManagementFactory)
			// String packagePath =
			// PlatformConstants.class.getProtectionDomain().getCodeSource().getLocation().toString();
			String packageUrl = localAppPackage0(activity);
			appSerial = DigestUtils.md5Hex(ethSerial + packageUrl);

			// 获取应用进程号(Android JDK不支持ManagementFactory)
			// Get PID running Java virtual machine.
			// processId =
			// ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
			// processSerial = appSerial.substring(0, appSerial.length() -
			// processId.length()) + processId;
		} catch (Exception e) {
			throw new TransportException("Failure of initialization platform parameters.", e);
		}
	}

	/**
	 * Check activity instance.
	 * 
	 * @param activity
	 */
	private static void checkActivity(Object activity) {
		try {
			boolean flag = false;
			activityClass = Class.forName("android.app.Activity", false, ClassLoader.getSystemClassLoader());
			for (Class<?> clazz = activity.getClass(); (clazz = activity.getClass().getSuperclass()) != null;) {
				if (clazz == activityClass)
					flag = true;
			}
			if (!flag)
				throw new TransportException("Not found `android.app.Activity`.");

		} catch (Exception e) {
			throw new TransportException(e);
		}
	}

	/**
	 * Access to network card information
	 * 
	 * @return
	 * @throws IOException
	 */
	private static String localMac0() throws IOException {
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

	/**
	 * Get the application package name
	 * 
	 * @param activity
	 * @return
	 * @throws IOException
	 */
	private static String localAppPackage0(Object activity) throws Exception {
		// Because Android is incompatible with J2SE SPI, So use reflection to
		// invoke Android activity instance to get.

		if (PlatformDependent.isAndroid() && activity != null) {
			// Check activity instance.
			checkActivity(activity);

			// Reflection execution to get the name of the package.
			Method m = activityClass.getDeclaredMethod("getApplication");
			m.setAccessible(true);
			Object application = m.invoke(activity);
			if (application != null) {
				Method m0 = application.getClass().getDeclaredMethod("getPackageName");
				m0.setAccessible(true);
				return String.valueOf(m0.invoke(application));
			}
		}
		// Backstage service used.
		else
			return PlatformConstants.class.getProtectionDomain().getCodeSource().getLocation().toString();

		return null;
	}

	public static void main(String[] args) throws IOException {
		initial(null);
		System.out.println("osName=" + osName);
		System.out.println("ethSerial=" + ethSerial + ", length=" + ethSerial.length());
		System.out.println("appSerial=" + appSerial + ", length=" + appSerial.length());
		System.out.println("processId=" + processId + ", length=" + processId.length());
		System.out.println("processSerial=" + processSerial + ", length=" + processSerial.length());
	}
}
