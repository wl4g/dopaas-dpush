package io.transport.core.protocol.handler;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.transport.core.config.Configuration;
import io.transport.core.config.Configuration.RpcConfig;
import io.transport.core.protocol.codec.TransportMessageDecoder;
import io.transport.core.protocol.codec.TransportMessageEncoder;

/**
 * 客户端SDK-Server Handler初始器
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月12日
 * @since
 */
@Component
@Lazy(false)
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ChildHandlerInitializer extends ChannelInitializer<SocketChannel> {
	final private static Logger logger = LoggerFactory.getLogger(ChildHandlerInitializer.class);
	@Resource
	private Configuration config;
	@Resource
	private BeanFactory beanFactory;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		RpcConfig conf = this.config.getRpcConfig();
		if (logger.isDebugEnabled())
			logger.debug("Initital channel handler...");

		// pipeline管理channel中的Handler，在channel队列中添加一个handler来处理业务
		ChannelPipeline p = ch.pipeline();
		if (conf.getLoggingEnable()) {
			p.addLast(new LoggingHandler(LogLevel.valueOf(conf.getLoggingLevel())));
			if (logger.isInfoEnabled())
				logger.info("Netty internal log has been used. (Rpc)level={}", conf.getLoggingLevel());
		}

		IdleStateHandler idleHandler = new IdleStateHandler(conf.getReadIdleSeconds(), conf.getWriteIdleSeconds(),
				conf.getAllIdleSeconds());
		p.addLast(idleHandler);
		// ### 必须每次 getBean(..), 不能用 @Autowired
		p.addLast("decoder", this.beanFactory.getBean(TransportMessageDecoder.class));
		p.addLast("encoder", this.beanFactory.getBean(TransportMessageEncoder.class));
		p.addLast("transport", (ChannelHandler) this.beanFactory.getBean("transportMessageHandler"));
	}

}
