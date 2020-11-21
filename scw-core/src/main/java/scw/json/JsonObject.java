package scw.json;

import java.util.Set;

import scw.util.KeyValuePair;

public interface JsonObject extends Json<String>, Iterable<KeyValuePair<String, JsonElement>>{
	static final String PREFIX = "{";
	static final String SUFFIX = "}";
	
	Set<String> keySet();

	boolean containsKey(String key);
	
	boolean remove(String key);
	
	boolean put(String key, Object value);
}