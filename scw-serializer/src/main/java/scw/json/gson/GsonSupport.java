package scw.json.gson;

import java.lang.reflect.Type;

import scw.json.AbstractJSONSupport;
import scw.json.EmptyJsonElement;
import scw.json.JsonArray;
import scw.json.JsonElement;
import scw.json.JsonObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonSupport extends AbstractJSONSupport {
	public static final GsonSupport INSTANCE = new GsonSupport();

	private static final Gson GSON = new GsonBuilder().registerTypeAdapterFactory(ExtendGsonTypeAdapter.FACTORY).create();

	public JsonArray parseArray(String text) {
		com.google.gson.JsonElement gsonJsonElement = GSON.fromJson(text, com.google.gson.JsonElement.class);
		return new GsonArray(gsonJsonElement.getAsJsonArray(), GSON);
	}

	public JsonObject parseObject(String text) {
		com.google.gson.JsonElement jsonElement = GSON.fromJson(text, com.google.gson.JsonElement.class);
		return new GsonObject(jsonElement.getAsJsonObject(), GSON);
	}

	public <T> T parseObjectInternal(String text, Class<T> type) {
		return GSON.fromJson(text, type);
	}

	public Object parseObjectInternal(String text, Type type) {
		return GSON.fromJson(text, type);
	}

	public JsonElement parseJson(String text) {
		com.google.gson.JsonElement gsonJsonElement = GSON.fromJson(text, com.google.gson.JsonElement.class);
		return new GsonElement(gsonJsonElement, GSON, EmptyJsonElement.INSTANCE);
	}

	@Override
	protected String toJsonStringInternal(Object obj) {
		return GSON.toJson(obj);
	}
}
