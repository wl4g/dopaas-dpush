package io.transport.common.concurrent;

import org.apache.commons.lang3.RandomUtils;

/**
 * Thread Tool.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月31日
 * @since
 */
public class Threads {

	/**
	 * Fixed time range sleep
	 * 
	 * @param sleep
	 * @throws RuntimeException
	 */
	public static void sleep(long sleep) throws RuntimeException {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Random time range dormancy
	 * 
	 * @param min
	 * @param max
	 * @throws RuntimeException
	 */
	public static void sleep(long min, long max) throws RuntimeException {
		try {
			Thread.sleep(RandomUtils.nextLong(min, max));
		} catch (InterruptedException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
