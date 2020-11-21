package scw.value.property;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import scw.core.Assert;
import scw.core.utils.CollectionUtils;
import scw.event.EventListener;
import scw.event.EventRegistration;
import scw.event.MultiEventRegistration;
import scw.event.support.AbstractDynamicValue;
import scw.event.support.DynamicValue;
import scw.event.support.ValueEvent;
import scw.util.MultiIterator;
import scw.util.StringMatcher;
import scw.value.AnyValue;
import scw.value.StringValue;
import scw.value.StringValueFactory;
import scw.value.Value;
import scw.value.ValueWrapper;
import scw.value.property.DynamicProperties.DynamicMapRegistration;
import scw.value.property.DynamicProperties.ValueCreator;

public class PropertyFactory extends StringValueFactory implements BasePropertyFactory {
	private final List<BasePropertyFactory> basePropertyFactories;
	private final DynamicProperties dynamicProperties;
	private final boolean priorityOfUseSelf;

	/**
	 * @param concurrent
	 * @param priorityOfUseSelf
	 *            是否优先使用自身的值
	 */
	public PropertyFactory(boolean concurrent, boolean priorityOfUseSelf) {
		this.dynamicProperties = new DynamicProperties(concurrent);
		this.basePropertyFactories = concurrent ? new CopyOnWriteArrayList<BasePropertyFactory>()
				: new LinkedList<BasePropertyFactory>();
		this.priorityOfUseSelf = priorityOfUseSelf;
	}

	public boolean isPriorityOfUseSelf() {
		return priorityOfUseSelf;
	}

	public void addFirstBasePropertyFactory(BasePropertyFactory basePropertyFactory) {
		if (basePropertyFactory == null) {
			return;
		}

		basePropertyFactories.add(0, basePropertyFactory);
	}

	public void addLastBasePropertyFactory(BasePropertyFactory basePropertyFactory) {
		if (basePropertyFactory == null) {
			return;
		}

		basePropertyFactories.add(basePropertyFactory);
	}

	public void addLastBasePropertyFactory(List<BasePropertyFactory> basePropertyFactories) {
		if (CollectionUtils.isEmpty(basePropertyFactories)) {
			return;
		}

		for (BasePropertyFactory propertyFactory : basePropertyFactories) {
			addLastBasePropertyFactory(propertyFactory);
		}
	}

	public List<BasePropertyFactory> getBasePropertyFactories() {
		return Collections.unmodifiableList(basePropertyFactories);
	}
	
	private void appendByMatcher(Map<String, Value> map, String pattern, StringMatcher stringMatcher){
		for (Entry<String, Value> entry : dynamicProperties.entrySet()) {
			if (map.containsKey(entry.getKey()) || entry.getValue() == null) {
				continue;
			}
			
			if(stringMatcher.match(pattern, entry.getKey())){
				map.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	 * 根据匹配规则获取结果
	 * @param pattern
	 * @param stringMatcher
	 * @return
	 */
	public Map<String, Value> getByMatcher(String pattern, StringMatcher stringMatcher) {
		if(!stringMatcher.isPattern(pattern)){
			Value value = getValue(pattern);
			if(value == null){
				return Collections.emptyMap();
			}
			
			return Collections.singletonMap(pattern, value);
		}
		
		Map<String, Value> map = new LinkedHashMap<String, Value>();
		if (priorityOfUseSelf) {
			appendByMatcher(map, pattern, stringMatcher);
		}

		for (BasePropertyFactory basePropertyFactory : basePropertyFactories) {
			for(String key : basePropertyFactory){
				if (map.containsKey(key)) {
					continue;
				}

				if(stringMatcher.match(pattern, key)){
					Value value = basePropertyFactory.getValue(key);
					if(value == null){
						continue;
					}
					map.put(key, value);
				}
			}
		}

		if (!priorityOfUseSelf) {
			appendByMatcher(map, pattern, stringMatcher);
		}
		return map;
	}

	@Override
	public Value getValue(String key) {
		if (priorityOfUseSelf) {
			Value value = dynamicProperties.get(key);
			if (value != null) {
				return value;
			}
		}

		for (BasePropertyFactory basePropertyFactory : basePropertyFactories) {
			Value value = basePropertyFactory.getValue(key);
			if (value != null) {
				return value;
			}
		}

		Value value = super.getValue(key);
		if (value == null && !priorityOfUseSelf) {
			value = dynamicProperties.get(key);
		}
		return value;
	}
	
	public Iterator<String> iterator() {
		List<Iterator<String>> iterators = new LinkedList<Iterator<String>>();
		if(priorityOfUseSelf){
			iterators.add(dynamicProperties.keySet().iterator());
		}
		
		for (BasePropertyFactory basePropertyFactory : basePropertyFactories) {
			iterators.add(basePropertyFactory.iterator());
		}
		
		if(!priorityOfUseSelf){
			iterators.add(dynamicProperties.keySet().iterator());
		}
		
		return new MultiIterator<String>(iterators);
	}

	public boolean containsKey(String key) {
		if (dynamicProperties.containsKey(key)) {
			return true;
		}

		for (BasePropertyFactory basePropertyFactory : basePropertyFactories) {
			if (basePropertyFactory.containsKey(key)) {
				return true;
			}
		}
		return false;
	}

	private class PropertyEventListener implements EventListener<ValueEvent<Value>> {
		private final EventListener<PropertyEvent> eventListener;
		private final String key;

		public PropertyEventListener(String key, EventListener<PropertyEvent> eventListener) {
			this.key = key;
			this.eventListener = eventListener;
		}

		public void onEvent(ValueEvent<Value> event) {
			eventListener.onEvent(new PropertyEvent(PropertyFactory.this, key, event));
		}
	}

	public EventRegistration registerListener(String key, EventListener<PropertyEvent> eventListener) {
		EventRegistration registration = dynamicProperties.getEventDispatcher().registerListener(key,
				new PropertyEventListener(key, eventListener));
		EventRegistration[] registrations = new EventRegistration[basePropertyFactories.size() + 1];
		registrations[0] = registration;
		int index = 1;
		for (BasePropertyFactory basePropertyFactory : basePropertyFactories) {
			registrations[index++] = basePropertyFactory.registerListener(key, eventListener);
		}
		return new MultiEventRegistration(registrations);
	}

	public Value remove(String key) {
		Assert.requiredArgument(key != null, "key");
		return dynamicProperties.remove(key);
	}

	public Value put(String key, Object value) {
		return put(key, value, false);
	}

	public Value put(String key, Object value, boolean format) {
		Assert.requiredArgument(key != null, "key");
		Assert.requiredArgument(value != null, "value");
		return dynamicProperties.put(key, toValue(value, format));
	}

	private Value toValue(Object value, boolean resolvePlaceholders) {
		Value v;
		if (value instanceof Value) {
			if (value instanceof StringFormatValue || value instanceof AnyFormatValue || value instanceof FormatValue) {
				v = (Value) value;
			} else {
				v = resolvePlaceholders ? new FormatValue((Value) value) : (Value) value;
			}
		} else if (value instanceof String) {
			v = resolvePlaceholders ? new StringFormatValue((String) value) : new StringValue((String) value);
		} else {
			v = resolvePlaceholders ? new AnyFormatValue(value) : new AnyValue(value);
		}
		return v;
	}

	public Value putIfAbsent(String key, Object value) {
		return putIfAbsent(key, value, false);
	}

	public Value putIfAbsent(String key, Object value, boolean format) {
		Assert.requiredArgument(key != null, "key");
		Assert.requiredArgument(value != null, "value");
		return dynamicProperties.putIfAbsent(key, toValue(value, format));
	}

	public void clear() {
		dynamicProperties.clear();
	}

	public PropertyFactoryRegistration loadProperties(String resource) {
		return loadProperties(resource, (String) null);
	}

	public PropertyFactoryRegistration loadProperties(String resource, String charsetName) {
		return loadProperties(null, resource, charsetName);
	}

	public PropertyFactoryRegistration loadProperties(final String keyPrefix, String resource, String charsetName) {
		return loadProperties(keyPrefix, resource, charsetName, true);
	}

	public PropertyFactoryRegistration loadProperties(String resource, boolean format) {
		return loadProperties((String) resource, (String) null, format);
	}

	public PropertyFactoryRegistration loadProperties(String resource, String charsetName, boolean format) {
		return loadProperties(null, resource, charsetName, format);
	}

	public PropertyFactoryRegistration loadProperties(final String keyPrefix, String resource, String charsetName,
			final boolean format) {
		DynamicMapRegistration propertiesRegistration = dynamicProperties.loadProperties(keyPrefix, resource,
				charsetName, new ValueCreator() {

					public Value create(String key, Object value) {
						return toValue(value, format);
					}
				});
		return new PropertyFactoryRegistration(propertiesRegistration);
	}

	public void loadProperties(Properties properties) {
		loadProperties(null, properties, false);
	}

	public void loadProperties(String keyPrefix, Properties properties) {
		loadProperties(keyPrefix, properties, false);
	}

	public void loadProperties(String keyPrefix, Properties properties, boolean format) {
		if (properties != null) {
			for (Entry<Object, Object> entry : properties.entrySet()) {
				Object key = entry.getKey();
				if (key == null) {
					continue;
				}

				Object value = entry.getValue();
				if (value == null) {
					continue;
				}
				put(keyPrefix == null ? key.toString() : (keyPrefix + key.toString()), value, format);
			}
		}
	}

	public final <T> DynamicValue<T> getDynamicValue(String name, Class<? extends T> type, T defaultValue) {
		return new PropertyDynamicValue<T>(name, type, defaultValue);
	}

	public final DynamicValue<Object> getDynamicValue(String name, Type type, Object defaultValue) {
		return new PropertyDynamicValue<Object>(name, type, defaultValue);
	}

	public class PropertyFactoryRegistration {
		private final DynamicMapRegistration propertiesRegistration;

		public PropertyFactoryRegistration(DynamicMapRegistration propertiesRegistration) {
			this.propertiesRegistration = propertiesRegistration;
		}

		public PropertyFactory getPropertyFactory() {
			return PropertyFactory.this;
		}

		public DynamicMapRegistration getPropertiesRegistration() {
			return propertiesRegistration;
		}

		/**
		 * 默认只有当资源存在时才注册
		 * 
		 * @return
		 */
		public PropertyFactory registerListener() {
			return registerListener(true);
		}

		/**
		 * @param isExist
		 *            true表示只有当资源存在时才注册
		 * @return
		 */
		public PropertyFactory registerListener(boolean isExist) {
			propertiesRegistration.register(isExist);
			return getPropertyFactory();
		}
	}

	private class StringFormatValue extends StringValue {
		private static final long serialVersionUID = 1L;

		public StringFormatValue(String value) {
			super(value);
		}

		@Override
		public String getAsString() {
			return format(super.getAsString());
		}
	}

	private class AnyFormatValue extends AnyValue {
		private static final long serialVersionUID = 1L;

		public AnyFormatValue(Object value) {
			super(value);
		}

		public String getAsString() {
			return format(super.getAsString());
		};
	}

	private class FormatValue extends ValueWrapper {

		public FormatValue(Value value) {
			super(value);
		}

		@Override
		public String getAsString() {
			return format(super.getAsString());
		}
	}

	private final class PropertyDynamicValue<T> extends AbstractDynamicValue<T> {
		private final String name;
		private final T defaultValue;
		private final Type type;

		public PropertyDynamicValue(String name, Type type, T defaultValue) {
			this.name = name;
			this.type = type;
			this.defaultValue = defaultValue;
			setValue(getNewValue());
			switchDynamicState(true);
		}

		@SuppressWarnings("unchecked")
		protected T getNewValue() {
			return (T) PropertyFactory.this.getValue(name, type, defaultValue);
		}

		public EventRegistration registerListener(final EventListener<ValueEvent<T>> eventListener) {
			return PropertyFactory.this.registerListener(name, new EventListener<PropertyEvent>() {

				public void onEvent(PropertyEvent event) {
					eventListener.onEvent(new ValueEvent<T>(event, getNewValue()));
				}
			});
		}
	}
}
