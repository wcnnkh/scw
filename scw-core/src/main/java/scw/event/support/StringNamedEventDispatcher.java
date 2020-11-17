package scw.event.support;

import java.util.Map.Entry;

import scw.event.BasicEventDispatcher;
import scw.event.Event;
import scw.util.StringMatcher;

public class StringNamedEventDispatcher<T extends Event> extends DefaultNamedEventDispatcher<String, T> {
	private StringMatcher stringMatcher;

	public StringNamedEventDispatcher(boolean concurrent) {
		super(concurrent);
	}

	public StringMatcher getStringMatcher() {
		return stringMatcher;
	}

	public void setStringMatcher(StringMatcher stringMatcher) {
		this.stringMatcher = stringMatcher;
	}

	public void publishEvent(String name, T event) {
		if (stringMatcher == null) {
			super.publishEvent(name, event);
		} else {
			for (Entry<String, BasicEventDispatcher<T>> entry : getNamedEventListenerMap().entrySet()) {
				if (stringMatcher.match(entry.getKey(), name)) {
					entry.getValue().publishEvent(event);
				}
			}
		}
	}

	@Override
	public void publishEventByObjectName(Object name, T event) {
		publishEvent(name.toString(), event);
	}

}
