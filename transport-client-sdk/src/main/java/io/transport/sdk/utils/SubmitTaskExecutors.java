package io.transport.sdk.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务执行器
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年2月8日
 * @since
 */
final public class SubmitTaskExecutors {
	final private static double LIMIT_COEFFICIENT = 0.4d; // 系数
	private static int POOL_SIZE = 1; // 线程数

	static {
		try {
			int numberOfCores = Runtime.getRuntime().availableProcessors();
			POOL_SIZE = (int) (numberOfCores / (1 - LIMIT_COEFFICIENT));
		} catch (Exception e) {
			System.out.println("Initializes task executor failured.");
			e.printStackTrace();
		}
	}

	public static ExecutorService getLimitExecutor() {
		return LimitKernelExecutor.getExecutor();
	}

	private static class LimitKernelExecutor {
		final private static ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);

		public static ExecutorService getExecutor() {
			return executor;
		}

	}

}
