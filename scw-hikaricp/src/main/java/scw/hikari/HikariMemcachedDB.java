package scw.hikari;

import scw.core.annotation.Order;
import scw.memcached.Memcached;
import scw.sql.orm.cache.TemporaryCacheManager;
import scw.sql.orm.support.generation.MemcachedGeneratorService;

public class HikariMemcachedDB extends HikariDB {
	
	public HikariMemcachedDB(String configLocation) {
		super(configLocation);
	}
	
	@Order
	public HikariMemcachedDB(String configLocation, Memcached memcached) {
		super(configLocation);
		setCacheManager(new TemporaryCacheManager(memcached, true, getCachePrefix()));
		setGeneratorService(new MemcachedGeneratorService(memcached));
	}
}
