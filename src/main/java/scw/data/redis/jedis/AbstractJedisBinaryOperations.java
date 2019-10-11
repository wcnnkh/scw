package scw.data.redis.jedis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import scw.data.redis.ResourceManager;
import scw.data.redis.enums.EXPX;
import scw.data.redis.enums.NXXX;
import scw.data.redis.operations.AbstractBinaryRedisOperations;

public abstract class AbstractJedisBinaryOperations extends AbstractBinaryRedisOperations
		implements ResourceManager<Jedis> {

	public byte[] get(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.get(key);
		} finally {
			close(jedis);
		}
	}

	public void set(byte[] key, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			jedis.set(key, value);
		} finally {
			close(jedis);
		}
	}

	public boolean setnx(byte[] key, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.setnx(key, value) == 1;
		} finally {
			close(jedis);
		}
	}

	public void setex(byte[] key, int seconds, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			jedis.setex(key, seconds, value);
		} finally {
			close(jedis);
		}
	}

	public boolean exists(byte[] key) {
		Jedis jedis = null;
		Boolean b;
		try {
			jedis = getResource();
			b = jedis.exists(key);
			return b == null ? false : b;
		} finally {
			close(jedis);
		}
	}

	public Long expire(byte[] key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.expire(key, seconds);
		} finally {
			close(jedis);
		}
	}

	public boolean del(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.del(key) == 1;
		} finally {
			close(jedis);
		}
	}

	public Long hset(byte[] key, byte[] field, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.hset(key, field, value);
		} finally {
			close(jedis);
		}
	}

	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.hsetnx(key, field, value);
		} finally {
			close(jedis);
		}
	}

	public Long hdel(byte[] key, byte[]... fields) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.hdel(key, fields);
		} finally {
			close(jedis);
		}
	}

	public Boolean hexists(byte[] key, byte[] field) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.hexists(key, field);
		} finally {
			close(jedis);
		}
	}

	public Long ttl(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.ttl(key);
		} finally {
			close(jedis);
		}
	}

	public Long incr(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.incr(key);
		} finally {
			close(jedis);
		}
	}

	public Long decr(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.decr(key);
		} finally {
			close(jedis);
		}
	}

	public Collection<byte[]> hvals(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.hvals(key);
		} finally {
			close(jedis);
		}
	}

	public byte[] hget(byte[] key, byte[] field) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.hget(key, field);
		} finally {
			close(jedis);
		}
	}

	public Collection<byte[]> hmget(byte[] key, byte[]... fields) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.hmget(key, fields);
		} finally {
			close(jedis);
		}
	}

	public Long lpush(byte[] key, byte[]... values) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.lpush(key, values);
		} finally {
			close(jedis);
		}
	}

	public Long rpush(byte[] key, byte[]... values) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.rpush(key, values);
		} finally {
			close(jedis);
		}
	}

	public byte[] rpop(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.rpop(key);
		} finally {
			close(jedis);
		}
	}

	public byte[] lpop(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.lpop(key);
		} finally {
			close(jedis);
		}
	}

	public Set<byte[]> smembers(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.smembers(key);
		} finally {
			close(jedis);
		}
	}

	public Long srem(byte[] key, byte[]... members) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.srem(key, members);
		} finally {
			close(jedis);
		}
	}

	public Long sadd(byte[] key, byte[]... members) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.sadd(key, members);
		} finally {
			close(jedis);
		}
	}

	public Long zadd(byte[] key, long score, byte[] member) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.zadd(key, score, member);
		} finally {
			close(jedis);
		}
	}

	public Boolean set(byte[] key, byte[] value, NXXX nxxx, EXPX expx, long time) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return JedisUtils.isOK(jedis.set(key, value, JedisUtils.parseSetParams(nxxx, expx, time)));
		} finally {
			close(jedis);
		}
	}

	public Boolean sIsMember(byte[] key, byte[] member) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.sismember(key, member);
		} finally {
			close(jedis);
		}
	}

	public byte[] lindex(byte[] key, int index) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return lindex(key, index);
		} finally {
			close(jedis);
		}
	}

	public Long llen(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.llen(key);
		} finally {
			close(jedis);
		}
	}

	@SuppressWarnings("unchecked")
	public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.eval(script, keys == null ? Collections.EMPTY_LIST : keys,
					args == null ? Collections.EMPTY_LIST : args);
		} finally {
			close(jedis);
		}
	}

	public Map<byte[], byte[]> hgetAll(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.hgetAll(key);
		} finally {
			close(jedis);
		}
	}

	public List<byte[]> brpop(int timeout, byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.brpop(timeout, key);
		} finally {
			close(jedis);
		}
	}

	public List<byte[]> blpop(int timeout, byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.blpop(timeout, key);
		} finally {
			close(jedis);
		}
	}

	public List<byte[]> mget(byte[]... keys) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.mget(keys);
		} finally {
			close(jedis);
		}
	}

	public Long hlen(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return jedis.hlen(key);
		} finally {
			close(jedis);
		}
	}

	public Boolean hmset(byte[] key, Map<byte[], byte[]> hash) {
		Jedis jedis = null;
		try {
			jedis = getResource();
			return JedisUtils.isOK(jedis.hmset(key, hash));
		} finally {
			close(jedis);
		}
	}

	public Map<byte[], byte[]> mget(Collection<byte[]> keys) {
		if (keys == null || keys.isEmpty()) {
			return null;
		}

		List<byte[]> list = mget(keys.toArray(new byte[keys.size()][]));
		if (list == null || list.isEmpty()) {
			return null;
		}

		Map<byte[], byte[]> map = new HashMap<byte[], byte[]>(keys.size(), 1);
		Iterator<byte[]> keyIterator = keys.iterator();
		Iterator<byte[]> valueIterator = list.iterator();
		while (keyIterator.hasNext() && valueIterator.hasNext()) {
			byte[] key = keyIterator.next();
			byte[] value = valueIterator.next();
			if (key == null || value == null) {
				continue;
			}

			map.put(key, value);
		}
		return map;
	}
}
