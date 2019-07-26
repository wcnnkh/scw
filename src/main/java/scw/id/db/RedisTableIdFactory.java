package scw.id.db;

import scw.core.utils.ClassUtils;
import scw.data.redis.Redis;
import scw.locks.Lock;
import scw.locks.RedisLock;
import scw.sql.orm.ORMOperations;

public final class RedisTableIdFactory extends AbstractTableIdFactory {
	private final Redis redis;

	public RedisTableIdFactory(ORMOperations db, Redis redis) {
		super(db);
		this.redis = redis;
	}

	private String getCacheKey(Class<?> tableClass, String fieldName) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(this.getClass().getName());
		sb.append("&");
		sb.append(ClassUtils.getProxyRealClassName(tableClass));
		sb.append("&");
		sb.append(fieldName);
		return sb.toString();
	}

	public long generator(Class<?> tableClass, String fieldName) {
		String key = getCacheKey(tableClass, fieldName);
		if (!redis.getStringOperations().exists(key)) {
			// 不存在
			Lock lock = new RedisLock(redis, key + "&lock");
			try {
				lock.lockWait();
				if (!redis.getStringOperations().exists(key)) {
					long maxId = getMaxId(tableClass, fieldName);
					return redis.getStringOperations().incr(key, 1, maxId + 1);
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				lock.unlock();
			}
		}
		return redis.getStringOperations().incr(key);
	}

}