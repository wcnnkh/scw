package scw.event.support;

import scw.event.BasicEventDispatcher;
import scw.event.Event;
import scw.event.EventListener;
import scw.event.EventRegistration;

public class EmptyBasicEventDispatcher<T extends Event> implements BasicEventDispatcher<T> {

	public EventRegistration registerListener(EventListener<T> eventListener) {
		return EventRegistration.EMPTY;
	}

	public void publishEvent(T event) {
		// ignore
	}
}
