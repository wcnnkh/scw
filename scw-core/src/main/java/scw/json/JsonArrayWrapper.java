package scw.json;

import java.util.Iterator;

public class JsonArrayWrapper extends JsonWrapper<Integer> implements JsonArray {
	private final JsonArray target;

	public JsonArrayWrapper(JsonArray target) {
		super(target);
		this.target = target;
	}

	public Iterator<JsonElement> iterator() {
		return target.iterator();
	}

	public boolean add(Object element) {
		return target.add(element);
	}

	public boolean remove(int index) {
		return target.remove(index);
	}
}
