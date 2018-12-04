package io.transport.sdk;

import java.io.Closeable;
import java.io.IOException;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GenericFutureListener;
import io.transport.sdk.Configuration.HostAndPort;
import io.transport.sdk.exception.TransportException;
import io.transport.sdk.logger.Level;
import io.transport.sdk.protocol.codec.TransportMessageDecoder;
import io.transport.sdk.protocol.codec.TransportMessageEncoder;

/**
 * Channel socket client.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @time 2016年8月19日
 * @since
 */
public final class TransportConnector implements Closeable {
	private Configuration config;
	private TransportClient client;
	private Bootstrap bootstrap;
	private EventLoopGroup workerGroup;
	private Channel channel;

	public TransportConnector(TransportClient client) {
		super();
		this.client = client;
		this.config = this.client.getConfig();
	}

	/**
	 * 建立连接并初始化Nio channel
	 * 
	 * @return this
	 * @throws InterruptedException
	 */
	synchronized public TransportConnector configure() {
		try {
			if (this.bootstrap != null) {
				// this.config.getLogger().warn("Initialized connector.");
				return this;
			}

			// Bootstrap program.
			this.bootstrap = new Bootstrap();
			// Receiving connections and processing connections by Nio.
			this.workerGroup = new NioEventLoopGroup();
			this.bootstrap.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.config.getConnecTimeout() * 1000);
			// this.bootstrap.option(ChannelOption.SO_TIMEOUT,
			// this.config.soTimeout());
			// When a connection arrives, a channel will be created.
			this.bootstrap.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					// pipeline管理channel中的Handler，在channel队列中添加一个handler来处理业务
					ChannelPipeline p = ch.pipeline();
					if (config.isLoggingEnable() && config.getLevel().getValue() >= Level.DEBUG.getValue())
						p.addLast(new LoggingHandler(LogLevel.INFO));

					p.addLast(new IdleStateHandler(config.getReadIdleSeconds(), config.getWriteIdleSeconds(),
							config.getAllIdleSeconds()));
					p.addLast("decoder", new TransportMessageDecoder(config));
					p.addLast("encoder", new TransportMessageEncoder(config));
					p.addLast("receiveTextHandler", (ChannelHandler) config.getHandler().newInstance().setClient(client));
				}
			});
			// The thread begins to wait here unless there is a socket event
			// wake-up.
			// f.channel().closeFuture().sync();

		} catch (Exception e) {
			throw new TransportException("Connection channel configuration error.", e);
		}

		return this;
	}

	/**
	 * 连接到服务器
	 * 
	 * @param sync
	 * @param callback
	 */
	synchronized public void connect(final boolean sync) {
		try {
			if (this.bootstrap == null)
				this.configure();

			// Get the current load balancing node.
			final HostAndPort hap = this.config.getRoutingBalancer().determineCurrentLookupNode();
			this.config.getLoggerImpl().info("Connecting to " + hap + " ...");

			// Configuration completion, starting connection with server,
			// blocking by calling the sync synchronization method until the
			// connection is successful.
			ChannelFuture cf = this.bootstrap.connect(hap.getHost(), hap.getPort());

			// Reconnect listener.
			cf.addListener(new GenericFutureListener<ChannelFuture>() {

				@Override
				public void operationComplete(ChannelFuture f) throws Exception {
					if (!f.isSuccess()) {
						f.channel().eventLoop().schedule(new Runnable() {

							@Override
							public void run() {
								config.getLoggerImpl().info("Connecting to " + hap + " failed.");
								// Update connection failure (counter, for load
								// balance calculation).
								config.getRoutingBalancer().onConnectFailed(hap);
								// Retry connection.
								connect(sync);
							}
						}, config.getReconnectDelay(), TimeUnit.SECONDS);
					} else {
						config.getLoggerImpl().info("Connected " + hap);
						// Log on after the connection is successful.
						client.login();
					}
				}
			});

			if (sync)
				this.channel = cf.sync().channel();
			else
				this.channel = cf.channel();

		} catch (Exception e) {
			throw new TransportException("Connection failed.", e);
		} finally {
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}));
		}
	}

	public Channel channel() {
		return channel;
	}

	@Override
	synchronized public void close() throws IOException {
		try {
			if (this.workerGroup != null)
				this.workerGroup.shutdownGracefully();
			if (this.channel != null)
				this.channel.close();

		} catch (Exception e) {
			throw new TransportException(e);
		} finally {
			this.channel = null;
			this.workerGroup = null;
			this.bootstrap = null;
			System.gc();
		}
	}

}
