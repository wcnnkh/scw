package scw.json.parser;

import java.io.Serializable;
import java.util.Iterator;

import scw.core.Converter;
import scw.core.IteratorConverter;
import scw.json.AbstractJson;
import scw.json.JSONException;
import scw.json.JsonArray;
import scw.json.JsonElement;

public class DefaultJsonArray extends AbstractJson<Integer> implements JsonArray, Converter<Object, JsonElement>, Serializable{
	private static final long serialVersionUID = 1L;
	private SimpleJSONArray simpleJSONArray;
	
	public DefaultJsonArray(String text){
		Object json = JSONValue.parse(text);
		if(json instanceof SimpleJSONArray){
			this.simpleJSONArray = (SimpleJSONArray) json;
		}else{
			throw new JSONException("This is not a JSON array:" + text);
		}
	}
	
	public int size() {
		return simpleJSONArray.size();
	}

	public String toJSONString() {
		return simpleJSONArray.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public Iterator<JsonElement> iterator() {
		return new IteratorConverter<Object, JsonElement>(simpleJSONArray.iterator(), this);
	}
	
	public JsonElement convert(Object k) {
		return new DefaultJsonElement(k);
	}

	public JsonElement getValue(Integer index) {
		Object value = simpleJSONArray.get(index);
		return value == null? null:convert(value);
	}

	@SuppressWarnings("unchecked")
	public boolean add(Object element) {
		return simpleJSONArray.add(element);
	}

	public boolean remove(int index) {
		return simpleJSONArray.remove(index) != null;
	}

}
