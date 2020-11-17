package scw.event;

public interface NamedEventDispatcher<K, T extends Event> extends NamedEventRegistry<K, T> {
	void publishEvent(K name, T event);
}