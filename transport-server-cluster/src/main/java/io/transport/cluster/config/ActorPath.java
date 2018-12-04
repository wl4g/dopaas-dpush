package io.transport.cluster.config;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import akka.AkkaException;

/**
 * Actor路径分配器
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年3月27日
 * @since
 */
public class ActorPath implements Serializable {
	private static final long serialVersionUID = 5816838669347803994L;
	final transient public static String ACTOR_PROTO = "akka.tcp://";
	final transient public static String SEPARATOR_H = "@";
	final transient public static String SEPARATOR_P = ":";
	final transient public static String SEPARATOR_U = "/user/";
	final transient public static String DEFAULT_ACTOR_NAME = "defaultActor";

	private String systemName;
	private String hostname;
	private int port;
	private String actorName;

	public ActorPath(String systemName, String hostname, int port) {
		this(systemName, hostname, port, null);
	}

	public ActorPath(String systemName, String hostname, int port, String actorName) {
		super();
		this.systemName = systemName;
		this.hostname = hostname;
		this.port = port;
		this.actorName = actorName;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getActorName() {
		return actorName;
	}

	public void setActorName(String actorName) {
		this.actorName = actorName;
	}

	public void validation() {
		if (StringUtils.isAnyEmpty(systemName, hostname))
			throw new AkkaException("The necessary parameters for operating actorpath are empty.");
	}

	/**
	 * 转为基础path
	 * 
	 * @return 基础path(只到端口结束，不包括路径目录)
	 */
	public String asBaseString() {
		// Check field.
		this.validation();
		// Join path.
		StringBuffer path = new StringBuffer(ACTOR_PROTO);
		path.append(getSystemName());
		path.append(SEPARATOR_H);
		path.append(getHostname());
		path.append(SEPARATOR_P);
		path.append(getPort());

		return path.toString();
	}

	/**
	 * 转为actor完整path
	 * 
	 * @return actor完整path(包括/user/目录)
	 */
	public String asString() {
		StringBuffer basicPath = new StringBuffer(this.asBaseString());
		basicPath.append(SEPARATOR_U);
		if (!StringUtils.isEmpty(getActorName()))
			basicPath.append(getActorName());

		return basicPath.toString();
	}

	@Override
	public String toString() {
		return this.asString();

	}

}
