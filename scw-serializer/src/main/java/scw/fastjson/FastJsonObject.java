package scw.fastjson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import scw.core.Converter;
import scw.core.IteratorConverter;
import scw.json.AbstractJson;
import scw.json.JsonArray;
import scw.json.JsonElement;
import scw.json.JsonObject;
import scw.util.KeyValuePair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

public final class FastJsonObject extends AbstractJson<String> implements JsonObject, JSONAware, Serializable, Converter<Entry<String, Object>, KeyValuePair<String, JsonElement>> {
	private static final long serialVersionUID = 1L;
	private JSONObject jsonObject;

	public FastJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public boolean put(String key, Object value) {
		jsonObject.put(key, value);
		return true;
	}

	public scw.json.JsonObject getJsonObject(String key) {
		JSONObject json = jsonObject.getJSONObject(key);
		return json == null ? null : new FastJsonObject(json);
	}

	public JsonArray getJsonArray(String key) {
		com.alibaba.fastjson.JSONArray json = jsonObject.getJSONArray(key);
		return json == null ? null : new FastJsonArray(json);
	}

	public JsonElement getValue(String key) {
		String text = jsonObject.getString(key);
		return text == null ? null : new FastJsonElement(text, getDefaultValue(key));
	}

	public boolean containsKey(String key) {
		return jsonObject.containsKey(key);
	}

	public String toJsonString() {
		return JSON.toJSONString(jsonObject, ExtendFastJsonValueFilter.INSTANCE);
	}

	public <T> T getObjectSupport(String key, Class<? extends T> type) {
		return jsonObject.getObject(key, type);
	}

	public Object getObjectSupport(String key, Type type) {
		return jsonObject.getObject(key, type);
	}

	public int size() {
		return jsonObject.size();
	}

	public String toJSONString() {
		return toJsonString();
	}
	
	@Override
	public int hashCode() {
		return jsonObject.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		if(obj instanceof FastJsonObject){
			return jsonObject.equals(((FastJsonObject) obj).jsonObject);
		}

		return false;
	}

	public Set<String> keySet() {
		return jsonObject.keySet();
	}

	public boolean remove(String key) {
		return jsonObject.remove(key) != null;
	}

	public Iterator<KeyValuePair<String, JsonElement>> iterator() {
		return new IteratorConverter<Entry<String, Object>, KeyValuePair<String, JsonElement>>(jsonObject.entrySet().iterator(), this);
	}

	public KeyValuePair<String, JsonElement> convert(Entry<String, Object> k) {
		return new KeyValuePair<String, JsonElement>(k.getKey(), getValue(k.getKey()));
	}
}
