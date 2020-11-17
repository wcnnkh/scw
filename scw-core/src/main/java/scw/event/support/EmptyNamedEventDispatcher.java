package scw.event.support;

import scw.event.Event;
import scw.event.EventListener;
import scw.event.EventRegistration;
import scw.event.NamedEventDispatcher;

public class EmptyNamedEventDispatcher<K, T extends Event> implements NamedEventDispatcher<K, T> {
	public EventRegistration registerListener(K name, EventListener<T> eventListener) {
		return EventRegistration.EMPTY;
	}

	public void publishEvent(K name, T event) {
		// ignore
	}
}
