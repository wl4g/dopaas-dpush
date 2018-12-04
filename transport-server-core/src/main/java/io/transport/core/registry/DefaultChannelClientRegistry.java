package io.transport.core.registry;

import io.netty.channel.socket.SocketChannel;
import io.transport.cluster.ActorService;
import io.transport.common.cache.JedisService;
import io.transport.core.config.Configuration;
import io.transport.core.protocol.message.DeviceInfo;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;

/**
 * Client (terminal) socket connection channel registry
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月11日
 * @since
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DefaultChannelClientRegistry implements ChannelRegistry {
	final private static Logger logger = LoggerFactory.getLogger(DefaultChannelClientRegistry.class);
	private Map<String, Client> channelRegistry;

	@Resource
	private Configuration conf;
	@Resource
	private JedisService jedisService;
	@Resource
	private ActorService actorService;

	public DefaultChannelClientRegistry() {
		synchronized (this) {
			if (this.channelRegistry == null)
				this.channelRegistry = new ConcurrentHashMap<>(16384, 0.75f);
		}
	}

	@Override
	public Set<Client> getLocalClients(String groupId) {
		Set<String> deviceIds = this.getAllDeviceIds(groupId);
		Set<Client> clients = null;
		if (deviceIds != null && !deviceIds.isEmpty()) {
			clients = Sets.newLinkedHashSetWithExpectedSize(deviceIds.size());
			for (String cId : deviceIds) {
				Client c = this.getLocalClient(cId);
				if (c != null)
					clients.add(c);
			}
		}
		return clients;
	}

	@Override
	public Client getLocalClient(String deviceId) {
		return this.channelRegistry.get(String.valueOf(deviceId));
	}

	@Override
	public void setLocalChannel(String deviceId, SocketChannel channel) {
		if (!this.localContains(deviceId)) {
			logger.warn("Set channel(deviceId: {})不存在.", deviceId);
			return;
		}
		Client cli = this.getLocalClient(deviceId);
		if (cli != null) {
			if (logger.isDebugEnabled())
				logger.debug("Update client channel: {}", cli.asText());
			cli.setChannel(channel);
		}
	}

	@Override
	public boolean localContains(String deviceId) {
		if (StringUtils.isEmpty(deviceId))
			return false;
		return this.channelRegistry.containsKey(deviceId);
	}

	@Override
	public int localSize() {
		return this.channelRegistry.size();
	}

	@Override
	public Client getClient(String deviceId) {
		String key0 = conf.getCtlDeviceIdClientInfoPKey() + deviceId;
		return JSON.parseObject(this.jedisService.get(key0), Client.class);
	}

	@Override
	public boolean addRegistry(String appId, Client client) {
		if (client == null || StringUtils.isEmpty(client.getDeviceInfo().getDeviceId()))
			return false;

		DeviceInfo info = client.getDeviceInfo();
		String groupId = info.getGroupId();
		String deviceId = info.getDeviceId();
		if (this.localContains(deviceId)) {
			logger.warn("Overlay Client, deviceId={}", deviceId);
			// Ignore process, because repository does not handle business
			// logic.
		}

		// 1.1 Save appId-groupIds information.
		if (!StringUtils.isEmpty(appId)) {
			String key0 = conf.getCtlAppIdGroupsPKey() + appId;
			this.jedisService.setSetAdd(key0, groupId);
		}
		// 1.2 Save groupId-deviceIds information.
		if (!StringUtils.isEmpty(groupId)) {
			String key0 = conf.getCtlGroupIdDevicesPKey() + groupId;
			this.jedisService.setSetAdd(key0, deviceId);
		}
		// 1.3 Save deviceId(client) information.
		if (!StringUtils.isEmpty(deviceId)) {
			String key0 = conf.getCtlDeviceIdClientInfoPKey() + deviceId;
			this.jedisService.set(key0, JSON.toJSONString(client), 0);
		}

		// 2.1 Create and save actor(Using deviceId as actorName.).
		this.actorService.create(deviceId);

		return (this.channelRegistry.put(deviceId, client) != null) ? true : false;
	}

	@Override
	public boolean removeClient(String deviceId) {
		if (StringUtils.isEmpty(deviceId))
			return false;

		// 1.0 Get client by deviceId.
		Client cli = this.getLocalClient(deviceId);
		// 1.1 Remove deviceId channel.
		if (cli != null) {
			cli.close();
			// 1.1.1 Get device info by deviceId.
			DeviceInfo info = cli.getDeviceInfo();

			// 1.2 Remove redis groupId sub deviceId.
			if (info != null && info.getGroupId() != null) {
				String key0 = conf.getCtlGroupIdDevicesPKey() + info.getGroupId();
				// Remove groupId-deviceIds mapping.
				Long r = this.jedisService.delSetMember(key0, info.getDeviceId());
				if (r == null || r == 0)
					logger.error("Remove groupId={}, deviceId={} failed.", info.getGroupId(), deviceId);
				else if (logger.isInfoEnabled())
					logger.info("Remove groupId={}, deviceId={} completed.", info.getGroupId(), deviceId);
			}
		} else
			logger.warn("Remove deviceId={} failed, client does not exist.", deviceId);

		// 2.1 Remove actor.
		String actorName = this.conf.getCtlDeviceIdActorPkey() + deviceId;
		this.actorService.destroy(actorName);

		return channelRegistry.remove(deviceId) == null ? false : true;
	}

	@Override
	public Set<String> getAllGroupIds(String appId) {
		String key0 = conf.getCtlAppIdGroupsPKey() + appId;
		return this.jedisService.getSet(key0);
	}

	@Override
	public Set<String> getAllDeviceIds(String groupId) {
		String key0 = conf.getCtlGroupIdDevicesPKey() + groupId;
		return this.jedisService.getSet(key0);
	}

}
