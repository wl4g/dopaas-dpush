package io.transport.common.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Real monitor message.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年3月7日
 * @since
 */
public class ChannelMetricsInfo extends Message {
	private static final long serialVersionUID = -6660013570456173675L;

	private RuntimeInfo runtimeInfo = new RuntimeInfo();
	private EnvInfo envInfo = new EnvInfo();

	public ChannelMetricsInfo() {
		super();
	}

	public RuntimeInfo getRuntimeInfo() {
		return runtimeInfo;
	}

	public void setRuntimeInfo(RuntimeInfo runtimeInfo) {
		this.runtimeInfo = runtimeInfo;
	}

	public EnvInfo getEnvInfo() {
		return envInfo;
	}

	public void setEnvInfo(EnvInfo envInfo) {
		this.envInfo = envInfo;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public static class RuntimeInfo {
		@JSONField(name = "nodeName")
		private String nodeName;
		@JSONField(name = "process")
		private String process;
		@JSONField(name = "cpus")
		private String cpus;

		@JSONField(name = "systemFreeMemory")
		private String systemFreeMemory;

		// JVM内存
		@JSONField(name = "jvmUsedTotalMemory")
		private String jvmUsedTotalMemory;
		@JSONField(name = "jvmFreeMemory")
		private String jvmFreeMemory;
		@JSONField(name = "jvmMaxMemory")
		private String jvmMaxMemory;
		@JSONField(name = "jvmUsedHeapMemory")
		private String jvmUsedHeapMemory; // 堆内存已使用
		@JSONField(name = "jvmMaxHeapMemory")
		private String jvmMaxHeapMemory; // 最大堆内存
		@JSONField(name = "jvmUsedNonHeapMemory")
		private String jvmUsedNonHeapMemory;// 堆外内存已使用
		@JSONField(name = "jvmMaxNonHeapMemory")
		private String jvmMaxNonHeapMemory;// 最大堆外内存
		@JSONField(name = "jvmTotalStartedThreadCount")
		private String jvmTotalStartedThreadCount;

		// 当前节点已监听端口
		@JSONField(name = "portWS")
		private String portWS; // 当前WS监听端口
		@JSONField(name = "portRPC")
		private String portRPC; // 当前RPC监听端口
		@JSONField(name = "portActor")
		private String portActor; // 当前Actor监听端口

		// 系统参数
		//
		@JSONField(name = "sysOpenfiles")
		private String sysOpenfiles; // 当前系统的文件打开总数
		@JSONField(name = "processOpenfiles")
		private String processOpenfiles; // 当前进程的文件打开数
		@JSONField(name = "portWSOpenfiles")
		private String portWSOpenfiles; // 当前WS端口的文件打开数
		@JSONField(name = "portRPCOpenfiles")
		private String portRPCOpenfiles; // 当前RPC端口的文件打开数
		@JSONField(name = "portActorOpenfiles")
		private String portActorOpenfiles; // 当前Actor端口的文件打开数

		public String getJvmMaxHeapMemory() {
			return jvmMaxHeapMemory;
		}

		public void setJvmMaxHeapMemory(String jvmMaxHeapMemory) {
			this.jvmMaxHeapMemory = jvmMaxHeapMemory;
		}

		public String getJvmMaxNonHeapMemory() {
			return jvmMaxNonHeapMemory;
		}

		public void setJvmMaxNonHeapMemory(String jvmMaxNonHeapMemory) {
			this.jvmMaxNonHeapMemory = jvmMaxNonHeapMemory;
		}

		public String getJvmUsedHeapMemory() {
			return jvmUsedHeapMemory;
		}

		public void setJvmUsedHeapMemory(String jvmUsedHeapMemory) {
			this.jvmUsedHeapMemory = jvmUsedHeapMemory;
		}

		public String getJvmUsedNonHeapMemory() {
			return jvmUsedNonHeapMemory;
		}

		public void setJvmUsedNonHeapMemory(String jvmUsedNonHeapMemory) {
			this.jvmUsedNonHeapMemory = jvmUsedNonHeapMemory;
		}

		public String getJvmTotalStartedThreadCount() {
			return jvmTotalStartedThreadCount;
		}

		public void setJvmTotalStartedThreadCount(String jvmTotalStartedThreadCount) {
			this.jvmTotalStartedThreadCount = jvmTotalStartedThreadCount;
		}

		public String getSystemFreeMemory() {
			return systemFreeMemory;
		}

		public void setSystemFreeMemory(String systemFreeMemory) {
			this.systemFreeMemory = systemFreeMemory;
		}

		public String getJvmUsedTotalMemory() {
			return jvmUsedTotalMemory;
		}

		public void setJvmUsedTotalMemory(String jvmUsedTotalMemory) {
			this.jvmUsedTotalMemory = jvmUsedTotalMemory;
		}

		public String getNodeName() {
			return nodeName;
		}

		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}

		public String getProcess() {
			return process;
		}

		public void setProcess(String process) {
			this.process = process;
		}

		public String getCpus() {
			return cpus;
		}

		public void setCpus(String cpus) {
			this.cpus = cpus;
		}

		public String getJvmFreeMemory() {
			return jvmFreeMemory;
		}

		public void setJvmFreeMemory(String jvmFreeMemory) {
			this.jvmFreeMemory = jvmFreeMemory;
		}

		public String getJvmMaxMemory() {
			return jvmMaxMemory;
		}

		public void setJvmMaxMemory(String jvmMaxMemory) {
			this.jvmMaxMemory = jvmMaxMemory;
		}

		public String getPortWS() {
			return portWS;
		}

		public void setPortWS(String portWS) {
			this.portWS = portWS;
		}

		public String getPortRPC() {
			return portRPC;
		}

		public void setPortRPC(String portRPC) {
			this.portRPC = portRPC;
		}

		public String getPortActor() {
			return portActor;
		}

		public void setPortActor(String portActor) {
			this.portActor = portActor;
		}

		public String getSysOpenfiles() {
			return sysOpenfiles;
		}

		public void setSysOpenfiles(String sysOpenfiles) {
			this.sysOpenfiles = sysOpenfiles;
		}

		public String getProcessOpenfiles() {
			return processOpenfiles;
		}

		public void setProcessOpenfiles(String processOpenfiles) {
			this.processOpenfiles = processOpenfiles;
		}

		public String getPortWSOpenfiles() {
			return portWSOpenfiles;
		}

		public void setPortWSOpenfiles(String portWSOpenfiles) {
			this.portWSOpenfiles = portWSOpenfiles;
		}

		public String getPortRPCOpenfiles() {
			return portRPCOpenfiles;
		}

		public void setPortRPCOpenfiles(String portRPCOpenfiles) {
			this.portRPCOpenfiles = portRPCOpenfiles;
		}

		public String getPortActorOpenfiles() {
			return portActorOpenfiles;
		}

		public void setPortActorOpenfiles(String portActorOpenfiles) {
			this.portActorOpenfiles = portActorOpenfiles;
		}

	}

	public static class EnvInfo {

		// Java running env.
		//
		@JSONField(name = "java.io.tmpdir")
		private String javaIOTmpdir;
		@JSONField(name = "java.version")
		private String javaVersion;

		// Netty running env.
		//
		@JSONField(name = "io.netty.allocator.pageSize")
		private String nettyAllocatorPageSize;
		@JSONField(name = "io.netty.allocator.maxOrder")
		private String nettyAllocatorMaxOrder;
		@JSONField(name = "io.netty.allocator.tinyCacheSize")
		private String nettyAllocatorTinyCacheSize;
		@JSONField(name = "io.netty.allocator.smallCacheSize")
		private String nettyAllocatorSmallCacheSize;
		@JSONField(name = "io.netty.allocator.normalCacheSize")
		private String nettyAllocatorNormalCacheSize;
		@JSONField(name = "io.netty.allocator.numHeapArenas")
		private String nettyAllocatorNumHeapArenas;
		@JSONField(name = "io.netty.allocator.numDirectArenas")
		private String nettyAllocatorNumDirectArenas;
		@JSONField(name = "io.netty.allocator.cacheTrimInterval")
		private String nettyAllocatorCacheTrimInterval;
		@JSONField(name = "io.netty.allocator.maxCachedBufferCapacity")
		private String nettyAllocatorMaxCachedBufferCapacity;

		@JSONField(name = "io.netty.leakDetectionLevel")
		private String nettyLeakDetectionLevel;
		@JSONField(name = "io.netty.leakDetection.maxRecords")
		private String nettyLeakDetectionMaxRecords;
		@JSONField(name = "io.netty.maxDirectMemory")
		private String nettyMaxDirectMemory;
		@JSONField(name = "io.netty.noPreferDirect")
		private String nettyNoPreferDirect;
		@JSONField(name = "io.netty.noUnsafe")
		private String nettyNoUnsafe;
		@JSONField(name = "io.netty.defaultPromise.maxListenerStackDepth")
		private String nettyDefaultPromiseMaxListenerStackDepth;
		@JSONField(name = "io.netty.tmpdir")
		private String nettyTmpdir;
		@JSONField(name = "io.netty.bitMode")
		private String nettyBitMode;
		@JSONField(name = "io.netty.noJavassist")
		private String noJavassist;

		// Other running env.
		//
		@JSONField(name = "isAndroid")
		private String isAndroid;
		@JSONField(name = "isWindows")
		private String isWindows;
		@JSONField(name = "isRoot")
		private String isRoot;

		public String getJavaVersion() {
			return javaVersion;
		}

		public void setJavaVersion(String javaVersion) {
			this.javaVersion = javaVersion;
		}

		public String getJavaIOTmpdir() {
			return javaIOTmpdir;
		}

		public void setJavaIOTmpdir(String javaIOTmpdir) {
			this.javaIOTmpdir = javaIOTmpdir;
		}

		public String getIsRoot() {
			return isRoot;
		}

		public void setIsRoot(String isRoot) {
			this.isRoot = isRoot;
		}

		public String getIsAndroid() {
			return isAndroid;
		}

		public void setIsAndroid(String isAndroid) {
			this.isAndroid = isAndroid;
		}

		public String getIsWindows() {
			return isWindows;
		}

		public void setIsWindows(String isWindows) {
			this.isWindows = isWindows;
		}

		public String getNoJavassist() {
			return noJavassist;
		}

		public void setNoJavassist(String noJavassist) {
			this.noJavassist = noJavassist;
		}

		public String getNettyBitMode() {
			return nettyBitMode;
		}

		public void setNettyBitMode(String nettyBitMode) {
			this.nettyBitMode = nettyBitMode;
		}

		public String getNettyTmpdir() {
			return nettyTmpdir;
		}

		public void setNettyTmpdir(String nettyTmpdir) {
			this.nettyTmpdir = nettyTmpdir;
		}

		public String getNettyDefaultPromiseMaxListenerStackDepth() {
			return nettyDefaultPromiseMaxListenerStackDepth;
		}

		public void setNettyDefaultPromiseMaxListenerStackDepth(String nettyDefaultPromiseMaxListenerStackDepth) {
			this.nettyDefaultPromiseMaxListenerStackDepth = nettyDefaultPromiseMaxListenerStackDepth;
		}

		public String getNettyNoUnsafe() {
			return nettyNoUnsafe;
		}

		public void setNettyNoUnsafe(String nettyNoUnsafe) {
			this.nettyNoUnsafe = nettyNoUnsafe;
		}

		public String getNettyNoPreferDirect() {
			return nettyNoPreferDirect;
		}

		public void setNettyNoPreferDirect(String nettyNoPreferDirect) {
			this.nettyNoPreferDirect = nettyNoPreferDirect;
		}

		public String getNettyMaxDirectMemory() {
			return nettyMaxDirectMemory;
		}

		public void setNettyMaxDirectMemory(String nettyMaxDirectMemory) {
			this.nettyMaxDirectMemory = nettyMaxDirectMemory;
		}

		public String getNettyLeakDetectionMaxRecords() {
			return nettyLeakDetectionMaxRecords;
		}

		public void setNettyLeakDetectionMaxRecords(String nettyLeakDetectionMaxRecords) {
			this.nettyLeakDetectionMaxRecords = nettyLeakDetectionMaxRecords;
		}

		public String getNettyLeakDetectionLevel() {
			return nettyLeakDetectionLevel;
		}

		public void setNettyLeakDetectionLevel(String nettyLeakDetectionLevel) {
			this.nettyLeakDetectionLevel = nettyLeakDetectionLevel;
		}

		public String getNettyAllocatorPageSize() {
			return nettyAllocatorPageSize;
		}

		public void setNettyAllocatorPageSize(String nettyAllocatorPageSize) {
			this.nettyAllocatorPageSize = nettyAllocatorPageSize;
		}

		public String getNettyAllocatorMaxOrder() {
			return nettyAllocatorMaxOrder;
		}

		public void setNettyAllocatorMaxOrder(String nettyAllocatorMaxOrder) {
			this.nettyAllocatorMaxOrder = nettyAllocatorMaxOrder;
		}

		public String getNettyAllocatorTinyCacheSize() {
			return nettyAllocatorTinyCacheSize;
		}

		public void setNettyAllocatorTinyCacheSize(String nettyAllocatorTinyCacheSize) {
			this.nettyAllocatorTinyCacheSize = nettyAllocatorTinyCacheSize;
		}

		public String getNettyAllocatorSmallCacheSize() {
			return nettyAllocatorSmallCacheSize;
		}

		public void setNettyAllocatorSmallCacheSize(String nettyAllocatorSmallCacheSize) {
			this.nettyAllocatorSmallCacheSize = nettyAllocatorSmallCacheSize;
		}

		public String getNettyAllocatorNormalCacheSize() {
			return nettyAllocatorNormalCacheSize;
		}

		public void setNettyAllocatorNormalCacheSize(String nettyAllocatorNormalCacheSize) {
			this.nettyAllocatorNormalCacheSize = nettyAllocatorNormalCacheSize;
		}

		public String getNettyAllocatorNumHeapArenas() {
			return nettyAllocatorNumHeapArenas;
		}

		public void setNettyAllocatorNumHeapArenas(String nettyAllocatorNumHeapArenas) {
			this.nettyAllocatorNumHeapArenas = nettyAllocatorNumHeapArenas;
		}

		public String getNettyAllocatorNumDirectArenas() {
			return nettyAllocatorNumDirectArenas;
		}

		public void setNettyAllocatorNumDirectArenas(String nettyAllocatorNumDirectArenas) {
			this.nettyAllocatorNumDirectArenas = nettyAllocatorNumDirectArenas;
		}

		public String getNettyAllocatorCacheTrimInterval() {
			return nettyAllocatorCacheTrimInterval;
		}

		public void setNettyAllocatorCacheTrimInterval(String nettyAllocatorCacheTrimInterval) {
			this.nettyAllocatorCacheTrimInterval = nettyAllocatorCacheTrimInterval;
		}

		public String getNettyAllocatorMaxCachedBufferCapacity() {
			return nettyAllocatorMaxCachedBufferCapacity;
		}

		public void setNettyAllocatorMaxCachedBufferCapacity(String nettyAllocatorMaxCachedBufferCapacity) {
			this.nettyAllocatorMaxCachedBufferCapacity = nettyAllocatorMaxCachedBufferCapacity;
		}

	}

}
