package scw.sql.orm.cache;

import java.util.Arrays;

import scw.aop.ProxyUtils;
import scw.core.utils.ArrayUtils;
import scw.data.Cache;
import scw.data.TransactionContextCache;
import scw.data.WrapperCache;

public final class DefaultCacheManager extends AbstractCacheManager<Cache> {
	private final Cache cache;

	/**
	 * 过期时间由cache实现
	 * 
	 * @param cache
	 * @param transaction
	 *            是否开启事务， 如果开启在处理失败后会删除key
	 * @param keyPrefix
	 */
	public DefaultCacheManager(Cache cache, boolean transaction, String keyPrefix) {
		this.cache = new WrapperCache(cache, transaction, keyPrefix);
	}

	public DefaultCacheManager() {
		this.cache = new TransactionContextCache(this);
	}

	public void save(Object bean) {
		cache.add(getObjectRelationalMapping().getObjectKey(ProxyUtils.getProxyFactory().getUserClass(bean.getClass()), bean), bean);
	}

	public void update(Object bean) {
		cache.set(getObjectRelationalMapping().getObjectKey(ProxyUtils.getProxyFactory().getUserClass(bean.getClass()), bean), bean);
	}

	public void saveOrUpdate(Object bean) {
		update(bean);
	}

	public <T> T getById(Class<? extends T> type, Object... params) {
		if (ArrayUtils.isEmpty(params)) {
			return null;
		}

		return cache.get(getObjectRelationalMapping().getObjectKeyById(type, Arrays.asList(params)));
	}

	@Override
	public Cache getCache() {
		return cache;
	}

	public void deleteById(Class<?> type, Object... params) {
		if (ArrayUtils.isEmpty(params)) {
			return;
		}

		getCache().delete(getObjectRelationalMapping().getObjectKeyById(type, Arrays.asList(params)));
	}

	public void delete(Object bean) {
		getCache()
				.delete(getObjectRelationalMapping().getObjectKey(ProxyUtils.getProxyFactory().getUserClass(bean.getClass()), bean));
	}
}
