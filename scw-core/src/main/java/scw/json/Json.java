package scw.json;

import scw.value.ValueFactory;

public interface Json<K> extends ValueFactory<K>, JSONAware{
	int size();

	boolean isEmpty();
	
	JsonElement getValue(K key);

	JsonArray getJsonArray(K key);

	JsonObject getJsonObject(K key);
}
