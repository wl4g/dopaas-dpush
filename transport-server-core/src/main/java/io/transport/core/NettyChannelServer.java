package io.transport.core;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * Netty server
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年12月19日
 * @since
 */
public class NettyChannelServer {
	final private Logger log = LoggerFactory.getLogger(getClass());
	volatile private AtomicBoolean running = new AtomicBoolean(false);
	private String name = "defaultServer";
	private String hostname = "127.0.0.1";
	private int port = 10030;
	private int backlog = 512;

	private ChannelInitializer<SocketChannel> handlerInitializer;

	public NettyChannelServer(ChannelInitializer<SocketChannel> handlerInitializer) {
		super();
		this.handlerInitializer = handlerInitializer;
	}

	public NettyChannelServer(String name, String hostname, int port, int backlog,
			ChannelInitializer<SocketChannel> handlerInitializer) {
		super();
		this.name = name;
		this.hostname = hostname;
		this.port = port;
		this.backlog = backlog;
		this.handlerInitializer = handlerInitializer;
	}

	public void doStart() {
		new Thread(() -> listen()).start();
	}

	/**
	 * Listener service startup
	 * 
	 * @throws Exception
	 * @sine
	 */
	private void listen() {
		if (this.running.compareAndSet(false, true)) {
			log.info("Netty server has been started({}).", this.port);
			return;
		}

		// 启动引导程序
		ServerBootstrap bootstrap = new ServerBootstrap();
		// 事件处理器组(masters用来接收客户端连接并分配给slaves，slaves用来处理客户端连接)
		EventLoopGroup masters = new NioEventLoopGroup(1, new DefaultThreadFactory("NettyServerMaster", true));
		// Threads set 0, Netty will use availableProcessors () * 2 by default
		EventLoopGroup worker = new NioEventLoopGroup(0, new DefaultThreadFactory("NettyServerWorker", true));
		try {
			bootstrap.group(masters, worker);
			// 设置为Nio通道模式
			bootstrap.channel(NioServerSocketChannel.class);
			// 设置通道传输模式，立即传输模式，不需要等待特定大小
			bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
			// 设置重用处于TIME_WAIT但是未完全关闭的socket地址
			// https://www.cnblogs.com/zemliu/p/3692996.html
			bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
			// 设置ByteBuff内存分配器
			// 主要有两种创建方式：UnpooledByteBufAllocator/PooledByteBufAllocator，在netty5.0中后者是默认的，可以重复利用之前分配的内存空间。这个可以有效减少内存的使用
			bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			// 设置worker的socket通道模式，长连接
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			// 设置最大连接数(TCP底层syns队列/accept队列)，是提供给NioServerSocketChannel用来接收进来的连接,也就是boss线程
			// https://www.jianshu.com/p/e6f2036621f4，注意会依赖操作系统的TCP连接队列
			bootstrap.option(ChannelOption.SO_BACKLOG, backlog);
			// http://www.52im.net/thread-166-1-1.html
			// bootstrap.childOption(ChannelOption.SO_SNDBUF, 32);
			// bootstrap.childOption(ChannelOption.SO_RCVBUF, 32);
			// 设置slaves的处理器队列
			bootstrap.childHandler(this.handlerInitializer);

			// 绑定端口号，以异步方式提供服务
			ChannelFuture f = bootstrap.bind(this.hostname, this.port).sync();
			f.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					Throwable t = future.cause();
					if (future.isSuccess())
						log.info("Netty started on port(s): {} ({})", hostname + ":" + port, name);
					else
						log.error(t.getMessage(), t);
				}
			});

			// The thread begins to wait here unless there is a socket event
			// wake-up.
			f.channel().closeFuture().sync();

		} catch (InterruptedException e) {
			log.error("Netty server start failed.", e);
			throw new RuntimeException(e);
		} finally {
			masters.shutdownGracefully();
			worker.shutdownGracefully();
			if (this.running.compareAndSet(true, false)) {
				throw new IllegalStateException("Socket not listen");
			}
			log.info("Netty server stop gracefully({}).", this.port);
		}
	}

}
