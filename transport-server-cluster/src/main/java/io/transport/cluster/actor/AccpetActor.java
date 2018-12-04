package io.transport.cluster.actor;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.net.HostAndPort;

import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import io.transport.cluster.ClusterService;
import io.transport.common.SpringContextHolder;
import io.transport.core.protocol.message.internal.TransportMessage;
import io.transport.core.utils.TransportProcessors;

/**
 * 接收其他 Actor 消息类
 * 
 * @author Wangy
 *
 */
public class AccpetActor extends UntypedActor {
	final protected static Logger logger = LoggerFactory.getLogger(AccpetActor.class);

	private ClusterService clusterService;

	private Cluster cluster = Cluster.get(getContext().system());
	private String actorName;

	public AccpetActor(String actorName) {
		try {
			this.actorName = actorName;
			this.clusterService = SpringContextHolder.getBean(ClusterService.class);
		} catch (Exception e) {
			logger.error("Initialization of AccpetActor failure.", e);
		}
	}

	@Override
	public void postStop() throws Exception {
		this.cluster.unsubscribe(getSelf());
	}

	@Override
	public void preStart() throws Exception {
		this.cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class,
				UnreachableMember.class);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg == null)
			return;
		if (logger.isInfoEnabled()) {
			Object print = msg;
			if (!(msg instanceof CharSequence)) {
				try {
					print = JSON.toJSONString(msg);
				} catch (Exception e) {
					// Ignore process.
				}
			}
			logger.info("Actor received. actorName={}, msg={}", actorName, print);
		}

		// Route forwarding.
		try {
			this.dispatch(msg);
		} catch (Exception e) {
			logger.error("Actor receive处理失败.", e);
		}
	}

	/**
	 * Routing forward push message
	 * 
	 * @param msg
	 *            message object.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void dispatch(Object msg) {
		if (msg instanceof MemberUp) {
			MemberUp mUp = (MemberUp) msg;
			logger.warn("Member is Up: {}", mUp.member());
			// Join cluster node.
			this.clusterService.joining(this.getHostAndPort(mUp.member()));

		} else if (msg instanceof UnreachableMember) {
			UnreachableMember mUnreachable = (UnreachableMember) msg;
			logger.warn("Member detected as unreachable: {}", mUnreachable.member());
			// Leaving cluster node.
			this.clusterService.leaving(this.getHostAndPort(mUnreachable.member()));

		} else if (msg instanceof MemberRemoved) {
			MemberRemoved mRemoved = (MemberRemoved) msg;
			logger.warn("Member is Removed: {}", mRemoved.member());
			// Remove cluster node.
			this.clusterService.leaving(this.getHostAndPort(mRemoved.member()));

		} else if (msg instanceof MemberEvent) {
			MemberRemoved mEvent = (MemberRemoved) msg;
			logger.warn("Member is event: {}", mEvent.member());
		}
		// Single message processing.
		else if (msg instanceof TransportMessage) {
			this.processTransportMessage((TransportMessage) msg);
		}
		// Batch message processing.
		else if (msg instanceof Collection) {
			Collection<Object> msgs = (Collection) msg;
			for (Object tmsg : msgs) {
				if (tmsg instanceof TransportMessage)
					this.processTransportMessage((TransportMessage) tmsg);
			}
		} else {
			super.unhandled(msg);
		}
	}

	/**
	 * Processing the transmission message.
	 * 
	 * @param msg
	 *            message object.
	 */
	private void processTransportMessage(TransportMessage msg) {

		if (!StringUtils.isEmpty(msg.getToGroupId())) {
			TransportProcessors.sentGroupMsg(msg.getToGroupId(), msg);
			if (logger.isDebugEnabled())
				logger.debug("Actor sent to toGroupId={}.", msg.getToGroupId());
		}

		// Point-to-point sending;
		else if (!StringUtils.isEmpty(msg.getToDeviceId())) {
			TransportProcessors.sentMsg(msg.getToDeviceId(), msg);
			if (logger.isDebugEnabled())
				logger.debug("Actor sent to toDeviceId={}.", msg.getToDeviceId());
		}

	}

	/**
	 * 依据Actor节点信息（节点信息ID）获取对应RPC/WS监听端口信息
	 * 
	 * @param member
	 * @return
	 */
	private HostAndPort getHostAndPort(Member member) {
		String host = member.address().host().get();
		int port = Integer.parseInt(String.valueOf(member.address().port().get()));
		return HostAndPort.fromParts(host, port);
	}

}
