package io.transport.core;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import io.transport.common.SpringContextHolder;
import io.transport.core.config.Configuration;
import io.transport.core.config.Configuration.ServerConfig;

/**
 * Netty服务监听器
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年12月19日
 * @since
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NettyChannelServerListener implements ApplicationRunner {
	final private static Logger logger = LoggerFactory.getLogger(NettyChannelServerListener.class);
	volatile private boolean startupFlag = false;
	@Autowired
	private Configuration config;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!this.startupFlag) {
			this.startupFlag = true;

			// 1.1 获取server配置bean列表
			Map<String, ServerConfig> confs = SpringContextHolder.getBeansOfType(ServerConfig.class);
			if (confs != null) {
				for (ServerConfig conf : confs.values())
					// 1.1.1 Starting.
					this.startup(conf);
			}

			// 1.2 Set Sfl4j logger
			InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
		}
	}

	/**
	 * 启动监听.
	 * 
	 * @param conf
	 */
	private void startup(ServerConfig conf) {
		try {
			// 控制监听服务启动
			if (conf.isStartup()) {
				new NettyChannelServer(conf.getName(), config.getHostname(), conf.getPort(), conf.getBacklog(),
						conf.getHandlerInitializer()).doStart();
			} else
				logger.warn("Non startup netty server({}:{}).", conf.getPort(), conf.getName());
		} catch (Exception e) {
			logger.error("启动Netty服务列表失败. (" + conf.getPort() + ")", e);
		}
	}

}
