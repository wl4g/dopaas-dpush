package io.transport.core.protocol.handler;

import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

import io.transport.cluster.ClusterService;

import io.transport.common.bean.NodeInfo;
import io.transport.common.cache.JedisService;
import io.transport.common.utils.exception.TransportException;
import io.transport.core.config.Configuration;
import io.transport.core.config.Configuration.DeploymentType;
import io.transport.core.exception.TransportAuthenticationException;
import io.transport.core.exception.TransportConnectLimitException;
import io.transport.core.protocol.message.internal.ConnectMessage;
import io.transport.core.protocol.message.internal.ConnectRespMessage;
import io.transport.core.registry.ChannelRegistry;
import io.transport.core.registry.Client;
import io.transport.core.service.DefaultClusterService;
import io.transport.core.utils.TransportProcessors;
import io.transport.mq.MessageService;

/**
 * Basic business abstraction Handler
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月11日
 * @since
 */
public abstract class AbstractChannelMessageHandler extends ChannelDuplexHandler {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(type = Configuration.class)
	protected Configuration conf;
	@Resource(type = JedisService.class)
	protected JedisService jedisService;
	@Resource
	protected ChannelRegistry registry;
	@Resource
	protected MessageService messageService;
	@Resource(type = DefaultClusterService.class)
	protected ClusterService clusterService;

	protected Client client;

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String clientId = this.client != null ? this.client.getDeviceInfo().getDeviceId() : "";
		logger.error("连接中断, clientId={}, channel={}", clientId, ctx.channel());
		// Close client.
		this.closeClient();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) throws Exception {
		if (t != null) {
			if (t instanceof TransportException)
				logger.error("处理异常. {}", t.getMessage());
			else
				logger.error("处理异常.", t);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			logger.warn("连接空闲. {}, client={}", event.state(), this.client.asText());

			switch (event.state()) {
			case READER_IDLE: // 读超时事件:可能客户端设备No-run或断线, 为节省资源则close.
				this.closeClient();
				break;
			case WRITER_IDLE: // 写超时事件:可能此连接上无业务同时也未收到心跳, 为节省资源则close.
				this.closeClient();
				break;
			case ALL_IDLE: // 读&写:异常情况(可能客户端设备No-run或断线), 为节省资源则close.
				this.closeClient();
				break;
			default:
				throw new UnsupportedOperationException("Illegal link detection type." + event.state());
			}
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		int maxConnects = TransportProcessors.isWSChannel(ctx.channel()) ? conf.getWsConfig().getAccpetMaxconnects()
				: conf.getRpcConfig().getAccpetMaxconnects();
		int localSize = this.registry.localSize();
		if (localSize >= maxConnects)
			throw new TransportConnectLimitException("Too many connections, current node connections is " + localSize);

		if (logger.isInfoEnabled())
			logger.info("新建连接. {}", ctx.channel());

		this.client = new Client((SocketChannel) ctx.channel());
		if (logger.isDebugEnabled())
			logger.debug("Reserved channel {}", this.client.getChannel());
	}

	/**
	 * Write messages back to the channel.
	 * 
	 * @param ctx
	 *            Channel context
	 * @param msg
	 *            message object.
	 */
	protected ChannelFuture echoWrite(ChannelHandlerContext ctx, Object msg) {
		return this.echoWrite(ctx, msg, false);
	}

	/**
	 * Write messages back to the channel.
	 * 
	 * @param ctx
	 *            Channel context
	 * @param object
	 *            message object.
	 * @param afterClose
	 *            Whether to close the channel back after writing it back.
	 */
	protected ChannelFuture echoWrite(ChannelHandlerContext ctx, Object msg, boolean afterClose) {
		ChannelFuture cf = null;
		// 如果当前连接客户端是浏览器类型(则使用WebSocket包装)
		if (TransportProcessors.isWSChannel(ctx.channel()))
			cf = ctx.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
		else
			cf = ctx.writeAndFlush(msg);

		// If close.
		if (afterClose) {
			cf.addListener((ChannelFuture f) -> {
				if (f.isSuccess())
					this.closeClient(); // 返回消息完成后再关闭通道
			});
		}
		return cf;
	}

	/**
	 * 新建/注册客户端连接处理
	 * 
	 * @param ctx
	 * @param msg
	 */
	protected void processConnect(ChannelHandlerContext ctx, ConnectMessage msg) {
		// 1.0 Check device logged in.
		if (this.client == null)
			throw new TransportException("Illegal channel(client is null).");
		else {
			String reqDeviceId = msg.getDeviceInfo().getDeviceId();
			String deviceId = this.client.getDeviceInfo().getDeviceId();
			// `deviceId` repeated login control, close the last connection that
			// has been established.
			if (!StringUtils.isEmpty(deviceId) || this.registry.localContains(reqDeviceId)) {
				logger.warn("Logged in device, close last channel. deviceId(req)={}, deviceId={}", reqDeviceId,
						deviceId);
				this.registry.removeClient(reqDeviceId);
			}
		}

		//
		// 1.1 Check parameter.
		msg.validation();

		//
		// 1.2 Check of appId connections limit.
		this.accpetValidation(ctx, msg.getAppId());

		//
		// 2.1 Authentication appId/secret.
		this.login(ctx, msg);

		this.client.setAppId(msg.getAppId());
		this.client.setDeviceInfo(msg.getDeviceInfo());
		this.registry.addRegistry(msg.getAppId(), this.client);
		if (logger.isInfoEnabled())
			logger.info("新建客户端. {}, {}", ctx.channel(), this.client.getDeviceInfo().getDeviceId());

		//
		// 3.1 Response connect success message.
		ConnectRespMessage resp = new ConnectRespMessage();
		// If the current is ROUTING mode deployment
		if (this.conf.getDeploymentType() == DeploymentType.ROUTING) {
			// Get active cluster node info.
			Map<String, NodeInfo> nodes = this.clusterService.clusterNodes(true, null);
			if (nodes != null) {
				for (NodeInfo node : nodes.values()) {
					boolean isWsCh = TransportProcessors.isWSChannel(ctx.channel());
					if (isWsCh)
						resp.getHostAndPorts().add(node.getHost() + ":" + node.getWsPort());
					else
						resp.getHostAndPorts().add(node.getHost() + ":" + node.getRpcPort());
				}
			}
			if (logger.isInfoEnabled())
				logger.info("节点列表. {}", resp.getHostAndPorts());
		}
		// Response echo.
		this.echoWrite(ctx, resp);
	}

	/**
	 * 关闭/注销Client
	 */
	protected void closeClient() {
		try {
			if (this.client != null) {
				// 1.1 Close client channel.
				String ctxs = this.client.getChannel().toString();
				this.client.close();

				String deviceId = this.client.getDeviceInfo().getDeviceId();
				if (logger.isInfoEnabled())
					logger.info("Closed channel '{}', deviceId={}", ctxs, deviceId);

				// 1.2 Remove client repository.
				if (!StringUtils.isEmpty(deviceId))
					this.registry.removeClient(deviceId);
				else
					logger.warn("Closed channel fail, deviceId is null.");
			} else
				logger.warn("Closed fail, client is null.");

		} catch (Exception e) {
			logger.error("Close connection failed.", e);
		}
	}

	/**
	 * 校验是否认证.
	 */
	protected void authentication() {
		if (this.client == null || !this.registry.localContains(this.client.getDeviceInfo().getDeviceId()))
			throw new TransportAuthenticationException("Unauthenticated connection channel.");
	}

	/**
	 * 检查AppID连接限制数量
	 * 
	 * @param appId
	 */
	private void accpetValidation(ChannelHandlerContext ctx, String appId) {
		// 1.1 Get appId connects default.
		int appIdConnects = TransportProcessors.isWSChannel(ctx.channel())
				? conf.getWsConfig().getDefaultAppIdConnects() : conf.getRpcConfig().getDefaultAppIdConnects();

		// 1.2 Get current appId connects config limit.
		String appIdConnectsTxt = this.jedisService.get(conf.getCtlAppIdDeviceConnectsPKey());
		if (!StringUtils.isEmpty(appIdConnectsTxt))
			appIdConnects = Integer.parseInt(appIdConnectsTxt);

		// 1.3 Get appId groups.
		Set<String> groupIds = this.registry.getAllGroupIds(appId);

		// 1.4 Get groupId sub deviceIds all.
		final Set<String> deviceIds = Sets.newHashSet();
		if (groupIds != null) {
			for (String groupId : groupIds) {
				Set<String> deviceIds0 = this.registry.getAllDeviceIds(groupId);
				if (deviceIds0 != null) {
					for (String deviceId : deviceIds0)
						// 同时在内存channel池也存在，才能确定是有效连接.
						if (this.registry.localContains(deviceId))
							deviceIds.add(deviceId);
				}
			}
		}
		if (logger.isInfoEnabled())
			logger.info("AppId连接信息. appId={}, groups={}, connected(devices)={}", appId, groupIds, deviceIds);

		// 2.1 Check the number of appId connections limit.
		if (deviceIds != null && deviceIds.size() > appIdConnects)
			throw new TransportConnectLimitException(
					"Connect over limit, more than " + appIdConnects + ", already connected is " + deviceIds.size());
	}

	/**
	 * 登录连接认证处理.
	 * 
	 * @param ctx
	 * @param msg
	 */
	private void login(ChannelHandlerContext ctx, ConnectMessage msg) {
		// 是浏览器(使用的WebSocket协议连接), 则直接验证token是否有效.
		if (TransportProcessors.isWSChannel(ctx.channel())) {
			String token = String.valueOf(msg.getDeviceInfo().getDeviceId());
			String appId = this.jedisService.get(token);
			if (!StringUtils.equals(appId, msg.getAppId()))
				throw new TransportAuthenticationException("Token authentication failed, token=" + token
						+ ", appId(req)=" + msg.getAppId() + ", appId=" + appId);
		}
		// Provider(Android或后台服务端),则使用的自定义协议连接
		else {
			String appId = String.valueOf(msg.getAppId());
			String secret = this.jedisService.get(appId);
			if (!StringUtils.equals(secret, msg.getAppSecret()))
				throw new TransportAuthenticationException(
						"AppId authentication failed. appId=" + appId + ", appSecret=" + secret);
		}
	}

	/**
	 * 转发给具体业务方法
	 * 
	 * @param ctx
	 * @param msg
	 */
	protected abstract void dispatch(ChannelHandlerContext ctx, Object msg);

}
