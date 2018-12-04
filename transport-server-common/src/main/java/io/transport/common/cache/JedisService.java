/**
 * Copyright &copy; 2012-2014 gdcmkejiAll rights reserved.
 */
package io.transport.common.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.transport.common.utils.ObjectUtils;
import io.transport.common.utils.StringUtils;
import redis.clients.jedis.JedisCluster;

/**
 * Redis operations tools
 * 
 * @author liweiling
 * @version 2014-6-29
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JedisService {
	final private static Logger logger = LoggerFactory.getLogger(JedisService.class);

	@Autowired
	private JedisCluster jedisCluster;

	public JedisCluster getJedisCluster() {
		Assert.isTrue(jedisCluster != null, "Redis cluster object creation failed.");
		return this.jedisCluster;
	}

	/**
	 * 获取缓存
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	public String get(final String key) {
		return (String) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				String value = jedisCluster.get(key);
				value = StringUtils.isNotBlank(value) && !"nil".equalsIgnoreCase(value) ? value : null;
				if (logger.isDebugEnabled())
					logger.debug("get {} = {}", key, value);
				return value;
			}
		});
	}

	/**
	 * 获取缓存
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	public Object getObject(final String key) {
		return execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Object value = toObject(jedisCluster.get(getBytesKey(key)));
				if (logger.isDebugEnabled()) {
					logger.debug("getObject {} = {}", key, value);
				}
				return value;
			}
		});
	}

	/**
	 * 设置缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param cacheSeconds
	 *            超时时间，0为不超时
	 * @return
	 */
	public String set(final String key, final String value, final int cacheSeconds) {
		return (String) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				String result = null;
				if (cacheSeconds != 0) {
					result = jedisCluster.setex(key, cacheSeconds, value);
				} else {
					result = jedisCluster.set(key, value);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("set {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 设置缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param cacheSeconds
	 *            超时时间，0为不超时
	 * @return
	 */
	public String setObject(final String key, final Object value, final int cacheSeconds) {
		return (String) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				String result = jedisCluster.set(getBytesKey(key), toBytes(value));
				if (cacheSeconds != 0) {
					jedisCluster.expire(key, cacheSeconds);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("setObject {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 获取List缓存
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	@SuppressWarnings("unchecked")
	public List<String> getList(final String key) {
		return (List<String>) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				List<String> value = jedisCluster.lrange(key, 0, -1);
				if (logger.isDebugEnabled()) {
					logger.debug("getList {} = {}", key, value);
				}
				return value;
			}
		});
	}

	/**
	 * 获取List缓存
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getObjectList(final String key) {
		return (List<Object>) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				List<Object> value = Lists.newArrayList();
				List<byte[]> list = jedisCluster.lrange(getBytesKey(key), 0, -1);
				for (byte[] bs : list) {
					value.add(toObject(bs));
				}
				return value;
			}
		});
	}

	/**
	 * 设置List缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param cacheSeconds
	 *            超时时间，0为不超时
	 * @return
	 */
	public Long setList(final String key, final List<String> value, final int cacheSeconds) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = jedisCluster.rpush(key, value.toArray(new String[] {}));
				if (cacheSeconds != 0) {
					jedisCluster.expire(key, cacheSeconds);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("setList {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 设置List缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param cacheSeconds
	 *            超时时间，0为不超时
	 * @return
	 */
	public Long setObjectList(final String key, final List<Object> value, final int cacheSeconds) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = 0L;
				if (value != null && !value.isEmpty()) {
					byte[][] members = new byte[value.size()][0];
					int i = 0;
					for (Object o : value) {
						members[i] = toBytes(o);
						++i;
					}
					result = jedisCluster.sadd(getBytesKey(key), members);
				}
				if (cacheSeconds != 0) {
					jedisCluster.expire(key, cacheSeconds);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("setObjectList {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 向List缓存中添加值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public Long listAdd(final String key, final String... value) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = jedisCluster.rpush(key, value);
				if (logger.isDebugEnabled())
					logger.debug("listAdd {} = {}", key, value);
				return result;
			}
		});
	}

	/**
	 * 向List缓存中添加值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public Long listObjectAdd(final String key, final Object... value) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = 0L;
				if (value != null && value.length != 0) {
					byte[][] members = new byte[value.length][0];
					int i = 0;
					for (Object o : value) {
						members[i] = toBytes(o);
						++i;
					}
					result = jedisCluster.rpush(getBytesKey(key), members);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("listObjectAdd {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 获取缓存
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getSet(final String key) {
		return (Set<String>) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Set<String> value = jedisCluster.smembers(key);
				if (logger.isDebugEnabled())
					logger.debug("getSet {} = {}", key, value);
				return value;
			}
		});
	}

	/**
	 * 获取缓存
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	@SuppressWarnings("unchecked")
	public <T> Set<T> getObjectSet(final String key) {
		return (Set<T>) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Set<T> value = Sets.newHashSet();
				Set<byte[]> set = jedisCluster.smembers(getBytesKey(key));
				for (byte[] bs : set) {
					value.add((T) toObject(bs));
				}
				if (logger.isDebugEnabled()) {
					logger.debug("getObjectSet {} = {}", key, value);
				}
				return value;
			}
		});
	}

	/**
	 * 设置Set缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param cacheSeconds
	 *            超时时间，0为不超时
	 * @return
	 */
	public Long setSet(final String key, final Set<String> value, final int cacheSeconds) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = 0L;
				if (value != null && !value.isEmpty())
					result = jedisCluster.sadd(key, value.toArray(new String[] {}));
				if (cacheSeconds != 0)
					jedisCluster.expire(key, cacheSeconds);
				if (logger.isDebugEnabled())
					logger.debug("setSet {} = {}", key, value);
				return result;
			}
		});
	}

	/**
	 * 设置Set缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param cacheSeconds
	 *            超时时间，0为不超时
	 * @return
	 */
	public Long setObjectSet(final String key, final Set<Object> value, final int cacheSeconds) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = 0L;
				if (value != null && !value.isEmpty()) {
					byte[][] members = new byte[value.size()][0];
					int i = 0;
					for (Object o : value) {
						members[i] = toBytes(o);
						++i;
					}
					result = jedisCluster.sadd(getBytesKey(key), members);
				}
				if (cacheSeconds != 0)
					jedisCluster.expire(key, cacheSeconds);

				if (logger.isDebugEnabled()) {
					logger.debug("setObjectSet {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 向Set缓存中添加值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public Long setSetAdd(final String key, final String... value) {

		return (Long) execute(new Callback() {

			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = 0L;
				if (value != null && value.length != 0)
					result = jedisCluster.sadd(key, value);
				if (logger.isDebugEnabled()) {
					logger.debug("setSetAdd {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 向Set缓存中添加值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public Long setSetObjectAdd(final String key, final Object... value) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = 0L;
				if (value != null && value.length != 0) {
					byte[][] members = new byte[value.length][0];
					int i = 0;
					for (Object o : value) {
						members[i] = toBytes(o);
						++i;
					}
					result = jedisCluster.sadd(getBytesKey(key), members);
				}
				logger.debug("setSetObjectAdd {} = {}", key, value);
				return result;
			}
		});
	}

	/**
	 * 删除Set缓存中普通成员
	 * 
	 * @param key
	 * @param members
	 * @return
	 */
	public Long delSetMember(final String key, final String... members) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = 0L;
				if (members != null && members.length != 0)
					result = jedisCluster.srem(key, members);
				if (logger.isDebugEnabled()) {
					logger.debug("delSetMember {}", key);
				}
				return result;
			}
		});
	}

	/**
	 * 删除Set缓存中对象成员
	 * 
	 * @param key
	 * @param members
	 * @return
	 */
	public Long delSetObjectMember(final String key, final Object... members) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = 0L;
				if (members != null && members.length != 0) {
					byte[][] members0 = new byte[members.length][0];
					int i = 0;
					for (Object o : members) {
						members0[i] = toBytes(o);
						++i;
					}
					result = jedisCluster.srem(getBytesKey(key), members0);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("delSetMember {}", key);
				}
				return result;
			}
		});
	}

	/**
	 * 获取Map缓存
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getMap(final String key) {
		return (Map<String, String>) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Map<String, String> value = jedisCluster.hgetAll(key);
				if (logger.isDebugEnabled()) {
					logger.debug("getMap {} = {}", key, value);
				}
				return value;
			}
		});
	}

	/**
	 * 获取Map缓存
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getObjectMap(final String key) {
		return (Map<String, Object>) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Map<String, Object> value = Maps.newHashMap();
				Map<byte[], byte[]> map = jedisCluster.hgetAll(getBytesKey(key));
				for (Map.Entry<byte[], byte[]> e : map.entrySet()) {
					value.put(StringUtils.toString(e.getKey()), toObject(e.getValue()));
				}
				if (logger.isDebugEnabled()) {
					logger.debug("getObjectMap {} = {}", key, value);
				}
				return value;
			}
		});
	}

	/**
	 * 设置Map缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param cacheSeconds
	 *            超时时间，0为不超时
	 * @return
	 */
	public String setMap(final String key, final Map<String, String> value, final int cacheSeconds) {
		return (String) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				String result = jedisCluster.hmset(key, value);
				if (cacheSeconds != 0) {
					jedisCluster.expire(key, cacheSeconds);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("setMap {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 设置Map缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param cacheSeconds
	 *            超时时间，0为不超时
	 * @return
	 */
	public String setObjectMap(final String key, final Map<String, Object> value, final int cacheSeconds) {
		return (String) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Map<byte[], byte[]> map = Maps.newHashMap();
				for (Map.Entry<String, Object> e : value.entrySet()) {
					map.put(getBytesKey(e.getKey()), toBytes(e.getValue()));
				}
				String result = jedisCluster.hmset(getBytesKey(key), (Map<byte[], byte[]>) map);
				if (cacheSeconds != 0) {
					jedisCluster.expire(key, cacheSeconds);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("setObjectMap {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 向Map缓存中添加值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public String mapPut(final String key, final Map<String, String> value) {
		return (String) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				String result = jedisCluster.hmset(key, value);
				if (logger.isDebugEnabled()) {
					logger.debug("mapPut {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 向Map缓存中添加值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public String mapObjectPut(final String key, final Map<String, Object> value) {
		return (String) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				String result = null;
				Map<byte[], byte[]> map = Maps.newHashMap();
				for (Map.Entry<String, Object> e : value.entrySet()) {
					map.put(getBytesKey(e.getKey()), toBytes(e.getValue()));
				}
				result = jedisCluster.hmset(getBytesKey(key), map);
				if (logger.isDebugEnabled()) {
					logger.debug("mapObjectPut {} = {}", key, value);
				}
				return result;
			}
		});
	}

	/**
	 * 移除Map缓存中的值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public Long mapRemove(final String key, final String mapKey) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = jedisCluster.hdel(key, mapKey);
				if (logger.isDebugEnabled()) {
					logger.debug("mapRemove {}  {}", key, mapKey);
				}
				return result;
			}
		});
	}

	/**
	 * 移除Map缓存中的值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public Long mapObjectRemove(final String key, final String mapKey) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = jedisCluster.hdel(getBytesKey(key), getBytesKey(mapKey));
				if (logger.isDebugEnabled()) {
					logger.debug("mapObjectRemove {}  {}", key, mapKey);
				}
				return result;
			}
		});
	}

	/**
	 * 判断Map缓存中的Key是否存在
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public Boolean mapExists(final String key, final String mapKey) {
		return (Boolean) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Boolean result = jedisCluster.hexists(key, mapKey);
				if (logger.isDebugEnabled()) {
					logger.debug("mapObjectExists {}  {}", key, mapKey);
				}
				return result;
			}
		});
	}

	/**
	 * 判断Map缓存中的Key是否存在
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return
	 */
	public Boolean mapObjectExists(final String key, final String mapKey) {
		return (Boolean) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Boolean result = jedisCluster.hexists(getBytesKey(key), getBytesKey(mapKey));
				if (logger.isDebugEnabled()) {
					logger.debug("mapObjectExists {}  {}", key, mapKey);
				}
				return result;
			}
		});
	}

	/**
	 * 删除缓存
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public Long del(final String key) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Long result = jedisCluster.del(key);
				if (logger.isDebugEnabled()) {
					logger.debug("del {}", key);
				}
				return result;
			}
		});
	}

	/**
	 * 删除缓存
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public Long delObject(final String key) {
		return (Long) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				long result = jedisCluster.del(getBytesKey(key));
				if (logger.isDebugEnabled()) {
					logger.debug("delObject {}", key);
				}
				return result;
			}
		});
	}

	/**
	 * 缓存是否存在
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public Boolean exists(final String key) {
		return (Boolean) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				Boolean result = jedisCluster.exists(key);
				if (logger.isDebugEnabled())
					logger.debug("exists {}", key);
				return result;
			}
		});
	}

	/**
	 * 缓存是否存在
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public Boolean existsObject(final String key) {
		return (Boolean) execute(new Callback() {
			@Override
			public Object call(JedisCluster jedisCluster) {
				boolean result = jedisCluster.exists(getBytesKey(key));
				if (logger.isDebugEnabled()) {
					logger.debug("existsObject {}", key);
				}
				return result;
			}
		});
	}

	/**
	 * 获取byte[]类型Key
	 * 
	 * @param key
	 * @return
	 */
	public byte[] getBytesKey(Object object) {
		if (object instanceof String) {
			return StringUtils.getBytes((String) object);
		} else {
			return ObjectUtils.serialize(object);
		}
	}

	/**
	 * Object转换byte[]类型
	 * 
	 * @param key
	 * @return
	 */
	public byte[] toBytes(Object object) {
		return ObjectUtils.serialize(object);
	}

	/**
	 * byte[]型转换Object
	 * 
	 * @param key
	 * @return
	 */
	public Object toObject(byte[] bytes) {
		return ObjectUtils.unserialize(bytes);
	}

	/**
	 * Commons executions.
	 * 
	 * @param callback
	 * @return
	 */
	private Object execute(Callback callback) {
		try {
			return callback.call(jedisCluster);
		} catch (Throwable t) {
			logger.error("Redis processing fail.", t);
			throw t;
		} finally {
			// Redis cluster mode does not need to display the release of
			// resources.
		}
	}

	public static abstract interface Callback {
		public Object call(JedisCluster jedisCluster);
	}
}
