package scw.data.memory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import scw.core.utils.CollectionUtils;
import scw.data.DataTemplete;
import scw.data.cas.CAS;
import scw.data.cas.CASOperations;

@SuppressWarnings("unchecked")
public final class MemoryDataTemplete implements DataTemplete {
	private final MemoryDataManager memoryDataManager;
	private final MemoryCasOperations memoryCasOperations;
	
	public MemoryDataTemplete(){
		this(new MemoryDataManager());
	}

	public MemoryDataTemplete(MemoryDataManager memoryDataManager) {
		this.memoryDataManager = memoryDataManager;
		this.memoryCasOperations = new MemoryCasOperations(memoryDataManager);
	}

	public <T> T get(String key) {
		MemoryData memoryData = memoryDataManager.getMemoryCache(key);
		if (memoryData == null) {
			return null;
		}

		CAS<T> cas = memoryData.get();
		return cas == null ? null : cas.getValue();
	}

	public <T> T getAndTouch(String key, int newExp) {
		MemoryData memoryData = memoryDataManager.getMemoryCache(key);
		if (memoryData == null) {
			return null;
		}

		memoryData.setExpire(newExp);
		memoryData.touch();
		CAS<T> cas = memoryData.get();
		return cas == null ? null : cas.getValue();
	}

	public void set(String key, Object value) {
		MemoryData memoryData = memoryDataManager.createDefaultMemoryCache(key);
		memoryData.set(value);
	}

	public void set(String key, int exp, Object value) {
		MemoryData memoryData = memoryDataManager.createDefaultMemoryCache(key);
		memoryData.set(value);
		memoryData.setExpire(exp);
	}

	public boolean add(String key, Object value) {
		MemoryData memoryData = memoryDataManager.createDefaultMemoryCache(key);
		return memoryData.setIfAbsent(value);
	}

	public boolean add(String key, int exp, Object value) {
		MemoryData memoryData = memoryDataManager.createDefaultMemoryCache(key);
		if (!memoryData.setIfAbsent(value)) {
			return false;
		}

		memoryData.setExpire(exp);
		return true;
	}

	public boolean touch(String key, int exp) {
		MemoryData memoryData = memoryDataManager.getMemoryCache(key);
		if (memoryData == null) {
			return false;
		}

		memoryData.setExpire(exp);
		return true;
	}

	public <T> Map<String, T> get(Collection<String> keyCollections) {
		if (CollectionUtils.isEmpty(keyCollections)) {
			return Collections.EMPTY_MAP;
		}

		Map<String, T> map = new HashMap<String, T>(keyCollections.size());
		for (String key : keyCollections) {
			T value = get(key);
			if (value == null) {
				continue;
			}

			map.put(key, value);
		}
		return map;
	}

	public boolean isExist(String key) {
		return memoryDataManager.getMemoryCache(key) != null;
	}

	public long incr(String key, long delta) {
		return incr(key, delta, 0, 0);
	}

	public long incr(String key, long delta, long initialValue) {
		return incr(key, delta, initialValue, 0);
	}

	public long incr(String key, long delta, long initialValue, int exp) {
		MemoryData memoryData = memoryDataManager.createCounterMemoryCache(key);
		long v = memoryData.incr(delta, initialValue);
		memoryData.setExpire(exp);
		return v;
	}

	public long decr(String key, long delta) {
		return decr(key, delta, 0, 0);
	}

	public long decr(String key, long delta, long initialValue) {
		return decr(key, delta, initialValue, 0);
	}

	public long decr(String key, long delta, long initialValue, int exp) {
		MemoryData memoryData = memoryDataManager.createCounterMemoryCache(key);
		long v = memoryData.decr(-delta, initialValue);
		memoryData.setExpire(exp);
		return v;
	}

	public boolean delete(String key) {
		return memoryDataManager.delete(key);
	}

	public void delete(Collection<String> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return;
		}

		for (String key : keys) {
			delete(key);
		}
	}

	public CASOperations getCASOperations() {
		return memoryCasOperations;
	}
}
