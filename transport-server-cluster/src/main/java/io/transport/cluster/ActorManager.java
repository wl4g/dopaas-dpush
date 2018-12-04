package io.transport.cluster;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.net.HostAndPort;

import akka.AkkaException;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.InvalidActorNameException;
import akka.actor.Props;
import io.transport.cluster.ClusterService;
import io.transport.cluster.actor.AccpetActor;
import io.transport.cluster.config.ActorPath;
import io.transport.cluster.config.AkkaConfiguration;
import io.transport.cluster.registry.ActorRegistry;
import io.transport.common.bean.NodeInfo;

/**
 * User - side registered actor manager
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月9日
 * @since
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ActorManager implements ApplicationRunner, DisposableBean {
	final private static Logger logger = LoggerFactory.getLogger(ActorManager.class);
	volatile private boolean running = false;
	private ActorBean defaultActorBean;

	@Resource
	private AkkaConfiguration conf;
	@Resource
	private ActorRegistry registry;
	@Resource
	private ClusterService clusterService;

	private ActorSystem _system;

	@Override
	public synchronized void run(ApplicationArguments args) throws Exception {
		if (this.running) {
			logger.warn("Actor system initialized.");
			return;
		}

		// 1.1 Creating a local actor system.
		try {
			this._system = ActorSystem.create(conf.getActorSystemName(), conf.getConfig());

			// 1.2 Create a local default actor.
			this.actorCreate(ActorPath.DEFAULT_ACTOR_NAME);
			this.running = true;

			if (logger.isInfoEnabled())
				logger.info("Actor system initialization successfully.");

		} catch (Throwable t) {
			throw new AkkaException("Initialization of the actor system has failed.", t);
		}

		// 2.1 Save cluster node info.
		try {
			NodeInfo hap = new NodeInfo(conf.getHostname(), conf.getConfiguration().getRpcConfig().getPort(),
					conf.getConfiguration().getWsConfig().getPort(), conf.getRemote().getPort());
			// 初始化加入集群
			this.clusterService.initial(hap);

		} catch (Exception e) {
			throw new AkkaException("Failure to initial the cluster node information.", e);
		}
	}

	@Override
	public synchronized void destroy() throws Exception {
		try {
			// 1.1 Shutdown system.
			if (this._system != null)
				_system.shutdown();

			// 1.2 Leave the local nodes out of the cluster.
			HostAndPort actorHap = HostAndPort.fromParts(conf.getHostname(), conf.getRemote().getPort());
			this.clusterService.leaving(actorHap);

			if (logger.isInfoEnabled())
				logger.info("actor system has been shutdown.");
		} catch (Throwable t) {
			logger.error("Actor system destroy failed.", t);
		}
	}

	/**
	 * Create a actor
	 * 
	 * @param actorAlias
	 * @return
	 */
	public ActorBean actorCreate(String actorAlias) {
		// 1.0 Check info.
		if (this._system == null)
			throw new AkkaException("Create actor failed('" + actorAlias + "'), actor system is unnitialized.");
		if (StringUtils.equalsIgnoreCase(actorAlias, ActorPath.DEFAULT_ACTOR_NAME) && this.defaultActorBean != null)
			throw new AkkaException("Create actor failed, because the name '" + actorAlias + "' is the guardian.");

		// 1.1 Create accpetActor.
		try {
			this._system.actorOf(Props.create(AccpetActor.class, actorAlias), actorAlias);
		} catch (InvalidActorNameException e) {
			if (logger.isInfoEnabled())
				logger.info("Create an existing actor '{}'", actorAlias);
		} catch (Throwable t) {
			logger.error("Creating actor '" + actorAlias + "' failed.", t);
			throw new AkkaException(t.getMessage(), t);
		}

		// 1.2 Selection actor info.
		String path = new ActorPath(conf.getActorSystemName(), conf.getHostname(), conf.getRemote().getPort(), actorAlias)
				.asString();
		ActorSelection actorSel = this._system.actorSelection(path);
		ActorBean ab = new ActorBean(actorSel.anchor(), path);
		// Default actorBean.
		if (StringUtils.equalsIgnoreCase(actorAlias, ActorPath.DEFAULT_ACTOR_NAME))
			this.defaultActorBean = ab;

		// 1.3 Save actor info.
		this.registry.addRegistry(actorAlias, ab);
		return ab;
	}

	/**
	 * Destroy a actor
	 * 
	 * @param actorName
	 * @return
	 */
	public ActorBean actorDestroy(String actorName) {
		ActorBean ab = this.registry.getActorBean(actorName);
		if (ab == null) {
			logger.warn("销毁Actor失败, actorName={} is does not exist.", actorName);
			return null;
		}

		try {
			// 1.1 终结actor
			// http://doc.okbase.net/217548/archive/52530.html
			// ActorSelection acti =
			// _system.actorSelection(ab.getRemoteActorAddr());
			// this._system.stop(acti.anchor()); // 会报错
			this._system.$div(actorName);

			// 1.2 移除actor缓存.
			this.registry.removeActor(actorName);
		} catch (Exception e) {
			logger.error("Destroy actor '" + ab.getRemoteActorAddr() + "' failed.", e);
			return null;
		}
		return ab;
	}

	/**
	 * Notification message to other corresponding actor
	 * 
	 * @param message
	 * @param actorName
	 * @return
	 */
	public ActorBean actorTell(String actorName, Object message) {
		if (StringUtils.isEmpty(actorName))
			throw new AkkaException("Failed to send actor '" + actorName + "', because it does not exist or destroyed.");

		ActorBean ab = null;
		try {
			// 1.1 get actor user.
			ab = this.registry.getActorBean(actorName);
			if (ab == null)
				return null;

			// Get actor path.
			ActorSelection acti = this._system.actorSelection(ab.getRemoteActorAddr());
			acti.tell(message, acti.anchor());
			if (logger.isDebugEnabled())
				logger.debug("It has been sent to actor: `{}`", ab.getRemoteActorAddr());

		} catch (Exception e) {
			logger.error("Failed to send to actor: `" + ab + "`", e);
		}
		return ab;
	}

	public static void main(String[] args) throws Exception {
		// // System.out.println(InetAddress.getLocalHost().getHostAddress());
		// Config config = ConfigFactory.load();
		// //
		// System.out.println(config.getString("akka.remote.netty.tcp.hostname"));
		//
		// // 追加或替换key的值
		// System.out.println(config.withValue("akka.remote.netty.tcp.hostname",
		// ConfigValueFactory.fromAnyRef("192.168.x.x.x.", null)));
	}

}
