package scw.event.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import scw.compatible.map.DefaultCompatibleMap;
import scw.core.utils.CollectionUtils;
import scw.event.NamedEventDispatcher;

public class EventMap<K, V> extends DefaultCompatibleMap<K, V> {
	private DefaultNamedEventDispatcher<K, ValueEvent<V>> eventDispatcher;

	public EventMap(boolean concurrent) {
		this(concurrent ? new ConcurrentHashMap<K, V>() : new HashMap<K, V>(), concurrent);
	}

	public EventMap(Map<K, V> targetMap, boolean concurrent) {
		super(targetMap);
		this.eventDispatcher = createDefaultNamedEventDispatcher(concurrent);
	}

	protected DefaultNamedEventDispatcher<K, ValueEvent<V>> createDefaultNamedEventDispatcher(boolean concurrent) {
		return new DefaultNamedEventDispatcher<K, ValueEvent<V>>(concurrent);
	}

	public NamedEventDispatcher<K, ValueEvent<V>> getEventDispatcher() {
		return eventDispatcher;
	}

	public boolean isSupportedConcurrent() {
		return getTargetMap() instanceof ConcurrentMap;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return Collections.unmodifiableSet(super.entrySet());
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(super.keySet());
	}

	@Override
	public Collection<V> values() {
		return Collections.unmodifiableCollection(super.values());
	}

	@Override
	public V put(K key, V value) {
		V v;
		if (isSupportedConcurrent()) {
			v = super.put(key, value);
		} else {
			synchronized (this) {
				v = super.put(key, value);
			}
		}

		ValueEvent<V> event = null;
		if (v == null) {
			event = new ValueEvent<V>(EventType.CREATE, value);
		} else {
			if (!v.equals(value)) {
				event = new ValueEvent<V>(EventType.UPDATE, value);
			}
		}

		if (event != null) {
			getEventDispatcher().publishEvent(key, event);
		}
		return v;
	}

	@Override
	public V remove(Object key) {
		V v;
		if (isSupportedConcurrent()) {
			v = super.remove(key);
		} else {
			synchronized (this) {
				v = super.remove(key);
			}
		}

		if (v != null) {
			eventDispatcher.publishEventByObjectName(key, new ValueEvent<V>(EventType.DELETE, v));
		}
		return v;
	}

	@Override
	public void clear() {
		Map<K, V> cloneMap;
		if (isSupportedConcurrent()) {
			cloneMap = new HashMap<K, V>(this);
			super.clear();
		} else {
			synchronized (this) {
				cloneMap = new HashMap<K, V>(this);
				super.clear();
			}
		}

		for (Entry<K, V> entry : cloneMap.entrySet()) {
			getEventDispatcher().publishEvent(entry.getKey(), new ValueEvent<V>(EventType.DELETE, entry.getValue()));
		}
	}

	@Override
	public V putIfAbsent(K key, V value) {
		V v;
		if (isSupportedConcurrent()) {
			v = super.putIfAbsent(key, value);
		} else {
			synchronized (this) {
				v = super.putIfAbsent(key, value);
			}
		}

		if (v != null) {
			getEventDispatcher().publishEvent(key, new ValueEvent<V>(EventType.CREATE, value));
		}
		return v;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (CollectionUtils.isEmpty(m)) {
			return;
		}

		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
}
