package io.transport.cluster.registry;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSON;

import io.transport.cluster.ActorBean;
import io.transport.common.cache.JedisService;
import io.transport.core.config.Configuration;

/**
 * Actor仓库(Redis实现)
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月9日
 * @since
 */
@Repository
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JedisActorRegistry implements ActorRegistry {
	final protected static Logger logger = LoggerFactory.getLogger(JedisActorRegistry.class);

	@Resource
	private Configuration conf;
	@Resource
	private JedisService jedisService;

	public ActorBean getActorBean(String actorAlias) {
		String actorNameKey = this.conf.getCtlDeviceIdActorPkey() + actorAlias;
		return JSON.parseObject(this.jedisService.get(actorNameKey), ActorBean.class);
	}

	public boolean addRegistry(String actorAlias, ActorBean actorBean) {
		String actorNameKey = this.conf.getCtlDeviceIdActorPkey() + actorAlias;
		String ret = this.jedisService.set(actorNameKey, JSON.toJSONString(actorBean), 0);
		if (logger.isInfoEnabled())
			logger.info("新建Actor: {}", actorBean.getRemoteActorAddr());

		return !StringUtils.isEmpty(ret);
	}

	public boolean removeActor(String actorAlias) {
		if (logger.isInfoEnabled())
			logger.info("Remove actor'{}'", actorAlias);

		Long ret = this.jedisService.del(actorAlias);
		return ret != null;
	}

	@Override
	public boolean containsActor(String actorAlias) {
		return this.jedisService.get(actorAlias) != null;
	}

}
