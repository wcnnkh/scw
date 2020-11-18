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

public final class JedisStringOperations extends AbstractRedisOperations<String, String>{
	private final JedisResourceFactory jedisResourceFactory;

	public JedisStringOperations(JedisResourceFactory jedisResourceFactory){
		this.jedisResourceFactory = jedisResourceFactory;
	}
	
	public String get(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.get(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public void set(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			JedisUtils.isOK(jedis.set(key, value));
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public boolean setnx(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.setnx(key, value) == 1;
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public void setex(String key, int seconds, String value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			jedis.setex(key, seconds, value);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public boolean exists(String key) {
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

	public Long expire(String key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.expire(key, seconds);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public boolean del(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.del(key) == 1;
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long hset(String key, String field, String value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hset(key, field, value);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long hsetnx(String key, String field, String value) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hsetnx(key, field, value);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long hdel(String key, String... fields) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hdel(key, fields);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Boolean hexists(String key, String field) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hexists(key, field);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long ttl(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.ttl(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long incr(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.incr(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long decr(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.decr(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Collection<String> hvals(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hvals(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public String hget(String key, String field) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hget(key, field);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Collection<String> hmget(String key, String... fields) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hmget(key, fields);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long lpush(String key, String... values) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.lpush(key, values);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long rpush(String key, String... values) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.rpush(key, values);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public String rpop(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.rpop(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public String lpop(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.lpop(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Set<String> smembers(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.smembers(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long srem(String key, String... members) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.srem(key, members);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long sadd(String key, String... members) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.sadd(key, members);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long zadd(String key, long score, String member) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.zadd(key, score, member);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Boolean set(String key, String value, NXXX nxxx, EXPX expx, long time) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return JedisUtils.isOK(jedis.set(key, value, JedisUtils.parseSetParams(nxxx, expx, time)));
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Boolean sIsMember(String key, String member) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.sismember(key, member);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public String lindex(String key, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return lindex(key, index);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long llen(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.llen(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	@SuppressWarnings("unchecked")
	public AnyValue[] eval(String script, List<String> keys, List<String> args) {
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

	public Map<String, String> hgetAll(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hgetAll(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public List<String> brpop(int timeout, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.brpop(timeout, key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public List<String> blpop(int timeout, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.blpop(timeout, key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public List<String> mget(Collection<String> keys) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.mget(keys.toArray(new String[0]));
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Long hlen(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.hlen(key);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public Boolean hmset(String key, Map<String, String> hash) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return JedisUtils.isOK(jedis.hmset(key, hash));
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public long incr(String key, long delta) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.incrBy(key, delta);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}

	public long decr(String key, long delta) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			return jedis.decrBy(key, delta);
		} finally {
			jedisResourceFactory.release(jedis);
		}
	}
}
