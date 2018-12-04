package io.transport.cluster.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.net.HostAndPort;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import akka.AkkaException;
import io.transport.core.config.Configuration;

/**
 * AKKA服务配置类
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年3月27日
 * @since
 */
@Component
public class AkkaConfiguration implements InitializingBean {
	final private static Logger logger = LoggerFactory.getLogger(AkkaConfiguration.class);

	@Autowired
	private Remote remote;
	@Autowired
	private Cluster cluster;
	@Autowired
	private Configuration configuration;
	@Value("${akka.system-name:defaultSystem}")
	private String actorSystemName;

	private Config config;
	private String userActorBasePath;

	public Remote getRemote() {
		return remote;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public String getActorSystemName() {
		return actorSystemName;
	}

	public Config getConfig() {
		return config;
	}

	public String getUserActorBasePath() {
		return userActorBasePath;
	}

	/**
	 * 获取本地节点主机，若配置为空则获取操作系统默认网卡地址
	 * 
	 * @return
	 */
	public String getHostname() {
		String hostname = getRemote().getHostname();
		if (!StringUtils.isEmpty(hostname))
			return hostname;
		else {
			try {
				hostname = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				throw new AkkaException(e.getMessage(), e);
			}
			logger.warn("The default address `{}` has been used without configuring the actor host address.", hostname);
			// throw new AkkaException(errmsg);
		}
		return hostname;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 1.1 Initial configuration.
		this.config = ConfigFactory.load();

		// 1.2 User actor basic path.
		this.userActorBasePath = new ActorPath(getActorSystemName(), getHostname(), getRemote().getPort()).asString();

		// 1.3 Using external configuration.
		// Actor port.
		String pKey = "akka.remote.netty.tcp.port";
		this.config = config.withValue(pKey, ConfigValueFactory.fromAnyRef(getRemote().getPort(), null));

		// Actor host name.
		String hKey = "akka.remote.netty.tcp.hostname";
		if (StringUtils.isEmpty(this.config.getString(hKey)))
			this.config = config.withValue(hKey, ConfigValueFactory.fromAnyRef(getHostname(), null));

		// Actor cluster seed-nodes.
		String seedNKey = "akka.cluster.seed-nodes";
		List<String> seedNodes = this.config.getStringList(seedNKey);
		if (seedNodes == null || seedNodes.isEmpty()) {
			// For each seed nodes.
			seedNodes = new ArrayList<>();
			for (String node : getCluster().getSeedNodes()) {
				HostAndPort hap = HostAndPort.fromString(node);
				seedNodes.add(new ActorPath(getActorSystemName(), hap.getHostText(), hap.getPort()).asBaseString());
			}
			this.config = config.withValue(seedNKey, ConfigValueFactory.fromAnyRef(seedNodes, null));
		}

		if (logger.isInfoEnabled())
			logger.info("Usage: Actor systemName={}, hostname={}, port={}, seed-nodes={}", getActorSystemName(),
					config.getString(hKey), config.getString(pKey), config.getStringList(seedNKey));
	}

	@Component
	public static class Remote {
		@Value("${akka.remote.hostname}")
		private String hostname;
		@Value("${akka.remote.port:2552}")
		private int port;

		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	@ConfigurationProperties(prefix = "akka.cluster")
	public static class Cluster {
		private List<String> seedNodes = new ArrayList<>(); // 种子节点列表，格式：以逗号分隔、ip:port

		public List<String> getSeedNodes() {
			return this.seedNodes;
		}

		public void setSeedNodes(List<String> seedNodes) {
			this.seedNodes = seedNodes;
		}

	}

}
