package scw.value.property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import scw.event.EventRegistration;
import scw.event.support.DefaultNamedEventDispatcher;
import scw.event.support.EventMap;
import scw.event.support.StringNamedEventDispatcher;
import scw.event.support.ValueEvent;
import scw.io.ResourceUtils;
import scw.io.event.ObservableResource;
import scw.io.event.ObservableResourceEvent;
import scw.io.event.ObservableResourceEventListener;
import scw.util.DefaultStringMatcher;
import scw.value.AnyValue;
import scw.value.StringValue;
import scw.value.Value;

public class DynamicProperties extends EventMap<String, Value> {

	public DynamicProperties(boolean concurrent) {
		super(concurrent);
	}

	public DynamicProperties(Map<String, Value> targetMap, boolean concurrent) {
		super(targetMap, concurrent);
	}
	
	@Override
	protected DefaultNamedEventDispatcher<String, ValueEvent<Value>> createDefaultNamedEventDispatcher(
			boolean concurrent) {
		StringNamedEventDispatcher<ValueEvent<Value>> dispatcher = new StringNamedEventDispatcher<ValueEvent<Value>>(concurrent);
		dispatcher.setStringMatcher(DefaultStringMatcher.getInstance());
		return dispatcher;
	}

	public DynamicMapRegistration loadProperties(String resource, ValueCreator creator) {
		return loadProperties(resource, (String) null, creator);
	}

	public DynamicMapRegistration loadProperties(String resource, String charsetName, ValueCreator creator) {
		return loadProperties(null, resource, charsetName, creator);
	}

	public DynamicMapRegistration loadProperties(String keyPrefix, String resource, String charsetName,
			ValueCreator creator) {
		ObservableResource<Properties> res = ResourceUtils.getResourceOperations().getProperties(resource, charsetName);
		if (res.getResource() != null) {
			loadProperties(keyPrefix, res.getResource(), creator);
		}

		return new DynamicMapRegistration(keyPrefix, res, creator);
	}

	public void loadProperties(Properties properties, ValueCreator creator) {
		loadProperties(null, properties, creator);
	}

	public void loadProperties(String keyPrefix, Properties properties, ValueCreator creator) {
		if (properties == null) {
			return;
		}

		for (Entry<Object, Object> entry : properties.entrySet()) {
			Object key = entry.getKey();
			if (key == null) {
				continue;
			}

			Object value = entry.getValue();
			if (value == null) {
				continue;
			}

			put(keyPrefix == null ? key.toString() : (keyPrefix + key.toString()),
					creator.create(key.toString(), value));
		}
	}

	public interface ValueCreator {
		static final ValueCreator CREATOR = new ValueCreator() {

			public Value create(String key, Object value) {
				if (value instanceof Value) {
					return (Value) value;
				} else if (value instanceof String) {
					return new StringValue((String) value);
				} else {
					return new AnyValue(value);
				}
			}
		};

		Value create(String key, Object value);
	}

	public class DynamicMapRegistration implements EventRegistration {
		private final ObservableResource<Properties> resource;
		private volatile EventRegistration eventRegistration;
		private final String keyPrefix;
		private final ValueCreator creator;
		private final List<String> keys = new ArrayList<String>();

		public DynamicMapRegistration(String keyPrefix, ObservableResource<Properties> resource, ValueCreator creator) {
			this.keyPrefix = keyPrefix;
			this.resource = resource;
			this.creator = creator;
			addKeys(resource.getResource());
		}

		private void addKeys(Properties properties) {
			if (properties == null) {
				return;
			}

			for (Object key : properties.keySet()) {
				if (key == null) {
					continue;
				}

				keys.add(key.toString());
			}
		}

		public boolean isRegister() {
			return eventRegistration != null;
		}

		/**
		 * 默认只有当资源存在时才注册
		 */
		public void register() {
			register(true);
		}

		/**
		 * @param isExist true表示只在当资源存在时才注册
		 */
		public void register(boolean isExist) {
			if (isRegister()) {
				return;
			}

			synchronized (this) {
				if (isRegister()) {
					return;
				}

				this.eventRegistration = resource.registerListener(new ObservableResourceEventListener<Properties>() {

					public void onEvent(ObservableResourceEvent<Properties> event) {
						Properties properties = event.getSource();
						HashSet<String> keySet = new HashSet<String>(keys);
						if (properties != null) {
							for (Entry<Object, Object> entry : properties.entrySet()) {
								Object value = entry.getValue();
								if (value == null) {
									continue;
								}

								String key = entry.getKey().toString();
								String k = keyPrefix == null ? key : (keyPrefix + key);
								Value v = creator.create(key, value);
								put(k, v);
								keySet.remove(key);
							}
						}
						for (String key : keySet) {
							String k = keyPrefix == null ? key : (keyPrefix + key);
							remove(k);
						}
						keys.clear();
						addKeys(properties);
					}
				}, isExist);
			}
		}

		public void unregister() {
			if (eventRegistration != null) {
				synchronized (this) {
					eventRegistration.unregister();
				}
			}
			eventRegistration = null;
		}
	}
}