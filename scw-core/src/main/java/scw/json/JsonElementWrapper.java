package scw.json;

import scw.value.ValueWrapper;

public class JsonElementWrapper extends ValueWrapper implements JsonElement {
	private final JsonElement target;

	public JsonElementWrapper(JsonElement target) {
		super(target);
		this.target = target;
	}

	public JsonArray getAsJsonArray() {
		return target.getAsJsonArray();
	}

	public JsonObject getAsJsonObject() {
		return target.getAsJsonObject();
	}

	public boolean isJsonArray() {
		return target.isJsonArray();
	}

	public boolean isJsonObject() {
		return target.isJsonObject();
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
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		if(obj instanceof JsonElementWrapper){
			return target.equals(((JsonElementWrapper) obj).target);
		}
		
		return target.equals(obj);
	}
}
