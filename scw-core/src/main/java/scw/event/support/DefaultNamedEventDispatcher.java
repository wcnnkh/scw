package scw.event.support;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import scw.compatible.CompatibleUtils;
import scw.compatible.map.CompatibleMap;
import scw.event.BasicEventDispatcher;
import scw.event.Event;
import scw.event.EventListener;
import scw.event.EventRegistration;
import scw.event.NamedEventDispatcher;

public class DefaultNamedEventDispatcher<K, T extends Event> implements NamedEventDispatcher<K, T> {
	private final CompatibleMap<K, BasicEventDispatcher<T>> namedEventListenerMap;
	private final boolean concurrent;

	public DefaultNamedEventDispatcher(boolean concurrent) {
		this.concurrent = concurrent;
		Map<K, BasicEventDispatcher<T>> namedEventListenerMap = concurrent
				? new ConcurrentHashMap<K, BasicEventDispatcher<T>>(8) : new HashMap<K, BasicEventDispatcher<T>>(8);
		this.namedEventListenerMap = CompatibleUtils.getMapCompatible().wrapper(namedEventListenerMap);
	}

	public final boolean isConcurrent() {
		return concurrent;
	}

	public CompatibleMap<K, BasicEventDispatcher<T>> getNamedEventListenerMap() {
		return namedEventListenerMap;
	}

	protected BasicEventDispatcher<T> createBasicEventDispatcher(K name) {
		return new DefaultBasicEventDispatcher<T>(isConcurrent());
	}

	public EventRegistration registerListener(K name, EventListener<T> eventListener) {
		BasicEventDispatcher<T> eventDispatcher = namedEventListenerMap.get(name);
		if (eventDispatcher == null) {
			eventDispatcher = createBasicEventDispatcher(name);
			BasicEventDispatcher<T> dispatcher = namedEventListenerMap.putIfAbsent(name, eventDispatcher);
			if (dispatcher != null) {
				eventDispatcher = dispatcher;
			}
		}

		return eventDispatcher.registerListener(eventListener);
	}

	public void publishEvent(K name, T event) {
		publishEventByObjectName(name, event);
	}

	public void publishEventByObjectName(Object name, T event) {
		BasicEventDispatcher<T> dispatcher = namedEventListenerMap.get(name);
		if (dispatcher == null) {
			return;
		}

		dispatcher.publishEvent(event);
		return;
	}
}
