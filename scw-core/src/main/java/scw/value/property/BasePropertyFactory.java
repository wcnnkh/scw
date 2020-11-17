package scw.value.property;

import java.util.Iterator;

import scw.event.EventListener;
import scw.event.EventRegistration;
import scw.value.BaseValueFactory;

public interface BasePropertyFactory extends BaseValueFactory<String>, Iterable<String>{
	/**
	 * 能获取到值不一定代表可以通过此方法迭代到
	 * @return
	 */
	Iterator<String> iterator();
	
	boolean containsKey(String key);
	
	EventRegistration registerListener(String key, EventListener<PropertyEvent> eventListener);
}
