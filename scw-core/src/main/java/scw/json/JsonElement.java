package scw.json;

import scw.value.Value;

public interface JsonElement extends Value, JSONAware {
	static final String SPLIT = ",";
	
	JsonArray getAsJsonArray();

	JsonObject getAsJsonObject();

	boolean isJsonArray();

	boolean isJsonObject();
}
