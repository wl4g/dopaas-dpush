package io.transport.common.executor;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.transport.common.SpringContextHolder;

/**
 * Secure thread executor.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年2月8日
 * @since
 */
public enum SafeThreadPoolExecutors {
	/**
	 * Hbase persistent thread pool.
	 */
	hbasePersisPool;

	final private static Logger logger = LoggerFactory.getLogger(SafeThreadPoolExecutors.class);

	private ExecutorService executor;
	private int corePoolSize;
	private int maximumPoolSize;
	private int keepAliveTime;
	private int safeAcceptCount;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	SafeThreadPoolExecutors() {
		PoolConfiguration poolConfig = SpringContextHolder.getBean(PoolConfiguration.class);
		Map<String, Integer> item = (Map) poolConfig.getProperties().get(name());

		this.corePoolSize = item.getOrDefault("corePoolSize", 1);
		this.maximumPoolSize = item.getOrDefault("maximumPoolSize", 5);
		this.keepAliveTime = item.getOrDefault("keepAliveTime", 0);
		this.safeAcceptCount = item.getOrDefault("acceptCount", 500);
		if (this.safeAcceptCount <= 0)
			throw new IllegalArgumentException("The `acceptCount` task receive queue size must be greater than 0.");

		// Add shutdown hook.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				this.getExecutor().shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	}

	/**
	 * Get the thread pool created by the current enumeration instance.
	 * 
	 * @return ExecutorService
	 */
	public ExecutorService getExecutor() {
		if (this.executor != null)
			return this.executor;
		else
			return (this.executor = new InternalSafeThreadPoolExecutor(name(), getCorePoolSize(), getMaximumPoolSize(),
					getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(getSafeAcceptCount())));
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public int getKeepAliveTime() {
		return keepAliveTime;
	}

	public int getSafeAcceptCount() {
		return safeAcceptCount;
	}

	/**
	 * Proxy Thread pool executor.
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年5月7日
	 * @since
	 */
	public static class InternalSafeThreadPoolExecutor extends ThreadPoolExecutor {

		private String poolName;

		public InternalSafeThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize, long keepAliveTime,
				TimeUnit unit, BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
			this.poolName = poolName;
		}

		@Override
		public Future<?> submit(Runnable task) {
			return super.submit(() -> {
				try {
					task.run();
				} catch (Exception e) {
					logger.error("Processing " + poolName + " fail.", e);
				}
			});
		}

		@Override
		public <T> Future<T> submit(Runnable task, T result) {
			return super.submit(() -> {
				try {
					task.run();
				} catch (Exception e) {
					logger.error("Processing " + poolName + " fail.", e);
				}
			}, result);
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return super.submit(() -> {
				try {
					return task.call();
				} catch (Exception e) {
					logger.error("Processing " + poolName + " fail.", e);
				}
				return null;
			});
		}

	}

	/**
	 * Executor thread pool configuration
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0 2018年6月2日
	 * @since
	 */
	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	@ConfigurationProperties(prefix = PoolConfiguration.EXECUTOR_CONF_P)
	public static class PoolConfiguration {
		final public static String EXECUTOR_CONF_P = "executors";

		private Map<String, Object> properties;

		public Map<String, Object> getProperties() {
			return properties;
		}

		public void setProperties(Map<String, Object> properties) {
			this.properties = properties;
		}

	}

}
