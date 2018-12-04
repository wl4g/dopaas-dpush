package io.transport.data.config;

import java.util.Properties;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Spring-data-hadoop-hbase configuration.<br/>
 * 
 * Note: use `@ConfigurationProperties` annotation to generate corresponding
 * getter setter method.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年11月16日
 * @since
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@ConfigurationProperties(prefix = HbaseConfiguration.DATA_CONF_P)
public class HbaseConfiguration {
	final public static String DATA_CONF_P = "data";

	private Properties properties;

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}

	public String get(String key) {
		return this.getProperties().getProperty(key);
	}

}
