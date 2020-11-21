package scw.json.gson;

import java.util.Iterator;

import com.google.gson.Gson;

import scw.core.Converter;
import scw.core.IteratorConverter;
import scw.json.AbstractJson;
import scw.json.EmptyJsonElement;
import scw.json.JsonArray;
import scw.json.JsonElement;

public final class GsonArray extends AbstractJson<Integer>
		implements JsonArray, Converter<com.google.gson.JsonElement, JsonElement> {
	private com.google.gson.JsonArray gsonJsonArray;
	private Gson gson;

	public GsonArray(com.google.gson.JsonArray gsonJsonArray, Gson gson) {
		this.gsonJsonArray = gsonJsonArray;
		this.gson = gson;
	}

	public JsonElement convert(com.google.gson.JsonElement gsonJsonElement) {
		return new GsonElement(gsonJsonElement, gson, EmptyJsonElement.INSTANCE);
	}

	public Iterator<scw.json.JsonElement> iterator() {
		return new IteratorConverter<com.google.gson.JsonElement, JsonElement>(gsonJsonArray.iterator(), this);
	}

	public JsonElement getValue(Integer index) {
		com.google.gson.JsonElement element = gsonJsonArray.get(index);
		return element == null ? null : new GsonElement(element, gson, getDefaultValue(index));
	}

	public boolean add(Object value) {
		if (value == null) {
			return false;
		}
		gsonJsonArray.add(gson.toJsonTree(value));
		return true;
	}

	public int size() {
		return gsonJsonArray.size();
	}

	public String toJSONString() {
		return gsonJsonArray.toString();
	}

	@Override
	public int hashCode() {
		return gsonJsonArray.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof GsonArray) {
			return gsonJsonArray.equals(((GsonArray) obj).gsonJsonArray);
		}
		return false;
	}

	public boolean remove(int index) {
		return gsonJsonArray.remove(index) != null;
	}
}
