package scw.json.parser;

import java.lang.reflect.Type;

import scw.json.AbstractJSONSupport;
import scw.json.JsonArray;
import scw.json.JsonElement;
import scw.json.JsonObject;

public class DefaultJSONSupport extends AbstractJSONSupport{

	public JsonArray parseArray(String text) {
		return new DefaultJsonArray(text);
	}

	public JsonObject parseObject(String text) {
		return new DefaultJsonObject(text);
	}

	public JsonElement parseJson(String text) {
		return new DefaultJsonElement(text);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T parseObjectInternal(String text, Class<T> type) {
		return (T) DefaultJsonElement.parse(text, type);
	}

	@Override
	protected Object parseObjectInternal(String text, Type type) {
		return DefaultJsonElement.parse(text, type);
	}

	@Override
	protected String toJsonStringInternal(Object obj) {
		return JSONValue.toJSONString(obj);
	}

}
