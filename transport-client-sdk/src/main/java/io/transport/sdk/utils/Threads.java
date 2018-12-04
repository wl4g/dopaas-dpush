package io.transport.sdk.utils;

/**
 * Thread Tool.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月31日
 * @since
 */
public class Threads {
	final private static ThreadLocal<Object> currentLocal = new ThreadLocal<Object>();

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

	public static <T> void setCurrent(T value) {
		currentLocal.set(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getCurrent() {
		return (T) currentLocal.get();
	}

	public static void removeCurrent() {
		currentLocal.remove();
	}
}
