package io.transport.core.config;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.transport.core.protocol.handler.ChildHandlerInitializer;
import io.transport.core.protocol.handler.ws.WSChildHandlerInitializer;

/**
 * Netty服务端配置类
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年12月25日
 * @since
 */
@Component
public class Configuration {

	/**
	 * transport基础前缀key
	 */
	@Value("${core.ctl-pkey:transport_}")
	private String ctlPKey = "transport_";
	/**
	 * appId保存groupIds的key
	 */
	@Value("${core.ctl-cluster-nodes-pkey:cluster_nodes_}")
	private String ctlClusterNodesPKey = "cluster_nodes_";
	/**
	 * appId保存groupIds的key
	 */
	@Value("${core.ctl-appId-groups-pkey:appId_groups_}")
	private String ctlAppIdGroupsPKey = "appId_groups_";
	/**
	 * groupId保存deviceIds的key
	 */
	@Value("${core.ctl-groupId-devices-pkey:groupId_devices_}")
	private String ctlGroupIdDevicesPKey = "groupId_devices_";
	/**
	 * deviceId保存client info的key
	 */
	@Value("${core.ctl-deviceId-clientInfo-pkey:deviceId_clientInfo_}")
	private String ctlDeviceIdClientInfoPKey = "deviceId_clientInfo_";
	/**
	 * appId默认连接数配置key
	 */
	@Value("${core.ctl-appId-device-connects-pkey:appId_device_connects_}")
	private String ctlAppIdDeviceConnectsPKey = "appId_device_connects_";
	/**
	 * 设备deviceId对应actor地址key前缀
	 */
	@Value("${ctl-deviceId-actor-pkey:actor_device_}")
	private String ctlDeviceIdActorPkey = "actor_device_";
	/**
	 * 临时缓存存储msgId-rowKey的NoSql的key
	 */
	@Value("${ctl-msgId-rowKey-pkey:msgId_rowkey_}")
	private String ctlMsgIdRowKeyPkey = "msgId_rowkey_";
	/**
	 * 临时缓存存储msgId-rowKey的NoSql的过期时间
	 */
	@Value("${ctl-msgId-rowKey-expire:600}")
	private Integer ctlMsgIdRowKeyExpire = 600;
	/**
	 * Restful API接口token认证key前缀
	 */
	@Value("${ctl-rest-auth-pkey:rest_auth_}")
	private String ctlRestAuthPkey = "rest_auth_";

	/**
	 * Push service deployment mode, if ROUTING mode, the client login success
	 * will receive the cluster node list information returned by the server,
	 * otherwise if NAT mode is configured, the server will not return to the
	 * cluster node list.
	 */
	@Value("${deployment-mode:ROUTING}")
	private DeploymentType deploymentType = DeploymentType.ROUTING;

	@Value("${core.hostname}")
	private String hostname;
	@Autowired
	private RpcConfig rpcConfig;
	@Autowired
	private WSConfig wsConfig;

	public String getCtlPKey() {
		return ctlPKey;
	}

	public String getCtlClusterNodesPKey() {
		return getCtlPKey() + ctlClusterNodesPKey;
	}

	public String getCtlAppIdGroupsPKey() {
		return getCtlPKey() + ctlAppIdGroupsPKey;
	}

	public String getCtlGroupIdDevicesPKey() {
		return getCtlPKey() + this.ctlGroupIdDevicesPKey;
	}

	public String getCtlDeviceIdClientInfoPKey() {
		return getCtlPKey() + this.ctlDeviceIdClientInfoPKey;
	}

	public String getCtlAppIdDeviceConnectsPKey() {
		return getCtlPKey() + ctlAppIdDeviceConnectsPKey;
	}

	public String getCtlDeviceIdActorPkey() {
		return getCtlPKey() + ctlDeviceIdActorPkey;
	}

	public String getCtlMsgIdRowKeyPkey() {
		return this.getCtlPKey() + ctlMsgIdRowKeyPkey;
	}

	public Integer getCtlMsgIdRowKeyExpire() {
		return ctlMsgIdRowKeyExpire;
	}

	public String getCtlRestAuthPkey() {
		return this.getCtlPKey() + ctlRestAuthPkey;
	}

	public DeploymentType getDeploymentType() {
		return deploymentType;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String inetHostname) {
		this.hostname = inetHostname;
	}

	//
	// RPC/WebSocket配置
	//

	public RpcConfig getRpcConfig() {
		return rpcConfig;
	}

	public void setRpcConfig(RpcConfig rpcConfig) {
		this.rpcConfig = rpcConfig;
	}

	public WSConfig getWsConfig() {
		return wsConfig;
	}

	public void setWsConfig(WSConfig wsConfig) {
		this.wsConfig = wsConfig;
	}

	/**
	 * 后端第三方服务、或Android等相关配置
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2017年12月25日
	 * @since
	 */
	@Component
	public static class RpcConfig implements ServerConfig {
		final public static String RPC_NAME = "RpcServer";

		@Value("${core.rpc.startup:false}")
		volatile private boolean startup;
		@Value("${core.rpc.name:" + RPC_NAME + "}")
		private String name = RPC_NAME;
		@Value("${core.rpc.port:9090}")
		private int port = 9090;
		@Value("${core.rpc.backlog:1024}")
		private int backlog = 1024;
		/**
		 * Default connection number limit of the same appId.
		 */
		@Value("${core.rpc.default-appId-connects:100}")
		private Integer defaultAppIdConnects = 100;
		/**
		 * Current node maximum connection number limit.
		 */
		@Value("${core.rpc.accpet-maxconnects:1000}")
		private Integer accpetMaxconnects = 1000;
		@Resource(type = ChildHandlerInitializer.class)
		private ChannelInitializer<SocketChannel> handlerInitializer;

		// Other configuration.
		@Value("${core.rpc.read-idle-seconds:120}")
		private Integer readIdleSeconds;
		@Value("${core.rpc.write-idle-seconds:120}")
		private Integer writeIdleSeconds;
		@Value("${core.rpc.all-idle-seconds:120}")
		private Integer allIdleSeconds;
		@Value("${core.rpc.logging.enable:false}")
		private Boolean loggingEnable;
		@Value("${core.rpc.logging.level:INFO}")
		private String loggingLevel;
		@Value("${core.rpc.max-content-len:32768}")
		private int maxContentLength; // Prevent DDOS attack.

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public boolean isStartup() {
			return startup;
		}

		public void setStartup(boolean startup) {
			this.startup = startup;
		}

		@Override
		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		@Override
		public int getBacklog() {
			return backlog;
		}

		public void setBacklog(int backlog) {
			this.backlog = backlog;
		}

		public Integer getDefaultAppIdConnects() {
			return defaultAppIdConnects;
		}

		public void setDefaultAppIdConnects(Integer defaultAppIdConnects) {
			this.defaultAppIdConnects = defaultAppIdConnects;
		}

		public Integer getAccpetMaxconnects() {
			return accpetMaxconnects;
		}

		public void setAccpetMaxconnects(Integer accpetMaxconnects) {
			this.accpetMaxconnects = accpetMaxconnects;
		}

		@Override
		public ChannelInitializer<SocketChannel> getHandlerInitializer() {
			return handlerInitializer;
		}

		public void setHandlerInitializer(ChannelInitializer<SocketChannel> handlerInitializer) {
			this.handlerInitializer = handlerInitializer;
		}

		public Integer getReadIdleSeconds() {
			return readIdleSeconds;
		}

		public void setReadIdleSeconds(Integer readIdleSeconds) {
			this.readIdleSeconds = readIdleSeconds;
		}

		public Integer getWriteIdleSeconds() {
			return writeIdleSeconds;
		}

		public void setWriteIdleSeconds(Integer writeIdleSeconds) {
			this.writeIdleSeconds = writeIdleSeconds;
		}

		public Integer getAllIdleSeconds() {
			return allIdleSeconds;
		}

		public void setAllIdleSeconds(Integer allIdleSeconds) {
			this.allIdleSeconds = allIdleSeconds;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Boolean getLoggingEnable() {
			return loggingEnable;
		}

		public void setLoggingEnable(Boolean loggingEnable) {
			this.loggingEnable = loggingEnable;
		}

		public String getLoggingLevel() {
			return loggingLevel;
		}

		public void setLoggingLevel(String loggingLevel) {
			this.loggingLevel = loggingLevel;
		}

		public int getMaxContentLength() {
			return maxContentLength;
		}

		public void setMaxContentLength(int maxContentLength) {
			this.maxContentLength = maxContentLength;
		}

	}

	/**
	 * 前端WebSocket连接相关配置
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2017年12月25日
	 * @since
	 */
	@Component
	public static class WSConfig implements ServerConfig {
		final public static String WS_NAME = "WebSocket";

		@Value("${core.websocket.name:" + WS_NAME + "}")
		private String name = WS_NAME;
		@Value("${core.websocket.startup:false}")
		volatile private boolean startup;
		@Value("${core.websocket.port:9091}")
		private int port = 9091;
		@Value("${core.websocket.backlog:1024}")
		private int backlog = 1024;
		/**
		 * Default connection number limit of the same appId.
		 */
		@Value("${core.rpc.default-appId-connects:50}")
		private Integer defaultAppIdConnects = 50;
		/**
		 * Current node maximum connection number limit.
		 */
		@Value("${core.rpc.accpet-maxconnects:1000}")
		private Integer accpetMaxconnects = 1000;

		@Resource(type = WSChildHandlerInitializer.class)
		private ChannelInitializer<SocketChannel> handlerInitializer;

		// Other configuration.
		@Value("${core.websocket.read-idle-seconds:120}")
		private Integer readIdleSeconds;
		@Value("${core.websocket.write-idle-seconds:120}")
		private Integer writeIdleSeconds;
		@Value("${core.websocket.all-idle-seconds:120}")
		private Integer allIdleSeconds;
		@Value("${core.websocket.logging.enable:false}")
		private Boolean loggingEnable;
		@Value("${core.websocket.logging.level:INFO}")
		private String loggingLevel;
		@Value("${core.websocket.path:/ws}")
		private String websocketPath;
		@Value("${core.websocket.allow-extensions:false}")
		private Boolean allowExtensions;
		@Value("${core.websocket.http-aggregator.max-content-len:65536}")
		private Integer httpAggregatorMaxContentLen;
		@Value("${core.websocket.ssl.enable:false}")
		private Boolean sslEnable;
		@Value("${core.websocket.ssl.keycert-chainfile:}")
		private String keyCertChainFile;
		@Value("${core.websocket.ssl.keyfile:}")
		private String keyFile;

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public boolean isStartup() {
			return startup;
		}

		public void setStartup(boolean startup) {
			this.startup = startup;
		}

		@Override
		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		@Override
		public int getBacklog() {
			return backlog;
		}

		public void setBacklog(int backlog) {
			this.backlog = backlog;
		}

		public Integer getDefaultAppIdConnects() {
			return defaultAppIdConnects;
		}

		public void setDefaultAppIdConnects(Integer defaultAppIdConnects) {
			this.defaultAppIdConnects = defaultAppIdConnects;
		}

		public Integer getAccpetMaxconnects() {
			return accpetMaxconnects;
		}

		public void setAccpetMaxconnects(Integer accpetMaxconnects) {
			this.accpetMaxconnects = accpetMaxconnects;
		}

		@Override
		public ChannelInitializer<SocketChannel> getHandlerInitializer() {
			return handlerInitializer;
		}

		public void setHandlerInitializer(ChannelInitializer<SocketChannel> handlerInitializer) {
			this.handlerInitializer = handlerInitializer;
		}

		public Integer getReadIdleSeconds() {
			return readIdleSeconds;
		}

		public void setReadIdleSeconds(Integer readIdleSeconds) {
			this.readIdleSeconds = readIdleSeconds;
		}

		public Integer getWriteIdleSeconds() {
			return writeIdleSeconds;
		}

		public void setWriteIdleSeconds(Integer writeIdleSeconds) {
			this.writeIdleSeconds = writeIdleSeconds;
		}

		public Integer getAllIdleSeconds() {
			return allIdleSeconds;
		}

		public void setAllIdleSeconds(Integer allIdleSeconds) {
			this.allIdleSeconds = allIdleSeconds;
		}

		public String getWebsocketPath() {
			return websocketPath;
		}

		public void setWebsocketPath(String websocketPath) {
			this.websocketPath = websocketPath;
		}

		public Boolean getAllowExtensions() {
			return allowExtensions;
		}

		public void setAllowExtensions(Boolean allowExtensions) {
			this.allowExtensions = allowExtensions;
		}

		public Integer getHttpAggregatorMaxContentLen() {
			return httpAggregatorMaxContentLen;
		}

		public void setHttpAggregatorMaxContentLen(Integer httpAggregatorMaxContentLen) {
			this.httpAggregatorMaxContentLen = httpAggregatorMaxContentLen;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Boolean getSslEnable() {
			return sslEnable;
		}

		public void setSslEnable(Boolean sslEnable) {
			this.sslEnable = sslEnable;
		}

		public String getKeyCertChainFile() {
			return keyCertChainFile;
		}

		public void setKeyCertChainFile(String keyCertChainFile) {
			this.keyCertChainFile = keyCertChainFile;
		}

		public String getKeyFile() {
			return keyFile;
		}

		public void setKeyFile(String keyFile) {
			this.keyFile = keyFile;
		}

		public Boolean getLoggingEnable() {
			return loggingEnable;
		}

		public void setLoggingEnable(Boolean loggingEnable) {
			this.loggingEnable = loggingEnable;
		}

		public String getLoggingLevel() {
			return loggingLevel;
		}

		public void setLoggingLevel(String loggingLevel) {
			this.loggingLevel = loggingLevel;
		}

	}

	/**
	 * Netty Server config相关配置
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2017年12月25日
	 * @since
	 */
	public static interface ServerConfig {

		String getName();

		boolean isStartup();

		int getPort();

		int getBacklog();

		ChannelInitializer<SocketChannel> getHandlerInitializer();
	}

	/**
	 * Transporter push service deployment mode, if ROUTING mode, the client
	 * login success will receive the cluster node list information returned by
	 * the server, otherwise if NAT mode is configured, the server will not
	 * return to the cluster node list.
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年4月24日
	 * @since
	 */
	public static enum DeploymentType {

		/**
		 * ROUTING mode, the client login success will receive the cluster node
		 * list information returned by the server.
		 */
		ROUTING,
		/**
		 * NAT mode is configured, the server will not return to the cluster
		 * node list.
		 */
		NAT;

	}

}
