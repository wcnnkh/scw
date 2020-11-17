package scw.compatible.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractCompatibleMap<K, V> implements CompatibleMap<K, V> {
	protected abstract Map<K, V> getTargetMap();

	public int size() {
		return getTargetMap().size();
	}

	public boolean isEmpty() {
		return getTargetMap().isEmpty();
	}

	public boolean containsKey(Object key) {
		return getTargetMap().containsKey(key);
	}

	public boolean containsValue(Object value) {
		return getTargetMap().containsValue(value);
	}

	public V get(Object key) {
		return getTargetMap().get(key);
	}

	public V put(K key, V value) {
		return getTargetMap().put(key, value);
	}

	public V remove(Object key) {
		return getTargetMap().remove(key);
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		getTargetMap().putAll(m);
	}

	public void clear() {
		getTargetMap().clear();
	}

	public Set<K> keySet() {
		return getTargetMap().keySet();
	}

	public Collection<V> values() {
		return getTargetMap().values();
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return getTargetMap().entrySet();
	}

	public V putIfAbsent(K key, V value) {
		if (getTargetMap() instanceof ConcurrentMap) {
			return ((ConcurrentMap<K, V>) getTargetMap()).putIfAbsent(key, value);
		}

		V v = get(key);
		if (v == null) {
			v = put(key, value);
		}
		return v;
	}

	@Override
	public String toString() {
		return getTargetMap().toString();
	}
	
	@Override
	public int hashCode() {
		return getTargetMap().hashCode();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		if(obj == this){
			return true;
		}
		
		if(obj instanceof AbstractCompatibleMap){
			return ((AbstractCompatibleMap) obj).getTargetMap().equals(getTargetMap());
		}
		
		return false;
	}
}
