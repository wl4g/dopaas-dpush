/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.transport.core.protocol.handler.ws;

import java.io.File;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.io.Resources;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.transport.common.SpringContextHolder;
import io.transport.core.config.Configuration;
import io.transport.core.config.Configuration.WSConfig;

/**
 * A HTTP server which serves Web Socket requests at:
 *
 * http://localhost:8080/websocket
 *
 * Open your browser at
 * <a href="http://localhost:8080/">http://localhost:8080/</a>, then the demo
 * page will be loaded and a Web Socket connection will be made automatically.
 *
 * This server illustrates support for the different web socket specification
 * versions and will work with:
 *
 * <ul>
 * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 * https://github.com/netty/netty/blob/00afb19d7a37de21b35ce4f6cb3fa7f74809f2ab/example/src/main/java/io/netty/example/http/websocketx/server/WebSocketServer.java
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月12日
 * @since
 */
@Component
@Lazy(false)
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class WSChildHandlerInitializer extends ChannelInitializer<SocketChannel> { // 1
	final private static Logger logger = LoggerFactory.getLogger(WSChildHandlerInitializer.class);

	@Autowired
	private Configuration config;
	private SslContext sslContext;

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		WSConfig conf = this.config.getWsConfig();
		if (logger.isDebugEnabled())
			logger.debug("Initital channel handler...\twebsocketPath={}", conf.getWebsocketPath());

		ChannelPipeline p = ch.pipeline();
		// Configure SSL.
		if (conf.getSslEnable()) {
			p.addLast(getSslContext().newHandler(ch.alloc()));
			if (logger.isDebugEnabled())
				logger.debug("The ssl(wss) handler has been enabled. sslCtx={}", getSslContext().toString());
		}

		// pipeline管理channel中的Handler，在channel队列中添加一个handler来处理业务
		if (conf.getLoggingEnable()) {
			p.addLast(new LoggingHandler(LogLevel.valueOf(conf.getLoggingLevel())));
			if (logger.isInfoEnabled())
				logger.info("Netty internal log has been used. (WS)level={}", conf.getLoggingLevel());
		}

		IdleStateHandler idleHandler = new IdleStateHandler(conf.getReadIdleSeconds(), conf.getWriteIdleSeconds(),
				conf.getAllIdleSeconds());
		p.addLast(idleHandler);
		p.addLast("http-codec", new HttpServerCodec()); // 将请求和应答消息解码为HTTP消息
		p.addLast("aggregator", new HttpObjectAggregator(65536)); // 将HTTP消息的多个部分合成一条完整的HTTP消息
		p.addLast("http-chunked", new ChunkedWriteHandler()); // 向客户端发送HTML5文件
		p.addLast("ws-protocol", new WebSocketServerProtocolHandler(conf.getWebsocketPath(), null, true));
		p.addLast("text-handler", SpringContextHolder.getBean("textWSFrameHandler"));
	}

	private SslContext getSslContext() throws SSLException {
		if (this.sslContext == null) {
			WSConfig conf = this.config.getWsConfig();
			/*
			 * SelfSignedCertificate ssc = new SelfSignedCertificate();
			 * SslContext sslCtx =
			 * SslContextBuilder.forServer(ssc.certificate(),
			 * ssc.privateKey()).build();
			 */
			File keyCertChainFile = new File(Resources.getResource(conf.getKeyCertChainFile()).getFile());
			File keyFile = new File(Resources.getResource(conf.getKeyFile()).getFile());
			this.sslContext = SslContextBuilder.forServer(keyCertChainFile, keyFile).sslProvider(SslProvider.OPENSSL).build();
		}
		return this.sslContext;
	}

}
