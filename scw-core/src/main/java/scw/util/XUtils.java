package scw.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import scw.core.Converter;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.lang.NotSupportedException;
import scw.mapper.MapperUtils;
import scw.value.ValueUtils;

public final class XUtils {
	private XUtils() {
	};

	public static String getUUID() {
		return StringUtils.removeChar(UUID.randomUUID().toString(), '-');
	}

	public static <T, R> T useResource(ResourceFactory<R> resourceFactory, Converter<R, T> converter) {
		R resource = null;
		try {
			resource = resourceFactory.getResource();
			return converter.convert(resource);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			resourceFactory.release(resource);
		}
	}

	public static <T, R> T execute(ResourceFactory<R> resourceFactory, Converter<R, T> resourceConverter) {
		R r = null;
		try {
			r = resourceFactory.getResource();
			return resourceConverter.convert(r);
		} finally {
			resourceFactory.release(r);
		}
	}

	/**
	 * 递归解析
	 * 
	 * @param instance
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map toMap(Object instance) {
		return toMap(instance, true);
	}

	/**
	 * 将对象转换为map
	 * 
	 * @see ValueUtils#isBaseType(Class) 此类型无法解析
	 * @param instance
	 * @param recursion
	 *            是否递归
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public static Map toMap(Object instance, boolean recursion) {
		if (instance == null) {
			return Collections.emptyMap();
		}

		if (instance instanceof ToMap) {
			ToMap toMap = (ToMap) instance;
			return recursion ? parseMap(toMap.toMap()) : toMap.toMap();
		} else if (instance instanceof Map) {
			return parseMap((Map) instance);
		}

		if (ValueUtils.isBaseType(instance.getClass())) {
			throw new NotSupportedException(instance.getClass().getName());
		}

		Map<String, Object> valueMap = MapperUtils.getMapper().getFieldValueMap(instance);
		return recursion ? parseMap(valueMap) : valueMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map parseMap(Map map) {
		if (CollectionUtils.isEmpty(map)) {
			return Collections.EMPTY_MAP;
		}

		Set<Entry> entries = map.entrySet();
		Map valueMap = new LinkedHashMap();
		for (Entry entry : entries) {
			valueMap.put(entry.getKey(), parseValue(entry.getValue()));
		}
		return valueMap;
	}

	@SuppressWarnings({ "rawtypes" })
	private static Object parseValue(Object value) {
		if (value == null) {
			return value;
		}

		if (ValueUtils.isBaseType(value.getClass())) {
			return value;
		} else if (value instanceof ToMap) {
			return parseMap(((ToMap) value).toMap());
		} else if (value instanceof Collection) {
			Collection list = (Collection) value;
			if (CollectionUtils.isEmpty(list)) {
				return value;
			}

			List<Object> newList = new ArrayList<Object>(list.size());
			for (Object v : list) {
				newList.add(parseValue(v));
			}
			return newList;
		} else if (value instanceof Map) {
			return parseMap((Map) value);
		} else if (value.getClass().isArray()) {
			int len = Array.getLength(value);
			if (len == 0) {
				return value;
			}

			Object[] array = new Object[len];
			for (int i = 0; i < len; i++) {
				array[i] = parseValue(Array.get(value, i));
			}
			return array;
		} else {
			Map<String, Object> valueMap = MapperUtils.getMapper().getFieldValueMap(value);
			return parseMap(valueMap);
		}
	}

	public static void appendToMap(Properties properties, Map<String, String> map) {
		if (properties == null || map == null) {
			return;
		}

		for (Entry<Object, Object> entry : properties.entrySet()) {
			map.put(entry.getKey() == null ? null : entry.getKey().toString(),
					entry.getValue() == null ? null : entry.getValue().toString());
		}
	}

	public static <T> T use(T... objs) {
		for (T obj : objs) {
			if (obj != null) {
				return obj;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getTarget(Object wrapper, Class<T> targetType) {
		if (targetType.isInstance(wrapper)) {
			return (T) wrapper;
		}

		if (wrapper instanceof Target) {
			return ((Target) wrapper).getTarget(targetType);
		}
		return null;
	}

	public static Comparator<String> getComparator(final StringMatcher matcher) {
		return new Comparator<String>() {

			public int compare(String o1, String o2) {
				if (matcher.isPattern(o1) && matcher.isPattern(o2)) {
					if (matcher.match(o1, o2)) {
						return 1;
					} else if (matcher.match(o2, o1)) {
						return -1;
					} else {
						return -1;
					}
				} else if (matcher.isPattern(o1)) {
					return 1;
				} else if (matcher.isPattern(o2)) {
					return -1;
				}
				return o1.equals(o1) ? 0 : -1;
			}
		};
	}

	/**
	 * 获取名称
	 * 
	 * @see Named#getName()
	 * @param instance
	 * @param defaultName
	 * @return
	 */
	public static String getName(Object instance, String defaultName) {
		if (instance == null) {
			return defaultName;
		}

		if (instance instanceof Named) {
			String name = ((Named) instance).getName();
			return name == null ? defaultName : name;
		}

		return defaultName;
	}
}