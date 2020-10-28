package scw.redis.jedis;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import scw.redis.AbstractRedisOperations;
import scw.redis.RedisUtils;
import scw.redis.enums.EXPX;
import scw.redis.enums.NXXX;
import scw.value.AnyValue;

public final class JedisBinaryOperations extends AbstractRedisOperations<byte[], byte[]> {
	private final JedisResourceFactory jedisResourceFactory;

	public JedisBinaryOperations(JedisResourceFactory jedisResourceFactory) {
		this.jedisResourceFactory = jedisResourceFactory;
	}

	public byte[] get(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.get(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public void set(byte[] key, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			jedis.set(key, value);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public boolean setnx(byte[] key, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.setnx(key, value) == 1;
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public void setex(byte[] key, int seconds, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			jedis.setex(key, seconds, value);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public boolean exists(byte[] key) {
		Jedis jedis = null;
		Boolean b;
		try {
			jedis = jedisResourceFactory.getResource();
			b = jedis.exists(key);
			return b == null ? false : b;
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long expire(byte[] key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.expire(key, seconds);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public boolean del(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.del(key) == 1;
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long hset(byte[] key, byte[] field, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hset(key, field, value);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hsetnx(key, field, value);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long hdel(byte[] key, byte[]... fields) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hdel(key, fields);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Boolean hexists(byte[] key, byte[] field) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hexists(key, field);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long ttl(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.ttl(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long incr(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.incr(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long decr(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.decr(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Collection<byte[]> hvals(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hvals(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public byte[] hget(byte[] key, byte[] field) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hget(key, field);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Collection<byte[]> hmget(byte[] key, byte[]... fields) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hmget(key, fields);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long lpush(byte[] key, byte[]... values) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.lpush(key, values);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long rpush(byte[] key, byte[]... values) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.rpush(key, values);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public byte[] rpop(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.rpop(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public byte[] lpop(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.lpop(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Set<byte[]> smembers(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.smembers(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long srem(byte[] key, byte[]... members) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.srem(key, members);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long sadd(byte[] key, byte[]... members) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.sadd(key, members);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long zadd(byte[] key, long score, byte[] member) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.zadd(key, score, member);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Boolean set(byte[] key, byte[] value, NXXX nxxx, EXPX expx, long time) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return JedisUtils.isOK(jedis.set(key, value, JedisUtils.parseSetParams(nxxx, expx, time)));
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Boolean sIsMember(byte[] key, byte[] member) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.sismember(key, member);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public byte[] lindex(byte[] key, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return lindex(key, index);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long llen(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.llen(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	@SuppressWarnings({ "unchecked" })
	public AnyValue[] eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			Object value = jedis.eval(script, keys == null ? Collections.EMPTY_LIST : keys,
					args == null ? Collections.EMPTY_LIST : args);
			return RedisUtils.wrapper(value);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Map<byte[], byte[]> hgetAll(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hgetAll(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public List<byte[]> brpop(int timeout, byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.brpop(timeout, key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public List<byte[]> blpop(int timeout, byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.blpop(timeout, key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public List<byte[]> mget(Collection<byte[]> keys) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.mget(keys.toArray(new byte[0][]));
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long hlen(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hlen(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Boolean hmset(byte[] key, Map<byte[], byte[]> hash) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return JedisUtils.isOK(jedis.hmset(key, hash));
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public long incr(byte[] key, long delta) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.incrBy(key, delta);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public long decr(byte[] key, long delta) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.decrBy(key, delta);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}
}
