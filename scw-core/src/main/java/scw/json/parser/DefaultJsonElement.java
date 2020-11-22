package scw.json.parser;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import scw.core.ResolvableType;
import scw.core.instance.InstanceUtils;
import scw.core.instance.NoArgsInstanceFactory;
import scw.json.AbstractJsonElement;
import scw.json.EmptyJsonElement;
import scw.json.JSONException;
import scw.json.JsonArray;
import scw.json.JsonElement;
import scw.json.JsonObject;
import scw.mapper.Field;
import scw.mapper.Fields;
import scw.mapper.FilterFeature;
import scw.mapper.MapperUtils;
import scw.value.AnyValue;
import scw.value.StringValue;
import scw.value.ValueUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DefaultJsonElement extends AbstractJsonElement implements
		JsonElement, Serializable {
	private static final long serialVersionUID = 1L;
	private String text;

	public DefaultJsonElement(Object json) {
		this(json, EmptyJsonElement.INSTANCE);
	}

	public DefaultJsonElement(Object json, JsonElement defaultValue) {
		super(defaultValue);
		this.text = toJSONString(json);
	}

	public DefaultJsonElement(String text) {
		this(text, EmptyJsonElement.INSTANCE);
	}

	public DefaultJsonElement(String text, JsonElement defaultValue) {
		super(defaultValue);
		this.text = text;
	}

	public String getAsString() {
		return text;
	}

	public String toJSONString() {
		return text;
	}

	public JsonArray getAsJsonArray() {
		return new DefaultJsonArray(text);
	}

	public JsonObject getAsJsonObject() {
		return new DefaultJsonObject(text);
	}

	public boolean isJsonArray() {
		return text.startsWith(JsonArray.PREFIX)
				&& text.endsWith(JsonArray.SUFFIX);
	}

	public boolean isJsonObject() {
		return text.startsWith(JsonObject.PREFIX)
				&& text.endsWith(JsonObject.SUFFIX);
	}

	@Override
	protected <T> T getAsObjectNotSupport(Class<? extends T> type) {
		return (T) parse(text, type);
	}

	@Override
	protected Object getAsObjectNotSupport(Type type) {
		return parse(text, type);
	}

	public static Object parse(String text, Type type) {
		return parse(text, ResolvableType.forType(type),
				InstanceUtils.INSTANCE_FACTORY);
	}

	public static String toJSONString(Object json) {
		if (ValueUtils.isBaseType(json.getClass())) {
			return String.valueOf(json);
		} else {
			return JSONValue.toJSONString(json);
		}
	}

	public static Object parse(Object json, ResolvableType type,
			NoArgsInstanceFactory instanceFactory) {
		if (json == null) {
			return null;
		}
		
		if(json instanceof String){
			return parse((String)json, type, instanceFactory);
		}

		Class<?> clazz = type.getRawClass();
		if (clazz == null) {
			return null;
		}

		if (clazz == Object.class) {
			return json;
		}

		if (ValueUtils.isBaseType(clazz)) {
			return new AnyValue(json).getAsObject(type.getType());
		}

		if (json instanceof List) {
			List jsonArray = (List) json;
			int size = jsonArray.size();
			if (clazz.isArray()) {
				ResolvableType componentType = ResolvableType.forClass(clazz
						.getComponentType());
				Object array = Array
						.newInstance(clazz.getComponentType(), size);
				for (int i = 0; i < size; i++) {
					Object value = parse(jsonArray.get(i), componentType,
							instanceFactory);
					Array.set(array, i, value);
				}
				return array;
			} else if (List.class.isAssignableFrom(clazz)) {
				List list = new ArrayList(size);
				for (Object item : jsonArray) {
					if (item == null) {
						list.add(null);
						continue;
					}

					Object value = parse(item, type.getGeneric(0),
							instanceFactory);
					list.add(value);
				}
				return list;
			}
		} else if (json instanceof Map) {
			if (type instanceof List) {
				return type;
			}

			Map jsonObject = (Map) json;
			if (Map.class.isAssignableFrom(clazz)) {
				Map map = new LinkedHashMap();
				for (Entry entry : (Set<Entry>) jsonObject.entrySet()) {
					if (entry.getKey() == null || entry.getValue() == null) {
						continue;
					}

					Object key = parse(entry.getKey(), type.getGeneric(0),
							instanceFactory);
					Object value = parse(entry.getValue(), type.getGeneric(1),
							instanceFactory);
					map.put(key, value);
				}
				return map;
			}

			Fields fields = MapperUtils.getMapper().getFields(clazz,
					FilterFeature.SETTER_IGNORE_STATIC,
					FilterFeature.SETTER_PUBLIC);
			Object instance = instanceFactory.getInstance(clazz);
			for (Field field : fields) {
				Object value = jsonObject.get(field.getSetter().getName());
				if (value == null) {
					continue;
				}

				value = parse(value, ResolvableType.forType(field.getSetter()
						.getGenericType()), instanceFactory);
				if (value == null) {
					continue;
				}

				field.getSetter().set(instance, value);
			}
			return instance;
		}
		throw new JSONException(type + " <== " + json);
	}

	public static Object parse(String text, ResolvableType type,
			NoArgsInstanceFactory instanceFactory) {
		if (text == null) {
			return null;
		}

		Class<?> clazz = type.getRawClass();
		if (clazz == null) {
			return null;
		}

		if (ValueUtils.isBaseType(clazz)) {
			return new StringValue(text).getAsObject(type.getType());
		}

		Object json = JSONValue.parse(text);
		if (json == null) {
			return null;
		}

		if (clazz == Object.class) {
			return json;
		}

		return parse(json, type, instanceFactory);
	}
}
