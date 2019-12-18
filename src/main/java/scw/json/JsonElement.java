package scw.json;

import scw.util.Value;

public interface JsonElement extends Value {
	JsonArray getAsJsonArray();

	JsonObject getAsJsonObject();
}