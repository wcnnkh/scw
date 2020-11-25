package scw.json.gson;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import scw.core.Converter;
import scw.core.IteratorConverter;
import scw.json.AbstractJson;
import scw.json.JsonElement;
import scw.json.JsonObject;
import scw.util.KeyValuePair;

import com.google.gson.Gson;

public final class GsonObject extends AbstractJson<String> implements JsonObject, Converter<Entry<String, com.google.gson.JsonElement>, KeyValuePair<String, JsonElement>> {
	private com.google.gson.JsonObject gsonJsonObject;
	private Gson gson;

	public GsonObject(com.google.gson.JsonObject gsonJsonObject, Gson gson) {
		this.gsonJsonObject = gsonJsonObject;
		this.gson = gson;
	}

	public boolean put(String key, Object value) {
		if (value == null) {
			return false;
		}

		gsonJsonObject.add(key, gson.toJsonTree(value));
		return true;
	}

	public JsonElement getValue(String key) {
		com.google.gson.JsonElement gsonJsonElement = gsonJsonObject.get(key);
		return gsonJsonElement == null ? null : new GsonElement(gsonJsonElement, gson, getDefaultValue(key));
	}

	public Set<String> keySet() {
		//return gsonJsonObject.keySet();
		return null;
	}

	public boolean containsKey(String key) {
		return gsonJsonObject.has(key);
	}

	public String toJSONString() {
		return gsonJsonObject.toString();
	}

	public int size() {
		return gsonJsonObject.size();
	}

	@Override
	public int hashCode() {
		return gsonJsonObject.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		if(obj instanceof GsonObject){
			return gsonJsonObject.equals(((GsonObject) obj).gsonJsonObject);
		}
		
		return false;
	}

	public boolean remove(String key) {
		return gsonJsonObject.remove(key) != null;
	}
	
	public KeyValuePair<String, JsonElement> convert(
			Entry<String, com.google.gson.JsonElement> k) {
		return new KeyValuePair<String, JsonElement>(k.getKey(), new GsonElement(k.getValue(), gson));
	}

	public Iterator<KeyValuePair<String, JsonElement>> iterator() {
		return new IteratorConverter<Entry<String, com.google.gson.JsonElement>, KeyValuePair<String,JsonElement>>(gsonJsonObject.entrySet().iterator(), this);
	}
}
