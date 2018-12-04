package io.transport.core;

import io.transport.common.bean.ChannelMetricsInfo;

/**
 * Monitor Service
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月2日
 * @since
 */
public interface MonitorService {

	/**
	 * Channel(Netty) metric information.
	 * 
	 * @return
	 */
	ChannelMetricsInfo metricsInfo();

}
