package io.transport.common.bean;

import com.alibaba.fastjson.JSON;

/**
 * 集群节点主机端口配置组
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年2月6日
 * @since
 */
public class NodeInfo {
	private Boolean isActive;
	private String host;
	private int rpcPort;
	private int wsPort;
	private int actorPort;

	public NodeInfo() {
		super();
	}

	public NodeInfo(String host, int rpcPort, int wsPort, int actorPort) {
		super();
		this.host = host;
		this.rpcPort = rpcPort;
		this.wsPort = wsPort;
		this.actorPort = actorPort;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getRpcPort() {
		return rpcPort;
	}

	public void setRpcPort(int rpcPort) {
		this.rpcPort = rpcPort;
	}

	public int getWsPort() {
		return wsPort;
	}

	public void setWsPort(int wsPort) {
		this.wsPort = wsPort;
	}

	public int getActorPort() {
		return actorPort;
	}

	public void setActorPort(int actorPort) {
		this.actorPort = actorPort;
	}

	public String getId() {
		return getHost() + ":" + getActorPort();
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

}
