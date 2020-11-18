package scw.sql.orm.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scw.aop.ProxyUtils;
import scw.core.utils.ArrayUtils;
import scw.core.utils.XTime;
import scw.data.TemporaryCache;
import scw.data.WrapperTemporaryCache;
import scw.sql.orm.cache.annotation.TemporaryCacheEnable;

public class TemporaryCacheManager extends AbstractCacheManager<TemporaryCache> {
	private static final String KEY = "key:";
	private static final TemporaryCacheConfig DEFAULT_CONFIG = new TemporaryCacheConfig(
			(int) (XTime.ONE_DAY * 2 / 1000), false, true);
	private static volatile Map<Class<?>, TemporaryCacheConfig> configMap = new HashMap<Class<?>, TemporaryCacheConfig>();

	private static final TemporaryCacheConfig getCacheConfig(Class<?> tableClass) {
		TemporaryCacheConfig config = configMap.get(tableClass);
		if (config == null) {
			synchronized (configMap) {
				config = configMap.get(tableClass);
				if (config == null) {
					TemporaryCacheEnable temporaryCacheEnable = tableClass
							.getAnnotation(TemporaryCacheEnable.class);
					if (temporaryCacheEnable == null) {
						config = DEFAULT_CONFIG;
					} else {
						config = new TemporaryCacheConfig(temporaryCacheEnable);
					}
					configMap.put(tableClass, config);
				}
			}
		}
		return config;
	}

	private final TemporaryCache cache;

	public TemporaryCacheManager(TemporaryCache cache, boolean transaction,
			String keyPrefix) {
		this.cache = new WrapperTemporaryCache(cache, transaction, keyPrefix);
	}

	public void save(Object bean) {
		Class<?> clazz = ProxyUtils.getProxyFactory().getUserClass(
				bean.getClass());
		TemporaryCacheConfig config = getCacheConfig(clazz);
		if (!config.isEnable()) {
			return;
		}

		String objectKey = getObjectRelationalMapping().getObjectKey(clazz,
				bean);
		cache.set(objectKey, config.getExp(), bean);
		if (config.isKeys()) {
			cache.add(KEY + objectKey, "");
		}
	}

	public void update(Object bean) {
		Class<?> clazz = ProxyUtils.getProxyFactory().getUserClass(
				bean.getClass());
		TemporaryCacheConfig config = getCacheConfig(clazz);
		if (!config.isEnable()) {
			return;
		}

		cache.set(getObjectRelationalMapping().getObjectKey(clazz, bean),
				config.getExp(), bean);
	}

	public void delete(Object bean) {
		Class<?> clazz = ProxyUtils.getProxyFactory().getUserClass(
				bean.getClass());
		TemporaryCacheConfig config = getCacheConfig(clazz);
		if (!config.isEnable()) {
			return;
		}

		String objectKey = getObjectRelationalMapping().getObjectKey(clazz,
				bean);
		if (config.isKeys()) {
			cache.delete(KEY + objectKey);
		}
		cache.delete(objectKey);
	}

	public void deleteById(Class<?> type, Object... params) {
		TemporaryCacheConfig config = getCacheConfig(type);
		if (!config.isEnable()) {
			return;
		}

		String objectKey = getObjectRelationalMapping().getObjectKeyById(type,
				Arrays.asList(params));
		if (config.isKeys()) {
			cache.delete(KEY + objectKey);
		}
		cache.delete(objectKey);
	}

	public void saveOrUpdate(Object bean) {
		Class<?> clazz = ProxyUtils.getProxyFactory().getUserClass(
				bean.getClass());
		TemporaryCacheConfig config = getCacheConfig(clazz);
		if (!config.isEnable()) {
			return;
		}

		String objectKey = getObjectRelationalMapping().getObjectKey(clazz,
				bean);
		cache.set(objectKey, config.getExp(), bean);
		if (config.isKeys()) {
			cache.set(KEY + objectKey, "");
		}
	}

	public <T> T getById(Class<? extends T> type, Object... params) {
		TemporaryCacheConfig config = getCacheConfig(type);
		if (!config.isEnable()) {
			return null;
		}

		return cache.getAndTouch(
				getObjectRelationalMapping().getObjectKeyById(type,
						Arrays.asList(params)), config.getExp());
	}

	public <K, V> Map<K, V> getInIdList(Class<? extends V> type,
			Collection<? extends K> inIds, Object... params) {
		TemporaryCacheConfig config = getCacheConfig(type);
		if (!config.isEnable()) {
			return null;
		}

		return super.getInIdList(type, inIds, params);
	}

	@Override
	public TemporaryCache getCache() {
		return cache;
	}

	@Override
	public boolean isSearchDB(Class<?> type, Object... params) {
		if (ArrayUtils.isEmpty(params)) {
			return false;
		}

		TemporaryCacheConfig config = getCacheConfig(type);
		if (config.isEnable() && config.isKeys()) {
			if (getObjectRelationalMapping().getColumns(type).getPrimaryKeys().size() != params.length) {
				return true;
			}

			String key = getObjectRelationalMapping().getObjectKeyById(type,
					Arrays.asList(params));
			return getCache().isExist(KEY + key);
		}
		return true;
	}
}
