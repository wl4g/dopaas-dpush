package io.transport.core.service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.transport.common.Constants;
import io.transport.common.bean.ChannelMetricsInfo;
import io.transport.common.bean.ChannelMetricsInfo.EnvInfo;
import io.transport.common.bean.ChannelMetricsInfo.RuntimeInfo;
import io.transport.common.utils.exec.SimpleCommand;
import io.transport.core.MonitorService;

/**
 * Netty核心服务监控Service实现
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月2日
 * @since
 */
@Service
public class DefaultMonitorService implements MonitorService {
	final private static Logger logger = LoggerFactory.getLogger(DefaultMonitorService.class);

	@Autowired
	private Environment env;

	@Override
	public ChannelMetricsInfo metricsInfo() {
		ChannelMetricsInfo info = new ChannelMetricsInfo();
		try {
			// Environment info.
			this.fillEnvInfo(info);
			// Runtime info.
			this.fillRuntimeInfo(info);
		} catch (Exception e) {
			logger.error("Get channel metrics info failed.", e);
		}
		return info;
	}

	/**
	 * Get filled environment parameters
	 * 
	 * @param info
	 */
	private void fillEnvInfo(ChannelMetricsInfo info) {
		EnvInfo env = info.getEnvInfo();
		env.setNettyAllocatorPageSize(fieldValue(PooledByteBufAllocator.class, "DEFAULT_PAGE_SIZE"));
		env.setNettyAllocatorMaxOrder(fieldValue(PooledByteBufAllocator.class, "DEFAULT_MAX_ORDER"));
		env.setNettyAllocatorTinyCacheSize(fieldValue(PooledByteBufAllocator.class, "DEFAULT_TINY_CACHE_SIZE"));
		env.setNettyAllocatorSmallCacheSize(fieldValue(PooledByteBufAllocator.class, "DEFAULT_SMALL_CACHE_SIZE"));
		env.setNettyAllocatorNormalCacheSize(fieldValue(PooledByteBufAllocator.class, "DEFAULT_NORMAL_CACHE_SIZE"));
		env.setNettyAllocatorMaxCachedBufferCapacity(
				fieldValue(PooledByteBufAllocator.class, "DEFAULT_MAX_CACHED_BUFFER_CAPACITY"));
		env.setNettyAllocatorCacheTrimInterval(fieldValue(PooledByteBufAllocator.class, "DEFAULT_CACHE_TRIM_INTERVAL"));
		env.setNettyAllocatorNumHeapArenas(fieldValue(PooledByteBufAllocator.class, "DEFAULT_NUM_HEAP_ARENA"));
		env.setNettyAllocatorNumDirectArenas(fieldValue(PooledByteBufAllocator.class, "DEFAULT_NUM_DIRECT_ARENA"));

		env.setNettyLeakDetectionLevel(fieldValue(ResourceLeakDetector.class, "level"));
		env.setNettyLeakDetectionMaxRecords(fieldValue(ResourceLeakDetector.class, "MAX_RECORDS"));
		env.setNettyMaxDirectMemory(fieldValue(PlatformDependent.class, "DIRECT_MEMORY_LIMIT"));
		env.setNettyNoPreferDirect(fieldValue(PlatformDependent.class, "DIRECT_BUFFER_PREFERRED"));
		env.setNettyNoUnsafe(fieldValue(PlatformDependent.class, "IS_EXPLICIT_NO_UNSAFE"));
		env.setNettyTmpdir(SystemPropertyUtil.get("io.netty.tmpdir"));
		env.setNettyBitMode(methodValue(PlatformDependent.class, "bitMode0"));

		env.setIsAndroid(fieldValue(PlatformDependent.class, "IS_ANDROID"));
		env.setIsWindows(fieldValue(PlatformDependent.class, "IS_WINDOWS"));
		env.setIsRoot(fieldValue(PlatformDependent.class, "IS_ROOT"));
		env.setNoJavassist(fieldValue(PlatformDependent.class, "HAS_JAVASSIST"));

		env.setNettyDefaultPromiseMaxListenerStackDepth(fieldValue(DefaultPromise.class, "MAX_LISTENER_STACK_DEPTH"));

		// JVM运行时环境
		env.setJavaIOTmpdir(System.getProperty("java.io.tmpdir"));
		env.setJavaVersion(System.getProperty("java.version"));
	}

	/**
	 * Get the fill runtime parameters
	 * 
	 * @param info
	 */
	private void fillRuntimeInfo(ChannelMetricsInfo info) {
		RuntimeInfo rt = info.getRuntimeInfo();
		rt.setNodeName(env.getProperty("spring.application.name", "unknown"));
		rt.setProcess(Constants.processId);
		rt.setCpus(Runtime.getRuntime().availableProcessors() + "");
		rt.setJvmUsedTotalMemory((Runtime.getRuntime().totalMemory() / 1048576) + "/MB");
		rt.setJvmFreeMemory((Runtime.getRuntime().freeMemory() / 1048576) + "/MB");
		rt.setJvmMaxMemory((Runtime.getRuntime().maxMemory() / 1048576) + "/MB");
		MemoryMXBean mmb = ManagementFactory.getMemoryMXBean();
		rt.setJvmUsedHeapMemory((mmb.getHeapMemoryUsage().getUsed() / 1048576) + "/MB");
		rt.setJvmMaxHeapMemory((mmb.getHeapMemoryUsage().getMax() / 1048576) + "/MB");
		rt.setJvmUsedNonHeapMemory((mmb.getNonHeapMemoryUsage().getUsed() / 1048576) + "/MB");
		rt.setJvmMaxNonHeapMemory((mmb.getNonHeapMemoryUsage().getMax() / 1048576) + "/MB");
		rt.setJvmTotalStartedThreadCount(ManagementFactory.getThreadMXBean().getTotalStartedThreadCount() + "");

		// Listen ports info.
		rt.setPortWS(env.getProperty("core.websocket.port"));
		rt.setPortRPC(env.getProperty("core.rpc.port"));
		rt.setPortActor(env.getProperty("akka.remote.port"));
		if (!Boolean.valueOf(info.getEnvInfo().getIsWindows())) {
			rt.setSysOpenfiles(SimpleCommand.exec("cat /proc/sys/fs/file-max"));
			rt.setProcessOpenfiles(SimpleCommand.exec("lsof -p " + rt.getProcess() + "|wc –l"));
			rt.setPortWSOpenfiles(SimpleCommand.exec("lsof -i:" + rt.getPortWS() + "|wc -l"));
			rt.setPortRPCOpenfiles(SimpleCommand.exec("lsof -i:" + rt.getPortRPC() + "|wc -l"));
			rt.setPortActorOpenfiles(SimpleCommand.exec("lsof -i:" + rt.getPortActor() + "|wc -l"));
			// System free info.
			rt.setSystemFreeMemory(SimpleCommand.exec("free -m|awk 'NR==2{print $4}'"));
		}

	}

	/**
	 * Get field values
	 * 
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	private String fieldValue(Class<?> clazz, String fieldName) {
		Field f = ReflectionUtils.findField(clazz, fieldName);
		f.setAccessible(true);
		Object val = ReflectionUtils.getField(f, null);
		if (val != null && val.getClass().isEnum()) {
			Enum<?> e = (Enum<?>) val;
			val = e.name();
		}
		return String.valueOf(val);
	}

	/**
	 * Get the method value
	 * 
	 * @param clazz
	 * @param methodName
	 * @return
	 */
	private String methodValue(Class<?> clazz, String methodName) {
		Method m = ReflectionUtils.findMethod(clazz, methodName);
		m.setAccessible(true);
		return String.valueOf(ReflectionUtils.invokeMethod(m, null));
	}

}
