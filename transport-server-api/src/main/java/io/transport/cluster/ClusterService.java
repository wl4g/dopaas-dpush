package io.transport.cluster;

import java.util.Map;

import com.google.common.net.HostAndPort;

import io.transport.common.bean.NodeInfo;

public interface ClusterService {

	/**
	 * 初始化节点信息(服务启动后加入集群列表, 此时活跃状态为false)
	 * 
	 * @param node
	 */
	void initial(NodeInfo node);

	/**
	 * 重新加入集群(更新actor对应节点活跃状态)
	 * 
	 * @param node
	 */
	void joining(HostAndPort actorHap);

	/**
	 * 离开集群
	 * 
	 * @param actorHap
	 *            节点主机地址:actor端口
	 */
	void leaving(HostAndPort actorHap);

	/**
	 * 当前集群列表
	 * 
	 * @param isActive
	 *            过滤仅返回活跃的节点
	 * @param actorHap
	 *            过滤仅匹配actorHap的单个节点
	 * @return
	 */
	Map<String, NodeInfo> clusterNodes(boolean isActive, HostAndPort actorHap);

}
