package scw.sql.orm.cache;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import scw.data.Cache;
import scw.sql.orm.ObjectRelationalMapping;
import scw.sql.orm.OrmUtils;

public abstract class AbstractCacheManager<C extends Cache> implements CacheManager {
	public abstract C getCache();

	public ObjectRelationalMapping getObjectRelationalMapping() {
		return OrmUtils.getObjectRelationalMapping();
	}

	public <K, V> Map<K, V> getInIdList(Class<? extends V> type, Collection<? extends K> inIds, Object... params) {
		if (params.length != getObjectRelationalMapping().getColumns(type).getPrimaryKeys().size() - 1) {
			return null;
		}

		Map<String, K> keyMap = getObjectRelationalMapping().getInIdKeyMap(type, inIds, params);
		Map<String, V> map = getCache().get(keyMap.keySet());
		if (map == null || map.isEmpty()) {
			return null;
		}

		Map<K, V> valueMap = new LinkedHashMap<K, V>(map.size(), 1);
		for (Entry<String, V> entry : map.entrySet()) {
			K k = keyMap.get(entry.getKey());
			if (k == null) {
				continue;
			}

			valueMap.put(k, entry.getValue());
		}

		for (Entry<String, K> entry : keyMap.entrySet()) {
			if (valueMap.containsKey(entry.getValue())) {
				continue;
			}

			if (getCache().isExist(entry.getKey())) {
				valueMap.put(entry.getValue(), null);
			}
		}
		return valueMap;
	}

	public boolean isSearchDB(Class<?> type, Object... params) {
		return true;
	}
}
