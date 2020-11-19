package scw.druid;

import scw.core.annotation.Order;
import scw.redis.Redis;
import scw.sql.orm.cache.TemporaryCacheManager;
import scw.sql.orm.support.generation.RedisGeneratorService;

public class DruidRedisDB extends DruidDB{
	
	public DruidRedisDB(String configLocation) {
		super(configLocation);
	}
	
	@Order
	public DruidRedisDB(String configLocation, Redis redis) {
		super(configLocation);
		setCacheManager(new TemporaryCacheManager(redis, true, getCachePrefix()));
		setGeneratorService(new RedisGeneratorService(redis));
	}
}
