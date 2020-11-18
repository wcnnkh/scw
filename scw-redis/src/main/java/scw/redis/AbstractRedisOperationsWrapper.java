package scw.redis;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import scw.core.utils.CollectionUtils;
import scw.redis.enums.EXPX;
import scw.redis.enums.NXXX;
import scw.value.AnyValue;

public abstract class AbstractRedisOperationsWrapper<K, WK, V, WV> implements RedisOperations<K, V> {
	protected abstract WK encodeKey(K key);

	protected abstract K decodeKey(WK key);

	protected abstract WV encodeValue(V value);

	protected abstract V decodeValue(WV value);

	protected abstract RedisOperations<WK, WV> getRedisOperations();

	@SuppressWarnings("unchecked")
	protected WK[] encodeKey(K[] keys) {
		List<WK> list = encodeKey(Arrays.asList(keys));
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}

		return list.toArray((WK[]) Array.newInstance(list.get(0).getClass(), list.size()));
	}

	protected List<WK> encodeKey(Collection<K> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return null;
		}

		List<WK> list = new ArrayList<WK>();
		for (K k : keys) {
			if (k == null) {
				continue;
			}

			list.add(encodeKey(k));
		}

		if (list.isEmpty()) {
			return null;
		}

		return list;
	}

	protected List<V> decodeValue(Collection<WV> values) {
		if (CollectionUtils.isEmpty(values)) {
			return new ArrayList<V>(4);
		}

		List<V> list = new ArrayList<V>();
		for (WV wv : values) {
			if (wv == null) {
				list.add(null);
			} else {
				list.add(decodeValue(wv));
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	protected WV[] encodeValue(V[] values) {
		List<WV> list = encodeValue(Arrays.asList(values));
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}

		return list.toArray((WV[]) Array.newInstance(list.get(0).getClass(), list.size()));
	}

	protected List<WV> encodeValue(Collection<V> values) {
		if (CollectionUtils.isEmpty(values)) {
			return null;
		}

		List<WV> list = new ArrayList<WV>(values.size());
		for (V v : values) {
			if (v == null) {
				continue;
			}

			list.add(encodeValue(v));
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	protected Map<WK, WV> encodeMap(Map<K, V> map) {
		if (CollectionUtils.isEmpty(map)) {
			return Collections.EMPTY_MAP;
		}

		Map<WK, WV> dataMap = new HashMap<WK, WV>();
		for (Entry<K, V> entry : map.entrySet()) {
			dataMap.put(encodeKey(entry.getKey()), encodeValue(entry.getValue()));
		}
		return dataMap;
	}

	protected Map<K, V> decodeMap(Map<WK, WV> map) {
		if (CollectionUtils.isEmpty(map)) {
			return new HashMap<K, V>(4);
		}

		Map<K, V> dataMap = new HashMap<K, V>(map.size());
		for (Entry<WK, WV> entry : map.entrySet()) {
			dataMap.put(decodeKey(entry.getKey()), decodeValue(entry.getValue()));
		}
		return dataMap;
	}

	protected Set<V> decodeValue(Set<WV> values) {
		if (CollectionUtils.isEmpty(values)) {
			return null;
		}

		LinkedHashSet<V> set = new LinkedHashSet<V>(values.size());
		for (WV wv : values) {
			if (wv == null) {
				continue;
			}

			set.add(decodeValue(wv));
		}
		return set;
	}

	public V get(K key) {
		return decodeValue(getRedisOperations().get(encodeKey(key)));
	}

	public void set(K key, V value) {
		getRedisOperations().set(encodeKey(key), encodeValue(value));
	}

	public boolean setnx(K key, V value) {
		return getRedisOperations().setnx(encodeKey(key), encodeValue(value));
	}

	public void setex(K key, int seconds, V value) {
		getRedisOperations().setex(encodeKey(key), seconds, encodeValue(value));
	}

	public boolean exists(K key) {
		return getRedisOperations().exists(encodeKey(key));
	}

	public Long expire(K key, int seconds) {
		return getRedisOperations().expire(encodeKey(key), seconds);
	}

	public boolean del(K key) {
		return getRedisOperations().del(encodeKey(key));
	}

	public Long hset(K key, K field, V value) {
		return getRedisOperations().hset(encodeKey(key), encodeKey(field), encodeValue(value));
	}

	public Long hsetnx(K key, K field, V value) {
		return getRedisOperations().hsetnx(encodeKey(key), encodeKey(field), encodeValue(value));
	}

	public Long hdel(K key, K... fields) {
		WK[] wk = encodeKey(fields);
		if (wk == null) {
			return 0L;
		}

		return getRedisOperations().hdel(encodeKey(key), wk);
	}

	public Long hlen(K key) {
		return getRedisOperations().hlen(encodeKey(key));
	}

	public Boolean hexists(K key, K field) {
		return getRedisOperations().hexists(encodeKey(key), encodeKey(field));
	}

	public Long ttl(K key) {
		return getRedisOperations().ttl(encodeKey(key));
	}

	public Long incr(K key) {
		return getRedisOperations().incr(encodeKey(key));
	}

	public Long decr(K key) {
		return getRedisOperations().decr(encodeKey(key));
	}

	public Collection<V> hvals(K key) {
		return decodeValue(getRedisOperations().hvals(encodeKey(key)));
	}

	public V hget(K key, K field) {
		return decodeValue(getRedisOperations().hget(encodeKey(key), encodeKey(field)));
	}

	public Collection<V> hmget(K key, K... fields) {
		WK[] wk = encodeKey(fields);
		if (wk == null) {
			return new ArrayList<V>(4);
		}

		return decodeValue(getRedisOperations().hmget(encodeKey(key), wk));
	}

	public Long lpush(K key, V... values) {
		WV[] wv = encodeValue(values);
		if (wv == null) {
			return 0L;
		}

		return getRedisOperations().lpush(encodeKey(key), wv);
	}

	public Long rpush(K key, V... values) {
		WV[] wv = encodeValue(values);
		if (wv == null) {
			return 0L;
		}

		return getRedisOperations().rpush(encodeKey(key), wv);
	}

	public V rpop(K key) {
		return decodeValue(getRedisOperations().rpop(encodeKey(key)));
	}

	public V lpop(K key) {
		return decodeValue(getRedisOperations().lpop(encodeKey(key)));
	}

	public Set<V> smembers(K key) {
		return decodeValue(getRedisOperations().smembers(encodeKey(key)));
	}

	public Long srem(K key, V... members) {
		WV[] wv = encodeValue(members);
		if (wv == null) {
			return 0L;
		}

		return getRedisOperations().srem(encodeKey(key), wv);
	}

	public Long sadd(K key, V... members) {
		WV[] wv = encodeValue(members);
		if (wv == null) {
			return 0L;
		}

		return getRedisOperations().sadd(encodeKey(key), wv);
	}

	public Long zadd(K key, long score, V member) {
		return getRedisOperations().zadd(encodeKey(key), score, encodeValue(member));
	}

	public Boolean set(K key, V value, NXXX nxxx, EXPX expx, long time) {
		return getRedisOperations().set(encodeKey(key), encodeValue(value), nxxx, expx, time);
	}

	public Boolean sIsMember(K key, V member) {
		return getRedisOperations().sIsMember(encodeKey(key), encodeValue(member));
	}

	public V lindex(K key, int index) {
		return decodeValue(getRedisOperations().lindex(encodeKey(key), index));
	}

	public Long llen(K key) {
		return getRedisOperations().llen(encodeKey(key));
	}

	public AnyValue[] eval(K script, List<K> keys, List<V> args) {
		return getRedisOperations().eval(encodeKey(script), encodeKey(keys), encodeValue(args));
	}

	public Map<K, V> hgetAll(K key) {
		return decodeMap(getRedisOperations().hgetAll(encodeKey(key)));
	}

	public List<V> brpop(int timeout, K key) {
		return decodeValue(getRedisOperations().brpop(timeout, encodeKey(key)));
	}

	public List<V> blpop(int timeout, K key) {
		return decodeValue(getRedisOperations().blpop(timeout, encodeKey(key)));
	}

	public Boolean hmset(K key, Map<K, V> hash) {
		return getRedisOperations().hmset(encodeKey(key), encodeMap(hash));
	}

	public V getAndTouch(K key, int newExp) {
		return decodeValue(getRedisOperations().getAndTouch(encodeKey(key), newExp));
	}

	public Map<K, V> get(Collection<K> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return new LinkedHashMap<K, V>(4);
		}

		Map<WK, K> keyMap = new HashMap<WK, K>();
		for (K key : keys) {
			if (key == null) {
				continue;
			}

			keyMap.put(encodeKey(key), key);
		}

		Map<WK, WV> map = getRedisOperations().get(keyMap.keySet());
		if (CollectionUtils.isEmpty(map)) {
			return new LinkedHashMap<K, V>();
		}

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Entry<WK, WV> entry : map.entrySet()) {
			K key = keyMap.get(entry.getKey());
			if (key == null) {
				continue;
			}
			result.put(key, decodeValue(entry.getValue()));
		}
		return result;
	}

	public long decr(K key, long delta) {
		return getRedisOperations().decr(encodeKey(key), delta);
	}

	public long incr(K key, long delta) {
		return getRedisOperations().incr(encodeKey(key), delta);
	}
}
