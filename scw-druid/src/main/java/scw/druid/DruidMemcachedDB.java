package scw.druid;

import scw.core.annotation.Order;
import scw.memcached.Memcached;
import scw.sql.orm.cache.TemporaryCacheManager;
import scw.sql.orm.support.generation.MemcachedGeneratorService;

public class DruidMemcachedDB extends DruidDB {
	
	public DruidMemcachedDB(String configLocation) {
		super(configLocation);
	}
	
	@Order
	public DruidMemcachedDB(String configLocation, Memcached memcached) {
		super(configLocation);
		setCacheManager(new TemporaryCacheManager(memcached, true, getCachePrefix()));
		setGeneratorService(new MemcachedGeneratorService(memcached));
	}
}
