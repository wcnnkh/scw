package scw.json.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import scw.json.AbstractJsonElement;
import scw.json.EmptyJsonElement;
import scw.json.JsonArray;
import scw.json.JsonObject;

public final class GsonElement extends AbstractJsonElement {
	private JsonElement gsonJsonElement;
	private Gson gson;
	
	public GsonElement(JsonElement gsonJsonElement, Gson gson) {
		this(gsonJsonElement, gson, EmptyJsonElement.INSTANCE);
	}

	public GsonElement(JsonElement gsonJsonElement, Gson gson, scw.json.JsonElement defaultValue) {
		super(defaultValue);
		this.gsonJsonElement = gsonJsonElement;
		this.gson = gson;
	}

	public String getAsString() {
		return gsonJsonElement.getAsString();
	}

	@Override
	protected <T> T getAsObjectNotSupport(Class<? extends T> type) {
		return gson.fromJson(gsonJsonElement, type);
	}

	@Override
	protected Object getAsObjectNotSupport(Type type) {
		return gson.fromJson(gsonJsonElement, type);
	}

	public JsonArray getAsJsonArray() {
		return new GsonArray(gsonJsonElement.getAsJsonArray(), gson);
	}

	public JsonObject getAsJsonObject() {
		return new GsonObject(gsonJsonElement.getAsJsonObject(), gson);
	}

	public boolean isJsonArray() {
		return gsonJsonElement.isJsonArray();
	}

	public boolean isJsonObject() {
		return gsonJsonElement.isJsonObject();
	}

	public boolean isEmpty() {
		return gsonJsonElement.isJsonNull();
	}

	public String toJSONString() {
		if (gsonJsonElement.isJsonArray() || gsonJsonElement.isJsonObject()) {
			return gsonJsonElement.toString();
		}
		return gsonJsonElement.getAsString();
	}

	@Override
	public int hashCode() {
		return gsonJsonElement.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof GsonElement) {
			return gsonJsonElement.equals(((GsonElement) obj).gsonJsonElement);
		}
		return false;
	}
}
