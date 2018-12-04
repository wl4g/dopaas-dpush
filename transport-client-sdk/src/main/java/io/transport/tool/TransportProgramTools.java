package io.transport.tool;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;

import io.netty.util.CharsetUtil;
import io.transport.sdk.Configuration;
import io.transport.sdk.TransportClient;
import io.transport.sdk.protocol.handler.ReceiveTextHandler;
import io.transport.sdk.protocol.message.internal.ResultRespMessage;
import io.transport.sdk.protocol.message.internal.TransportMessage;
import io.transport.sdk.store.Store;

/**
 * 测试工具程序<br/>
 * 可用于简单并发压测.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年3月21日
 * @since
 */
public class TransportProgramTools {
	final static Random random = new Random();
	final static Map<String, String> globalCache = new ConcurrentHashMap<String, String>();
	// Test
	// static String appId = "adb94919b24ef570";
	// static String appSecret = "daa6844b5ca248b08c90161f9f434724";
	// Prod
	static String appId = "fc3a4359b951b450";
	static String appSecret = "cae10f490378fc5621792e6691d03dee";
	// for test group id.
	static String groupId = "abcdef1111567890";
	// for test to group id.
	static String toGroupId = "3468557178274816";
	// for test to device id.
	static String toDeviceId = "abfd478466344e7ba69619fff2746976";

	public static void main(String[] args) throws Exception {
		// String hostAndPort = "127.0.0.1:10030";
		String hostAndPort = "47.107.72.60:10030";
		// String hostAndPort = "push.anjiancloud.com:80";
		int threads = 1, count = 1;
		if (args != null && args.length >= 3) {
			hostAndPort = args[0];
			threads = Integer.parseInt(args[1]);
			count = Integer.parseInt(args[2]);
		}

		for (int t = 0; t < threads; t++) {
			final String hostAndPort0 = hostAndPort;
			final int threads0 = threads;
			final int count0 = count;
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						String tName = Thread.currentThread().getName();
						System.out.println("开始执行... threadName=" + tName);
						execute(hostAndPort0, tName, threads0, count0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	/**
	 * 执行并发请求 <br/>
	 * 启动一个线程推送消息.
	 * 
	 * @param hostAndPort
	 *            服务器地址
	 * @param tName
	 *            并发线程名
	 * @param threads
	 *            并发数
	 * @param count
	 *            每个线程发送消息数
	 * @throws Exception
	 */
	private static void execute(String hostAndPort, String tName, int threads, int count) throws Exception {
		// 1.0 Create config object.
		Configuration config = new Configuration(appId, appSecret, groupId, MyReceiveTextHandler.class, new Store() {

			@Override
			public void put(String key, String value) {
				// TODO Auto-generated method stub
				globalCache.put(key, value);
			}

			@Override
			public String get(String key) {
				// TODO Auto-generated method stub
				return globalCache.get(key);
			}

			@Override
			public void remove(String key) {
				// TODO Auto-generated method stub
				globalCache.remove(key);
			}
		});
		config.setHostAndPorts(hostAndPort);
		// If concurrent tests are performed, each concurrent deviceId needs to
		// be set differently.
		if (threads > 1) {
			// Maximum can only be 32 byte.
			config.setDeviceId(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
		}
		// config.setLevel(io.transport.sdk.logger.Level.DEBUG);

		// 2.0 Create Transport instance and connect.
		TransportClient client = new TransportClient(config).join();
		// build.destroy(); // destroy instance.

		// For test the registered WS client.
		// System.out.println("准备注册WS连接设备... threadName=" + tName);
		// client.registered(24 * 60 * 60, "111111111122222222223333333333ab");

		// Definition message.
		String payload = "{\"testKey\":\"test1\", \"TestData\":\"发送给浏览器的测试JSON报文ABCDEFG123456\"}";
		int len = payload.getBytes(CharsetUtil.UTF_8).length;

		// Loop sending.
		for (int c = 0; c < count; c++) {
			// client.unicast(toDeviceId, payload);
			System.out.println("准备发送测试报文至浏览器 i=" + c + ", threadName=" + tName + ", len=" + len);
			client.groupcast(toGroupId, "test", payload);
			Thread.sleep(random.nextInt(1000)); // Random wait.
		}
		System.out.println("测试执行结束. threadName=" + tName);
		Thread.sleep(200000L);
		System.exit(0);
	}

	/**
	 * 测试回调处理程序
	 * 
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年3月21日
	 * @since
	 */
	public static class MyReceiveTextHandler extends ReceiveTextHandler {

		@Override
		protected void onConnected(String deviceIdToken) {
			System.out.println("认证成功... deviceToken=" + deviceIdToken);
		}

		@Override
		protected void onMessage(TransportMessage msg) {
			// TODO Auto-generated method stub
			System.out.println("服务器返回数据报文..." + JSON.toJSONString(msg));
		}

		@Override
		protected void onResult(ResultRespMessage msg) {
			// TODO Auto-generated method stub
			System.out.println("服务器返回处理结果报文..." + JSON.toJSONString(msg));
		}

	}

}
