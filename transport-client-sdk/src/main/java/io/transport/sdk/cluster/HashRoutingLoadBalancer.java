package io.transport.sdk.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;

import io.transport.sdk.Configuration;
import io.transport.sdk.Configuration.HostAndPort;
import io.transport.sdk.exception.TransportException;
import io.transport.sdk.utils.CRC16;
import io.transport.sdk.utils.PlatformConstants;

/**
 * Hash智能负载均衡器
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年2月5日
 * @since
 */
public class HashRoutingLoadBalancer extends AbstractRoutingLoadBalancer {
	final public static String HASH_LB_KEY = "HASH_LB_" + HashRoutingLoadBalancer.class.getSimpleName();

	public HashRoutingLoadBalancer(Configuration config) {
		super(config);
	}

	@Override
	public HostAndPort determineCurrentLookupNode() {
		//
		// 1.1 获取预选节点
		HostAndPort preHap = null;
		try {
			preHap = this.getPreselectedHap();
		} catch (Exception e) {
			this.config.getLoggerImpl().error("All cluster nodes of the server failed.", e);
			// 清空连接记录，强制重置节点列表
			this.clearHapRecords();
			// 重新获取预选
			preHap = this.getPreselectedHap();
		}
		return preHap;
	}

	@Override
	public void onConnectFailed(HostAndPort hap) {
		List<ConnectRecord> records = this.getHapRecords();
		records.add(new ConnectRecord(hap, System.currentTimeMillis(), false));
		// 持久化连接记录
		this.config.getStoreImpl().put(HASH_LB_KEY, JSON.toJSONString(records));
	}

	/**
	 * 获取预选节点
	 */
	private HostAndPort getPreselectedHap() {
		List<HostAndPort> preHaps = new ArrayList<HostAndPort>();
		for (HostAndPort hap0 : this.config.getHostAndPorts()) {
			if (this.isRetryNode(hap0)) // 是否可作为预选节点
				preHaps.add(hap0);
		}
		int crc16 = CRC16.crc16Modbus(PlatformConstants.appSerial.getBytes());
		int size = preHaps.size();
		if (size == 0)
			throw new TransportException("All cluster nodes of the server failed.");

		int nodeIndex = crc16 % size & (size - 1);
		return preHaps.get(nodeIndex);
	}

	/**
	 * 是否可作为重试节点（若nodeChooseInterval毫秒内的连接失败次数小于reconnectRetry则可作为预选节点）
	 * 
	 * return this.hapRecords.parallelStream().filter(record ->
	 * record.getHostAndPort().equals(hap))
	 * .collect(Collectors.groupingBy(record -> record,
	 * Collectors.counting())).getOrDefault(hap, 0L);
	 * 
	 * @param hap
	 * @return
	 */
	private boolean isRetryNode(HostAndPort hap) {
		long now = System.currentTimeMillis();
		long latelyInterval = config.getNodeChooseInterval() * 1000;
		int failCount = 0;
		for (ConnectRecord record : getHapRecords()) {
			if (record.getHostAndPort().equals(hap) && Math.abs(now - record.getTimestamp()) < latelyInterval
					&& !record.isStatus()) {
				++failCount;
			}
		}
		return failCount < config.getReconnectRetry();
	}

	/**
	 * 获取负载记录
	 * 
	 * @return
	 */
	private List<ConnectRecord> getHapRecords() {
		List<ConnectRecord> list = JSON.parseArray(this.config.getStoreImpl().get(HASH_LB_KEY), ConnectRecord.class);
		if (list == null)
			list = Collections.emptyList();
		return list;
	}

	/**
	 * 清除所有负载连接记录
	 */
	private void clearHapRecords() {
		this.config.getStoreImpl().remove(HASH_LB_KEY);
	}

	/**
	 * 集群节点连接记录
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年2月6日
	 * @since
	 */
	public static class ConnectRecord implements Serializable {
		private static final long serialVersionUID = -7006190360223991463L;

		private HostAndPort hostAndPort;
		private long timestamp;
		private boolean status;

		public ConnectRecord() {
			super();
		}

		public ConnectRecord(HostAndPort hostAndPort, long timestamp, boolean status) {
			super();
			this.hostAndPort = hostAndPort;
			this.timestamp = timestamp;
			this.status = status;
		}

		public HostAndPort getHostAndPort() {
			return hostAndPort;
		}

		public void setHostAndPort(HostAndPort hostAndPort) {
			this.hostAndPort = hostAndPort;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}

	}

	public static void main(String[] args) {
		// 初始化
		// Configuration config = new Configuration(null, null, null, null,
		// null);
		// List<HostAndPort> hostAndPorts = new ArrayList<HostAndPort>();
		// HostAndPort hap0 = new HostAndPort("192.168.1.100", 10030);
		// HostAndPort hap1 = new HostAndPort("192.168.1.101", 10030);
		// HostAndPort hap2 = new HostAndPort("192.168.1.102", 10030);
		// HostAndPort hap3 = new HostAndPort("192.168.1.103", 10030);
		// HostAndPort hap4 = new HostAndPort("192.168.1.104", 10030);
		// HostAndPort hap5 = new HostAndPort("192.168.1.105", 10030);
		// HostAndPort hap6 = new HostAndPort("192.168.1.106", 10030);
		// hostAndPorts.add(hap0);
		// hostAndPorts.add(hap1);
		// hostAndPorts.add(hap2);
		// hostAndPorts.add(hap3);
		// hostAndPorts.add(hap4);
		// hostAndPorts.add(hap5);
		// hostAndPorts.add(hap6);
		// config.setHostAndPorts(hostAndPorts);
		// HashRoutingLoadBalancer routingBalancer = new
		// HashRoutingLoadBalancer(config);
		//
		// // 模拟失败
		// routingBalancer.onConnectFailed(hap0);
		// routingBalancer.onConnectFailed(hap1);
		// routingBalancer.onConnectFailed(hap2);
		// routingBalancer.onConnectFailed(hap3);
		// routingBalancer.onConnectFailed(hap5);
		// routingBalancer.onConnectFailed(hap4);
		// routingBalancer.onConnectFailed(hap6);
		// System.out.println(routingBalancer.isRetryNode(hap0));
		// System.out.println(routingBalancer.determineCurrentLookupNode());
		//
		// // 模拟重连
		// int crc16 = 23, nodes = 10;
		// int mod = crc16 % nodes;
		// int index = mod & (nodes - 1);
		// System.out.println(index);
	}

}
