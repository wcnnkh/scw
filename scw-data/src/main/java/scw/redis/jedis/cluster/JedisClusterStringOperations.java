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

public final class JedisClusterStringOperations extends AbstractRedisOperations<String, String> {
	private final JedisClusterResourceFactory jedisClusterResourceFactory;

	public JedisClusterStringOperations(JedisClusterResourceFactory jedisClusterResourceFactory) {
		this.jedisClusterResourceFactory = jedisClusterResourceFactory;
	}

	public String get(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.get(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public void set(String key, String value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			jedisCluster.set(key, value);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public boolean setnx(String key, String value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.setnx(key, value) == 1;
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public void setex(String key, int seconds, String value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			jedisCluster.setex(key, seconds, value);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public boolean exists(String key) {
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

	public Long expire(String key, int seconds) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.expire(key, seconds);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public boolean del(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.del(key) == 1;
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long hset(String key, String field, String value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.del(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long hsetnx(String key, String field, String value) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hsetnx(key, field, value);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long hdel(String key, String... fields) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hdel(key, fields);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Boolean hexists(String key, String field) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hexists(key, field);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long ttl(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.ttl(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long incr(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.incr(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long decr(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.decr(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Collection<String> hvals(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hvals(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public String hget(String key, String field) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hget(key, field);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Collection<String> hmget(String key, String... fields) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hmget(key, fields);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long lpush(String key, String... values) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.lpush(key, values);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long rpush(String key, String... values) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.rpush(key, values);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public String rpop(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.rpop(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public String lpop(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.lpop(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Set<String> smembers(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.smembers(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long srem(String key, String... members) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.srem(key, members);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long sadd(String key, String... members) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.sadd(key, members);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long zadd(String key, long score, String member) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.zadd(key, score, member);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Boolean set(String key, String value, NXXX nxxx, EXPX expx, long time) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return JedisUtils.isOK(jedisCluster.set(key, value, JedisUtils.parseSetParams(nxxx, expx, time)));
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Boolean sIsMember(String key, String member) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.sismember(key, member);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public String lindex(String key, int index) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.lindex(key, index);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long llen(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.llen(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public AnyValue[] eval(String script, List<String> keys, List<String> args) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return RedisUtils.wrapper(jedisCluster.eval(script, keys, args));
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Map<String, String> hgetAll(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hgetAll(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public List<String> brpop(int timeout, String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.brpop(timeout, key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public List<String> blpop(int timeout, String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.blpop(timeout, key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public List<String> mget(String... keys) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.mget(keys);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Long hlen(String key) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.hlen(key);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public Boolean hmset(String key, Map<String, String> hash) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return JedisUtils.isOK(jedisCluster.hmset(key, hash));
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public List<String> mget(Collection<String> keys) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.mget(keys.toArray(new String[0]));
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public long incr(String key, long delta) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.incrBy(key, delta);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}

	public long decr(String key, long delta) {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = jedisClusterResourceFactory.getResource();
			return jedisCluster.decrBy(key, delta);
		} finally {
			jedisClusterResourceFactory.release(jedisCluster);
		}
	}
}
