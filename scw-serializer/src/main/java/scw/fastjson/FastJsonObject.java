package scw.fastjson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

import scw.json.AbstractJson;
import scw.json.JsonArray;
import scw.json.JsonElement;
import scw.json.JsonObject;

public final class FastJsonObject extends AbstractJson<String> implements JsonObject, JSONAware, Serializable {
	private static final long serialVersionUID = 1L;
	private JSONObject jsonObject;

	public FastJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public void put(String key, Object value) {
		jsonObject.put(key, value);
	}

	public scw.json.JsonObject getJsonObject(String key) {
		JSONObject json = jsonObject.getJSONObject(key);
		return json == null ? null : new FastJsonObject(json);
	}

	public JsonArray getJsonArray(String key) {
		com.alibaba.fastjson.JSONArray json = jsonObject.getJSONArray(key);
		return json == null ? null : new FastJsonArray(json);
	}

	public JsonElement get(String key) {
		String text = jsonObject.getString(key);
		return text == null ? null : new FastJsonElement(text, getDefaultValue(key));
	}

	public Collection<String> keys() {
		return jsonObject.keySet();
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
}
