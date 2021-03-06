package scw.redis.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import scw.redis.core.RedisConnection;
import scw.redis.core.RedisConnectionFactory;

public class JedisConnectionFactory implements RedisConnectionFactory<byte[], byte[]> {
	private final JedisPool jedisPool;

	public JedisConnectionFactory(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	@Override
	public RedisConnection<byte[], byte[]> getConnection() {
		Jedis jedis = jedisPool.getResource();
		return new JedisConnection(jedis);
	}

}
