package scw.fastjson;

import java.io.Serializable;
import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.parser.Feature;

import scw.core.utils.StringUtils;
import scw.json.AbstractJsonElement;
import scw.json.JsonArray;
import scw.json.JsonElement;
import scw.json.JsonObject;

public final class FastJsonElement extends AbstractJsonElement implements JSONAware, Serializable {
	private static final long serialVersionUID = 1L;
	private String text;

	public FastJsonElement(String text, JsonElement defaultValue) {
		super(defaultValue);
		this.text = text;
	}

	@Override
	protected <T> T getAsObjectNotSupport(Class<? extends T> type) {
		return JSON.parseObject(text, type, Feature.SupportNonPublicField);
	}

	@Override
	protected Object getAsObjectNotSupport(Type type) {
		return JSON.parseObject(text, type, Feature.SupportNonPublicField);
	}

	public JsonArray getAsJsonArray() {
		return new FastJsonArray(com.alibaba.fastjson.JSONArray.parseArray(text));
	}

	public JsonObject getAsJsonObject() {
		return new FastJsonObject(com.alibaba.fastjson.JSONObject.parseObject(text));
	}

	public String getAsString() {
		return text;
	}

	public String toJSONString() {
		return text;
	}

	public boolean isJsonArray() {
		return text == null ? false : JSON.isValidArray(text);
	}

	public boolean isJsonObject() {
		return text == null ? false : JSON.isValidObject(text);
	}

	public boolean isEmpty() {
		return StringUtils.isEmpty(text);
	}

	public String toJsonString() {
		return text;
	}

	@Override
	public int hashCode() {
		return text.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof FastJsonElement) {
			return StringUtils.equals(text, ((FastJsonElement) obj).text);
		}
		return false;
	}
}
