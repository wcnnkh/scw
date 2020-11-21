package scw.json;

import scw.value.AbstractStringValue;

public abstract class AbstractJsonElement extends AbstractStringValue implements JsonElement {

	public AbstractJsonElement(JsonElement defaultValue) {
		super(defaultValue);
	}
	
	@Override
	public JsonElement getDefaultValue() {
		return (JsonElement) super.getDefaultValue();
	}
	
	@Override
	public String toString() {
		return toJSONString();
	}
}
