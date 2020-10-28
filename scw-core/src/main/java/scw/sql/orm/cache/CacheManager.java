package scw.sql.orm.cache;

import java.util.Collection;
import java.util.Map;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface CacheManager {
	void save(Object bean);

	void update(Object bean);

	void delete(Object bean);

	void deleteById(Class<?> type, Object... params);

	void saveOrUpdate(Object bean);

	<T> T getById(Class<? extends T> type, Object... params);

	<K, V> Map<K, V> getInIdList(Class<? extends V> type, Collection<? extends K> inIds, Object... params);

	/**
	 * 是否应该从数据库查找
	 * 
	 * @param type
	 * @param params
	 * @return
	 */
	boolean isSearchDB(Class<?> type, Object... params);
}
