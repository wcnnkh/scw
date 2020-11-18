package scw.redis.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import scw.util.ResourceFactory;

public class JedisResourceFactory implements ResourceFactory<Jedis> {
	private JedisPool jedisPool;

	public JedisResourceFactory(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public Jedis getResource() {
		return jedisPool.getResource();
	}

	public void release(Jedis resource) {
		resource.close();
	}
}
