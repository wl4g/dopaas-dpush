package io.transport.sdk;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import io.transport.sdk.cluster.AbstractRoutingLoadBalancer;
import io.transport.sdk.cluster.HashRoutingLoadBalancer;
import io.transport.sdk.exception.TransportException;
import io.transport.sdk.logger.DefaultLogger;
import io.transport.sdk.logger.Level;
import io.transport.sdk.logger.Logger;
import io.transport.sdk.protocol.handler.ReceiveTextHandler;
import io.transport.sdk.store.Store;
import io.transport.sdk.utils.PlatformConstants;

/**
 * Transport 配置器
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月24日
 * @since
 */
final public class Configuration {
	private int connecTimeout; // 连接超时时
	private int reconnectDelay; // 重连间隔(s)
	private int loginTimeout; // 登录超时时
	private int reconnectRetry; // 集群节点重连次数
	private int nodeChooseInterval; // 集群预选最近有效节点间隔(ms)
	@Deprecated
	private int soTimeout; // SO读取超时
	private int readIdleSeconds; // 心跳读中断校验时间
	private int writeIdleSeconds; // 心跳写中断校验时间
	private int allIdleSeconds; // 心跳读写中断校验时间

	private boolean loggingEnable;
	private Level level;
	private Logger loggerImpl;

	private Store storeImpl; // 消息持久化实现
	private Class<? extends ReceiveTextHandler> handler;

	// 集群节点负载均衡器
	private AbstractRoutingLoadBalancer routingBalancer;
	private List<HostAndPort> hostAndPorts = new ArrayList<HostAndPort>();

	private String appId;
	private String appSecret;
	private String deviceId; // 运行设备ID(用于P2P推送,默认:网卡序列号+应用包名)
	private String groupId; // 组ID，用于组播消息

	/**
	 * Initialize the construction of configuration.
	 * 
	 * @param appId
	 *            Apply for application ID.
	 * @param appSecret
	 *            secret key of the application.
	 * @param groupId
	 *            Current device groupId.
	 * @param handler
	 *            Callback handler.
	 * @param storeImpl
	 *            Persistent repository implementation.
	 */
	public Configuration(String appId, String appSecret, String groupId, Class<? extends ReceiveTextHandler> handler,
			Store storeImpl) {
		this(appId, appSecret, groupId, handler, storeImpl, null);
	}

	/**
	 * Initialize the construction of configuration.
	 * 
	 * @param appId
	 *            Apply for application ID.
	 * @param appSecret
	 *            secret key of the application.
	 * @param groupId
	 *            Current device groupId.
	 * @param handler
	 *            Callback handler.
	 * @param storeImpl
	 *            Persistent repository implementation.
	 * @param activity
	 *            android.app.Activity
	 */
	public Configuration(String appId, String appSecret, String groupId, Class<? extends ReceiveTextHandler> handler,
			Store storeImpl, Object activity) {
		super();

		// Initial platform constants
		PlatformConstants.initial(activity);

		this.setConnecTimeout(30);
		this.setReconnectDelay(2);
		this.setLoginTimeout(10);
		this.setReconnectRetry(3);
		this.setNodeChooseInterval(600);
		// this.setSoTimeout(10000);
		this.setReadIdleSeconds(600);
		this.setWriteIdleSeconds(600);
		this.setAllIdleSeconds(900);

		this.setLevel(Level.INFO);
		this.setLoggerImpl(new DefaultLogger(this));
		this.setLoggingEnable(true);

		this.setAppId(appId);
		this.setAppSecret(appSecret);
		this.setDeviceId(PlatformConstants.appSerial);
		this.setGroupId(groupId);
		this.setHandler(handler);
		this.setStoreImpl(storeImpl);
		this.setRoutingBalancer(HashRoutingLoadBalancer.class);
	}

	public AbstractRoutingLoadBalancer getRoutingBalancer() {
		return this.routingBalancer;
	}

	public void setRoutingBalancer(Class<? extends AbstractRoutingLoadBalancer> routingLoadBalancer) {
		if (routingLoadBalancer != null) {
			try {
				Constructor<? extends AbstractRoutingLoadBalancer> constor = routingLoadBalancer
						.getConstructor(Configuration.class);
				this.routingBalancer = constor.newInstance(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Store getStoreImpl() {
		return storeImpl;
	}

	public void setStoreImpl(Store storeImpl) {
		this.storeImpl = storeImpl;
	}

	public Logger getLoggerImpl() {
		return loggerImpl;
	}

	public void setLoggerImpl(Logger loggerImpl) {
		if (loggerImpl != null) {
			this.loggerImpl = loggerImpl;
		}
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		if (level != null) {
			this.level = level;
		}
	}

	public Class<? extends ReceiveTextHandler> getHandler() {
		return handler;
	}

	public void setHandler(Class<? extends ReceiveTextHandler> handler) {
		if (handler != null) {
			this.handler = handler;
		}
	}

	public int getLoginTimeout() {
		return loginTimeout;
	}

	public void setLoginTimeout(int loginTimeout) {
		this.loginTimeout = loginTimeout;
	}

	public int getConnecTimeout() {
		return connecTimeout;
	}

	public void setConnecTimeout(int connecTimeout) {
		this.connecTimeout = connecTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public int getReadIdleSeconds() {
		return readIdleSeconds;
	}

	public void setReadIdleSeconds(int readIdleSeconds) {
		this.readIdleSeconds = readIdleSeconds;
	}

	public int getWriteIdleSeconds() {
		return writeIdleSeconds;
	}

	public void setWriteIdleSeconds(int writeIdleSeconds) {
		this.writeIdleSeconds = writeIdleSeconds;
	}

	public int getAllIdleSeconds() {
		return allIdleSeconds;
	}

	public void setAllIdleSeconds(int allIdleSeconds) {
		this.allIdleSeconds = allIdleSeconds;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public boolean isLoggingEnable() {
		return loggingEnable;
	}

	public void setLoggingEnable(boolean loggingEnable) {
		this.loggingEnable = loggingEnable;
	}

	public int getReconnectDelay() {
		return reconnectDelay;
	}

	public void setReconnectDelay(int reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}

	public List<HostAndPort> getHostAndPorts() {
		return this.hostAndPorts;
	}

	public void setHostAndPorts(List<HostAndPort> hostAndPorts) {
		if (hostAndPorts == null || !(hostAndPorts instanceof ArrayList))
			throw new TransportException(
					"`hostAndPorts` is null or hostAndPorts does not belong to an instance of `java.util.ArrayList`.");

		this.hostAndPorts = hostAndPorts;
	}

	public void setHostAndPorts(String hostAndPorts) {
		if (hostAndPorts == null)
			throw new TransportException("`host` is null.");

		for (String hap : hostAndPorts.split(","))
			this.hostAndPorts.add(HostAndPort.of(hap));
	}

	public int getNodeChooseInterval() {
		return nodeChooseInterval;
	}

	public void setNodeChooseInterval(int preNodeLatelyInterval) {
		this.nodeChooseInterval = preNodeLatelyInterval;
	}

	public int getReconnectRetry() {
		return reconnectRetry;
	}

	public void setReconnectRetry(int reconnectRetry) {
		this.reconnectRetry = reconnectRetry;
	}

	public void validation() {
		if (this.getAppId() == null || this.getAppSecret() == null || this.getGroupId() == null
				|| this.getHandler() == null || this.getHostAndPorts() == null || this.getHostAndPorts().isEmpty())
			throw new TransportException("host/port/appId/appSecret/groupId/handler参数为空或非法.");
	}

	@Override
	public String toString() {
		return "Configuration [logger=" + loggerImpl + ", hostAndPorts=" + getHostAndPorts() + ", reconnectDelay="
				+ reconnectDelay + ", connecTimeout=" + connecTimeout + ", loginTimeout=" + loginTimeout
				+ ", soTimeout=" + soTimeout + ", readIdleSeconds=" + readIdleSeconds + ", writeIdleSeconds="
				+ writeIdleSeconds + ", allIdleSeconds=" + allIdleSeconds + ", loggingEnable=" + loggingEnable
				+ ", logLevel=" + level + ", handler=" + handler + ", appId=" + appId + ", appSecret=" + appSecret
				+ ", groupId=" + groupId + "]";
	}

	/**
	 * 集群节点主机端口配置组
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年2月6日
	 * @since
	 */
	public static class HostAndPort {
		private String host;
		private int port;

		public HostAndPort() {
			super();
		}

		public HostAndPort(String host, int port) {
			super();
			this.host = host;
			this.port = port;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		@Override
		public String toString() {
			return getHost() + ":" + getPort();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			result = prime * result + port;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HostAndPort other = (HostAndPort) obj;
			if (host == null) {
				if (other.host != null)
					return false;
			} else if (!host.equals(other.host))
				return false;
			if (port != other.port)
				return false;
			return true;
		}

		public static HostAndPort of(String hapTxt) {
			try {
				if (hapTxt != null && hapTxt.contains(":")) {
					String[] arr = hapTxt.split("\\:");
					return new HostAndPort(arr[0], Integer.parseInt(arr[1]));
				}
			} catch (Exception e) {
				throw new TransportException("集群节点信息格式非法.", e);
			}
			return null;
		}

	}

}