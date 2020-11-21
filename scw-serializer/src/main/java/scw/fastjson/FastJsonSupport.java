package scw.fastjson;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.Feature;

import scw.json.AbstractJSONSupport;
import scw.json.EmptyJsonElement;
import scw.json.JsonElement;
import scw.json.JsonObject;

public final class FastJsonSupport extends AbstractJSONSupport {
	public static final FastJsonSupport INSTANCE = new FastJsonSupport();

	public scw.json.JsonArray parseArray(String text) {
		JSONArray jArray = com.alibaba.fastjson.JSONArray.parseArray(text);
		return jArray == null ? null : new FastJsonArray(jArray);
	}

	public JsonObject parseObject(String text) {
		com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(text);
		return jsonObject == null ? null : new FastJsonObject(jsonObject);
	}

	@Override
	protected String toJsonStringInternal(Object obj) {
		return JSON.toJSONString(obj, ExtendFastJsonValueFilter.INSTANCE);
	}

	@Override
	protected <T> T parseObjectInternal(String text, Class<T> type) {
		return JSON.parseObject(text, type, Feature.SupportNonPublicField);
	}

	@Override
	protected Object parseObjectInternal(String text, Type type) {
		return JSON.parseObject(text, type, Feature.SupportNonPublicField);
	}

	public JsonElement parseJson(String text) {
		return new FastJsonElement(text, EmptyJsonElement.INSTANCE);
	}
}
