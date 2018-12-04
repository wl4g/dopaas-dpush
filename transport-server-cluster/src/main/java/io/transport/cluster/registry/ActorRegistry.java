package io.transport.cluster.registry;

import io.transport.cluster.ActorBean;

/**
 * Actor仓库
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月9日
 * @since
 */
public interface ActorRegistry {

	/**
	 * 获取ActorBean
	 * 
	 * @param actorAlias
	 *            actor名称
	 * @return
	 */
	ActorBean getActorBean(String actorAlias);

	/**
	 * 新增一个ActorBean
	 * 
	 * @param actorAlias
	 *            actor名称
	 * @param actorBean
	 *            actorBean对象
	 */
	boolean addRegistry(String actorAlias, ActorBean actorBean);

	/**
	 * 移除ActorBean
	 * 
	 * @param actorAlias
	 *            actor名称
	 * @return
	 */
	boolean removeActor(String actorAlias);

	/**
	 * 判断是否包含Actor
	 * 
	 * @param actorAlias
	 * @return
	 */
	boolean containsActor(String actorAlias);

}
