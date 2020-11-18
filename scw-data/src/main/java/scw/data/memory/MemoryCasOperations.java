package scw.data.memory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import scw.core.utils.CollectionUtils;
import scw.data.cas.CAS;
import scw.data.cas.CASOperations;

public class MemoryCasOperations implements CASOperations {
	private final MemoryDataManager memoryDataManager;

	public MemoryCasOperations(MemoryDataManager memoryDataManager) {
		this.memoryDataManager = memoryDataManager;
	}

	public boolean cas(String key, Object value, int exp, long cas) {
		MemoryData memoryData = memoryDataManager.createDefaultMemoryCache(key);
		if (memoryData.set(new CAS<Object>(cas, value))) {
			memoryData.setExpire(exp);
			return true;
		}
		return false;
	}

	public <T> CAS<T> get(String key) {
		MemoryData memoryData = memoryDataManager.getMemoryCache(key);
		if (memoryData == null) {
			return null;
		}

		return memoryData.get();
	}

	public void set(String key, Object value, int exp) {
		MemoryData memoryData = memoryDataManager.createDefaultMemoryCache(key);
		memoryData.set(value);
		memoryData.setExpire(exp);
	}

	public boolean add(String key, Object value, int exp) {
		MemoryData memoryData = memoryDataManager.createDefaultMemoryCache(key);
		if (memoryData.setIfAbsent(value)) {
			memoryData.setExpire(exp);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public <T> Map<String, CAS<T>> get(Collection<String> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return Collections.EMPTY_MAP;
		}

		Map<String, CAS<T>> map = new LinkedHashMap<String, CAS<T>>(keys.size());
		for (String key : keys) {
			CAS<T> value = get(key);
			if (value == null) {
				continue;
			}

			map.put(key, value);
		}
		return map;
	}

	public boolean delete(String key, long cas) {
		return memoryDataManager.delete(key, cas);
	}

	public boolean delete(String key) {
		return memoryDataManager.delete(key);
	}

}
