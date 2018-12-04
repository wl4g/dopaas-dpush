package io.transport.cluster;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

import akka.actor.ActorRef;

/**
 * Actor Bean
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月9日
 * @since
 */
public class ActorBean implements Serializable {
	private static final long serialVersionUID = 6268360018625319668L;

	@JSONField(serialize = false)
	private ActorRef actorRef;
	private String remoteActorAddr;

	public ActorBean() {
	}

	public ActorBean(ActorRef actorRef, String remoteActorAddr) {
		this.actorRef = actorRef;
		this.remoteActorAddr = remoteActorAddr;
	}

	public ActorRef getActorRef() {
		return actorRef;
	}

	public void setActorRef(ActorRef actorRef) {
		this.actorRef = actorRef;
	}

	public String getRemoteActorAddr() {
		return remoteActorAddr;
	}

	public void setRemoteActorAddr(String remoteActorAddr) {
		this.remoteActorAddr = remoteActorAddr;
	}

	@Override
	public String toString() {
		return "ActorBean [actorRef=" + actorRef + ", remoteActorAddr=" + remoteActorAddr + "]";
	}

}
