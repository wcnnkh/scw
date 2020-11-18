package scw.data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import scw.core.utils.CollectionUtils;
import scw.io.SerializerUtils;

@SuppressWarnings("unchecked")
public abstract class AbstractMapCache implements Cache {
	public abstract Map<String, Object> getMap();

	protected abstract Map<String, Object> createMap();
	
	protected Object cloneValue(Object value){
		return SerializerUtils.clone(value);
	}

	public <T> T get(String key) {
		Map<String, Object> map = getMap();
		if (map == null) {
			return null;
		}

		return (T) map.get(key);
	}

	public <T> Map<String, T> get(Collection<String> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return null;
		}

		Map<String, Object> map = getMap();
		if (map == null) {
			return null;
		}

		Map<String, T> valueMap = new LinkedHashMap<String, T>(keys.size());
		for (String key : keys) {
			Object v = map.get(key);
			if (v == null) {
				continue;
			}

			valueMap.put(key, (T) v);
		}
		return valueMap;
	}

	public boolean add(String key, Object value) {
		Map<String, Object> map = getMap();
		if (map == null) {
			map = createMap();
		}

		if (map == null) {
			return false;
		}

		return map.putIfAbsent(key, cloneValue(value)) == null;
	}

	public void set(String key, Object value) {
		Map<String, Object> map = getMap();
		if (map == null) {
			map = createMap();
		}

		if (map == null) {
			return;
		}

		map.put(key, cloneValue(value));
	}

	public boolean isExist(String key) {
		Map<String, Object> map = getMap();
		if (map == null) {
			return false;
		}

		return map.containsKey(key);
	}

	public boolean delete(String key) {
		Map<String, Object> map = getMap();
		if (map == null) {
			return false;
		}

		return map.remove(key) != null;
	}

	public void delete(Collection<String> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return;
		}

		for (String key : keys) {
			delete(key);
		}
	}
}
