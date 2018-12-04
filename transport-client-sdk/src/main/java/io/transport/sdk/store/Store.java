package io.transport.sdk.store;

/**
 * 持久化接口，可用于存储记录、日志、消息持久化
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月2日
 * @since
 */
public interface Store {

	/**
	 * 添加存储值
	 * 
	 * @param key
	 * @param value
	 */
	void put(String key, String value);

	/**
	 * 获取存储值
	 * 
	 * @param key
	 * @param value
	 */
	String get(String key);

	/**
	 * 移除存储值
	 * 
	 * @param key
	 * @param value
	 */
	void remove(String key);

}
