package scw.redis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractRedisOperations<K, V> implements RedisOperations<K, V> {
	protected abstract Collection<V> mget(Collection<K> keys);

	public final Map<K, V> get(Collection<K> keys) {
		if (keys == null || keys.isEmpty()) {
			return null;
		}

		Collection<V> list = mget(keys);
		if (list == null || list.isEmpty()) {
			return null;
		} 

		Map<K, V> map = new HashMap<K, V>(keys.size(), 1);
		Iterator<K> keyIterator = keys.iterator();
		Iterator<V> valueIterator = list.iterator();
		while (keyIterator.hasNext() && valueIterator.hasNext()) {
			K key = keyIterator.next();
			V value = valueIterator.next();
			if (key == null || value == null) {
				continue;
			}

			map.put(key, value);
		}
		return map;
	}

	public V getAndTouch(K key, int newExp) {
		V v = get(key);
		if (v != null) {
			expire(key, newExp);
		}
		return v;
	}
}
