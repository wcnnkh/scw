package scw.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import scw.core.instance.InstanceUtils;
import scw.json.support.BuiltinGsonSupport;
import scw.util.FormatUtils;

public final class JSONUtils {
	private JSONUtils() {
	};

	/**
	 * 默认的json序列化工具
	 */
	public static final JSONSupport JSON_SUPPORT;

	static {
		JSONSupport jsonSupport = InstanceUtils.loadService(JSONSupport.class);
		JSON_SUPPORT = jsonSupport == null ? BuiltinGsonSupport.INSTANCE : jsonSupport;
		FormatUtils.info(JSONUtils.class, "using json support：{}", JSON_SUPPORT.getClass().getName());
	}

	public static JSONSupport getJsonSupport() {
		return JSON_SUPPORT;
	}

	public static String toJSONString(Object obj) {
		return JSON_SUPPORT.toJSONString(obj);
	}

	public static JsonObject parseObject(String text) {
		return JSON_SUPPORT.parseObject(text);
	}

	public static JsonArray parseArray(String text) {
		return JSON_SUPPORT.parseArray(text);
	}

	public static <T> T parseObject(String text, Class<T> type) {
		return JSON_SUPPORT.parseObject(text, type);
	}

	public static <T> T parseObject(String text, Type type) {
		return JSON_SUPPORT.parseObject(text, type);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> parseArray(JsonArray jsonArray, Type type) {
		if (jsonArray == null) {
			return null;
		}

		if (jsonArray.isEmpty()) {
			return Collections.emptyList();
		}

		List<T> list = new ArrayList<T>(jsonArray.size());
		for (int i = 0, len = jsonArray.size(); i < len; i++) {
			T value = (T) jsonArray.getObject(i, type);
			list.add(value);
		}
		return list;
	}

	public static <T extends JsonObjectWrapper> List<T> wrapper(JsonArray jsonArray, Class<? extends T> type) {
		if (jsonArray == null) {
			return null;
		}

		if (jsonArray.isEmpty()) {
			return Collections.emptyList();
		}

		List<T> list = new ArrayList<T>(jsonArray.size());
		for (int i = 0, len = jsonArray.size(); i < len; i++) {
			T value = InstanceUtils.INSTANCE_FACTORY.getInstance(type, jsonArray.getJsonObject(i));
			list.add(value);
		}
		return list;
	}
}
