package io.transport.cluster.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import io.transport.cluster.ActorBean;
import io.transport.cluster.ActorManager;
import io.transport.cluster.ActorService;

@Service
public class DefaultActorServiceImpl implements ActorService {

	@Resource
	private ActorManager manager;

	@Override
	public ActorBean create(String actorAlias) {
		return this.manager.actorCreate(actorAlias);
	}

	@Override
	public ActorBean tell(String actorAlias, Object message) {
		return this.manager.actorTell(actorAlias, message);
	}

	@Override
	public ActorBean destroy(String actorAlias) {
		return this.manager.actorDestroy(actorAlias);
	}

}
