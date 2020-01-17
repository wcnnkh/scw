package scw.generator.id;

import scw.data.redis.Redis;

public final class RedisIdFactory implements IdFactory<Long> {
	private final Redis redis;

	public RedisIdFactory(Redis redis) {
		this.redis = redis;
	}

	public Long generator(String name) {
		return redis.getStringOperations().incr(this.getClass().getName() + "#" + name);
	}
}