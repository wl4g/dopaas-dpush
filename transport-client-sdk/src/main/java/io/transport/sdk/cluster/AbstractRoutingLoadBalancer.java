package io.transport.sdk.cluster;

import io.transport.sdk.Configuration;
import io.transport.sdk.Configuration.HostAndPort;

/**
 * 基础集群节点负载均衡路由器
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年2月5日
 * @since
 */
public abstract class AbstractRoutingLoadBalancer {

	protected Configuration config;

	public AbstractRoutingLoadBalancer(Configuration config) {
		this.config = config;
	}

	/**
	 * 确立当前可尝试链接的节点
	 * 
	 * @return
	 */
	public abstract HostAndPort determineCurrentLookupNode();

	/**
	 * 记录链接失败
	 * 
	 * @param hap
	 */
	public abstract void onConnectFailed(HostAndPort hap);

}
