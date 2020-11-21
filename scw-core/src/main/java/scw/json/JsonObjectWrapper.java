package scw.json;

import java.util.Iterator;
import java.util.Set;

import scw.util.KeyValuePair;

public class JsonObjectWrapper extends JsonWrapper<String> implements JsonObject {
	private JsonObject target;

	public JsonObjectWrapper(JsonObject target) {
		super(target);
		this.target = target;
	}

	public boolean containsKey(String key) {
		return target.containsKey(key);
	}

	public boolean remove(String key) {
		return target.remove(key);
	}

	public boolean put(String key, Object value) {
		return target.put(key, value);
	}

	public Set<String> keySet() {
		return target.keySet();
	}

	public Iterator<KeyValuePair<String, JsonElement>> iterator() {
		return target.iterator();
	}
}
