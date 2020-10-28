package scw.redis;

import java.util.Collection;
import java.util.Map;

import scw.core.utils.CollectionUtils;
import scw.data.DataTemplete;
import scw.redis.enums.EXPX;
import scw.redis.enums.NXXX;

@SuppressWarnings("unchecked")
public class RedisDataTemplete implements DataTemplete {
	private final Redis redis;

	public RedisDataTemplete(Redis redis) {
		this.redis = redis;
	}

	public <T> T get(String key) {
		return (T) redis.getObjectOperations().get(key);
	}

	public <T> T getAndTouch(String key, int newExp) {
		return (T) redis.getObjectOperations().getAndTouch(key, newExp);
	}

	public void set(String key, Object value) {
		redis.getObjectOperations().set(key, value);
	}

	public void set(String key, int exp, Object value) {
		redis.getObjectOperations().setex(key, exp, value);
	}

	public boolean add(String key, Object value) {
		return redis.getObjectOperations().set(key, value, NXXX.NX, EXPX.EX, 0);
	}

	public boolean add(String key, int exp, Object value) {
		return redis.getObjectOperations().set(key, value, NXXX.NX, EXPX.EX, exp);
	}

	public boolean touch(String key, int newExp) {
		Object v = redis.getObjectOperations().getAndTouch(key, newExp);
		return v != null;
	}

	public <T> Map<String, T> get(Collection<String> keyCollections) {
		return (Map<String, T>) redis.getObjectOperations().get(keyCollections);
	}

	public boolean delete(String key) {
		return redis.getObjectOperations().del(key);
	}

	public boolean isExist(String key) {
		return redis.getObjectOperations().exists(key);
	}

	public long incr(String key, long delta) {
		return redis.getObjectOperations().incr(key, delta);
	}

	public long incr(String key, long delta, long initialValue) {
		return RedisUtils.incr(redis.getStringOperations(), key, delta, initialValue, 0);
	}

	public long incr(String key, long delta, long initialValue, int exp) {
		return RedisUtils.incr(redis.getStringOperations(), key, delta, initialValue, exp);
	}

	public long decr(String key, long delta) {
		return redis.getObjectOperations().decr(key, delta);
	}

	public long decr(String key, long delta, long initialValue) {
		return RedisUtils.decr(redis.getStringOperations(), key, delta, initialValue, 0);
	}

	public long decr(String key, long delta, long initialValue, int exp) {
		return RedisUtils.decr(redis.getStringOperations(), key, delta, initialValue, exp);
	}

	public void delete(Collection<String> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return;
		}

		for (String key : keys) {
			delete(key);
		}
	}
}
