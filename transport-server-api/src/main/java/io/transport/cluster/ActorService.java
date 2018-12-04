package io.transport.cluster;

/**
 * Cluster actor service Interface
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月16日
 * @since
 */
public interface ActorService {

	/**
	 * Create actor and save.
	 * 
	 * @param actorAlias
	 * @return
	 */
	Object create(String actorAlias);

	/**
	 * Tell message to cluster actorName.
	 * 
	 * @param actorAlias
	 * @param message
	 * @return
	 */
	Object tell(String actorAlias, Object message);

	/**
	 * Destroy and remove actor.
	 * 
	 * @param actorAlias
	 * @return
	 */
	Object destroy(String actorAlias);

}
