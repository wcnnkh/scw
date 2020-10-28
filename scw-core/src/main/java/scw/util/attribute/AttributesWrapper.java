package scw.util.attribute;

import java.util.Enumeration;

public class AttributesWrapper<K, V> implements Attributes<K, V> {
	private final Attributes<K, V> targetAttributes;

	public AttributesWrapper(Attributes<K, V> targetAttributes) {
		this.targetAttributes = targetAttributes;
	}

	public V getAttribute(K name) {
		return targetAttributes.getAttribute(name);
	}

	public Enumeration<K> getAttributeNames() {
		return targetAttributes.getAttributeNames();
	}

	public void setAttribute(K name, V o) {
		targetAttributes.setAttribute(name, o);
	}

	public void removeAttribute(K name) {
		targetAttributes.removeAttribute(name);
	}

	@Override
	public String toString() {
		return targetAttributes.toString();
	}

	@Override
	public int hashCode() {
		return targetAttributes.hashCode();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof AttributesWrapper) {
			return targetAttributes.equals(((AttributesWrapper) obj).targetAttributes);
		}

		return targetAttributes.equals(obj);
	}
}
