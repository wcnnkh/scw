package scw.hikari;

import scw.core.annotation.Order;
import scw.redis.Redis;
import scw.sql.orm.cache.TemporaryCacheManager;
import scw.sql.orm.support.generation.RedisGeneratorService;

public class HikariRedisDB extends HikariDB {
	
	public HikariRedisDB(String configLocation){
		super(configLocation);
	}
	
	@Order
	public HikariRedisDB(String configLocation, Redis redis) {
		super(configLocation);
		setCacheManager(new TemporaryCacheManager(redis, true, getCachePrefix()));
		setGeneratorService(new RedisGeneratorService(redis));
	}
}
