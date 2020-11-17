package scw.event;

public interface BasicEventDispatcher<T extends Event> extends BasicEventRegistry<T> {
	void publishEvent(T event);
}