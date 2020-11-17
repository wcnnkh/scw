package scw.event;

public interface BasicEventRegistry<T extends Event> {
	EventRegistration registerListener(EventListener<T> eventListener);
}
