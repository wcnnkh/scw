package scw.json;

import scw.value.ValueFactoryWrapper;

public class JsonWrapper<K> extends ValueFactoryWrapper<K> implements Json<K> {
	private final Json<K> target;

	public JsonWrapper(Json<K> target) {
		super(target);
		this.target = target;
	}

	public int size() {
		return target.size();
	}

	public boolean isEmpty() {
		return target.isEmpty();
	}

	public JsonElement getValue(K key) {
		return target.getValue(key);
	}

	public JsonArray getJsonArray(K key) {
		return target.getJsonArray(key);
	}

	public JsonObject getJsonObject(K key) {
		return target.getJsonObject(key);
	}

	public String toJSONString() {
		return target.toJSONString();
	}

	@Override
	public String toString() {
		return target.toString();
	}

	@Override
	public int hashCode() {
		return target.hashCode();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof JsonWrapper) {
			return ((JsonWrapper) obj).target.equals(target);
		}

		return target.equals(obj);
	}
}
