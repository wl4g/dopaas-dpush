package io.transport.core.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.net.HostAndPort;

import io.transport.cluster.ClusterService;
import io.transport.common.bean.NodeInfo;
import io.transport.common.cache.JedisService;
import io.transport.core.config.Configuration;

@Service
public class DefaultClusterService implements ClusterService {
	final private static Logger logger = LoggerFactory.getLogger(DefaultClusterService.class);

	@Autowired
	private Configuration config;
	@Autowired
	private JedisService jedisService;

	@Override
	public void initial(NodeInfo node) {
		// Set the inactive state of the node.
		node.setIsActive(false);
		Map<String, String> nodes = Maps.newHashMap();
		nodes.put(node.getId(), node.toString());
		// Update node.
		this.jedisService.mapPut(config.getCtlClusterNodesPKey(), nodes);

		if (logger.isInfoEnabled())
			logger.info("Node `{}` is joining the cluster.", node);
	}

	@Override
	public void joining(HostAndPort actorHap) {
		// Get the cluster all list matching actorPort, and update it to active
		// state.
		NodeInfo node = this.clusterNode(false, actorHap);
		if (node != null) {
			// Update to active state.
			node.setIsActive(true);
			Map<String, String> nodes = Maps.newHashMap();
			nodes.put(node.getId(), node.toString());
			// Update node.
			this.jedisService.mapPut(config.getCtlClusterNodesPKey(), nodes);

			if (logger.isInfoEnabled())
				logger.info("Node `{}` is joining the cluster.", actorHap);
		} else
			logger.warn("Node `{}` is failing to join the cluster, and the matching of the actor node is null.",
					actorHap);
	}

	@Override
	public void leaving(HostAndPort actorHap) {
		// Get the cluster all list matching actorPort, and then update it to
		// inactive state.
		NodeInfo node = this.clusterNode(false, actorHap);
		if (node != null) {
			// Update to inactive state.
			node.setIsActive(false);
			Map<String, String> nodes = Maps.newHashMap();
			nodes.put(node.getId(), node.toString());
			// Update node.
			this.jedisService.mapPut(config.getCtlClusterNodesPKey(), nodes);

			if (logger.isInfoEnabled())
				logger.info("Node `{}` has gone away from the removal cluster.", actorHap);
		} else
			logger.warn("Node `{}` failure to leave the cluster, and the matching of the actor node is null.",
					actorHap);
	}

	@Override
	public Map<String, NodeInfo> clusterNodes(boolean isActive, HostAndPort actorHap) {
		Map<String, NodeInfo> nodes = null;
		Map<String, String> nodesAll = this.jedisService.getMap(this.config.getCtlClusterNodesPKey());
		if (nodesAll != null) {
			nodes = new HashMap<>(64);
			for (String hap0 : nodesAll.values()) {
				boolean flag = false;
				NodeInfo node = JSON.parseObject(hap0, NodeInfo.class);
				if (isActive) {
					if (node != null && node.getIsActive())
						flag = true;
				} else
					flag = true;

				if (flag && (actorHap == null || actorHap != null && this.matchActorNode(node, actorHap)))
					nodes.put(node.getId(), node);
			}
		}
		return nodes;
	}

	/**
	 * Matching actor monitor node info.
	 * 
	 * @param isActive
	 * @param actorHap
	 * @return
	 */
	public NodeInfo clusterNode(boolean isActive, HostAndPort actorHap) {
		Map<String, NodeInfo> nodes = this.clusterNodes(isActive, actorHap);
		if (nodes != null) {
			if (nodes.size() == 1)
				return nodes.values().iterator().next();
			else if (nodes.size() > 1)
				throw new RuntimeException(
						"Matching to too many actor listener information `" + actorHap + "` nodes. at:\n" + nodes);
			// Ignore size=0
		}
		return null;
	}

	/**
	 * Matching actor monitor node port.
	 * 
	 * @param node
	 * @param actorHap
	 * @return
	 */
	private boolean matchActorNode(NodeInfo node, HostAndPort actorHap) {
		return StringUtils.equals(node.getHost(), actorHap.getHostText()) && node.getActorPort() == actorHap.getPort();
	}

}
