package io.transport.common.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

/**
 * Redis cluster Bean factory
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @time 2016年11月24日
 * @since
 */
@Component
@ConfigurationProperties(prefix = "redis")
public class JedisClusterFactoryBean implements FactoryBean<JedisCluster>, InitializingBean {
	final private static Logger logger = LoggerFactory.getLogger(JedisClusterFactoryBean.class);
	final private static Pattern p = Pattern.compile("^.+[:]\\d{1,9}\\s*$");

	/**
	 * Redis connection base configuration.
	 */
	private List<String> nodes;
	@Value("${redis.connect-timeout:10000}")
	private Integer connTimeout;
	@Value("${redis.so-timeout:10000}")
	private Integer soTimeout;
	@Value("${redis.passwd:}")
	private String passwd;
	@Value("${redis.max-attempts:20}")
	private Integer maxAttempts;

	/**
	 * Redis connection pool configuration.
	 */
	private GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
	@Value("${redis.max-wait-millis:10000}")
	private Integer maxWaitMillis;
	@Value("${redis.min-idle:10}")
	private Integer minIdle;
	@Value("${redis.max-idle:100}")
	private Integer maxIdle;
	@Value("${redis.max-total:60000}")
	private Integer maxTotal;

	private JedisCluster jedisCluster;

	@PostConstruct
	public void init() {
		genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setMaxWaitMillis(maxWaitMillis);
		genericObjectPoolConfig.setMinIdle(minIdle);
		genericObjectPoolConfig.setMaxIdle(maxIdle);
		genericObjectPoolConfig.setMaxTotal(maxTotal);
	}

	@Override
	public JedisCluster getObject() throws Exception {
		return jedisCluster;
	}

	@Override
	public Class<? extends JedisCluster> getObjectType() {
		return (this.jedisCluster != null ? this.jedisCluster.getClass() : JedisCluster.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Set<HostAndPort> haps = this.parseHostAndPort();
		if (StringUtils.isEmpty(passwd))
			this.jedisCluster = new JedisCluster(haps, connTimeout, soTimeout, maxAttempts, genericObjectPoolConfig);
		else
			this.jedisCluster = new JedisCluster(haps, connTimeout, soTimeout, maxAttempts, passwd, genericObjectPoolConfig);
	}

	public List<String> getNodes() {
		return nodes;
	}

	public void setNodes(List<String> nodes) {
		this.nodes = nodes;
	}

	private Set<HostAndPort> parseHostAndPort() throws Exception {
		try {
			Set<HostAndPort> haps = new HashSet<HostAndPort>();
			for (String node : this.getNodes()) {
				boolean isIpPort = p.matcher(node).matches();
				if (!isIpPort)
					throw new IllegalArgumentException("ip 或 port 不合法");

				String[] ipAndPort = node.split(":");
				HostAndPort hap = new HostAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
				if (logger.isInfoEnabled())
					logger.info("Redis cluster(node): {}", hap);

				haps.add(hap);
			}
			return haps;

		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception e) {
			throw new Exception("解析 jedis 配置文件失败", e);
		}
	}

}