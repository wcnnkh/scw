package scw.redis.jedis.cluster;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.JedisCluster;
import scw.redis.AbstractRedisOperations;
import scw.redis.RedisUtils;
import scw.redis.enums.EXPX;
import scw.redis.enums.NXXX;
import scw.redis.jedis.JedisUtils;
import scw.value.AnyValue;

public final class JedisClusterBinaryOperations extends AbstractRedisOperations<byte[], byte[]> {
	private final JedisClusterResourceFactory jedisClusterResourceFactory;

	public JedisClusterBinaryOperations(JedisClusterResourceFactory jedisClusterResourceFactory) {
		this.jedisClusterResourceFactory = jedisClusterResourceFactory;
	}

	public byte[] get(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.get(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public void set(byte[] key, byte[] value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			jedisCluster.set(key, value);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public boolean setnx(byte[] key, byte[] value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.setnx(key, value) == 1;
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public void setex(byte[] key, int seconds, byte[] value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			jedisCluster.setex(key, seconds, value);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public boolean exists(byte[] key) {
		JedisCluster jedisCluster = null;
		Boolean b;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			b = jedisCluster.exists(key);
			return b == null ? false : b;
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long expire(byte[] key, int seconds) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.expire(key, seconds);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public boolean del(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.del(key) == 1;
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long hset(byte[] key, byte[] field, byte[] value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.del(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hsetnx(key, field, value);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long hdel(byte[] key, byte[]... fields) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hdel(key, fields);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Boolean hexists(byte[] key, byte[] field) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hexists(key, field);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long ttl(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.ttl(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long incr(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.incr(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long decr(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.decr(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Collection<byte[]> hvals(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hvals(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public byte[] hget(byte[] key, byte[] field) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hget(key, field);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Collection<byte[]> hmget(byte[] key, byte[]... fields) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hmget(key, fields);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long lpush(byte[] key, byte[]... values) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.lpush(key, values);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long rpush(byte[] key, byte[]... values) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.rpush(key, values);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public byte[] rpop(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.rpop(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public byte[] lpop(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.lpop(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Set<byte[]> smembers(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.smembers(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long srem(byte[] key, byte[]... members) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.srem(key, members);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long sadd(byte[] key, byte[]... members) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.sadd(key, members);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long zadd(byte[] key, long score, byte[] member) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.zadd(key, score, member);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Boolean set(byte[] key, byte[] value, NXXX nxxx, EXPX expx, long time) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return JedisUtils.isOK(jedisCluster.set(key, value, JedisUtils.parseSetParams(nxxx, expx, time)));
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Boolean sIsMember(byte[] key, byte[] member) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.sismember(key, member);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public byte[] lindex(byte[] key, int index) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.lindex(key, index);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long llen(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.llen(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public AnyValue[] eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return RedisUtils.wrapper(jedisCluster.eval(script, keys, args));
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Map<byte[], byte[]> hgetAll(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hgetAll(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public List<byte[]> brpop(int timeout, byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.brpop(timeout, key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public List<byte[]> blpop(int timeout, byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.blpop(timeout, key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public List<byte[]> mget(byte[]... keys) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.mget(keys);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long hlen(byte[] key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hlen(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Boolean hmset(byte[] key, Map<byte[], byte[]> hash) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return JedisUtils.isOK(jedisCluster.hmset(key, hash));
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	@Override
	protected Collection<byte[]> mget(Collection<byte[]> keys) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.mget(keys.toArray(new byte[0][]));
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public long incr(byte[] key, long delta) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.incrBy(key, delta);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public long decr(byte[] key, long delta) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.decrBy(key, delta);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}
}
