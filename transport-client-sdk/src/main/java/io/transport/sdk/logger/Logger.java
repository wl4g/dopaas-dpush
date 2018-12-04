package io.transport.sdk.logger;

import java.io.Serializable;

/**
 * 日志接口
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月19日
 * @since
 */
public abstract interface Logger extends Serializable {

	void debug(String msg);

	void debug(String msg, Throwable t);

	void info(String msg);

	void info(String msg, Throwable t);

	void warn(String msg);

	void warn(String msg, Throwable t);

	void error(String msg);

	void error(String msg, Throwable t);

}
