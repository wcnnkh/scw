package scw.util;

import java.io.Serializable;

import scw.core.utils.ObjectUtils;
import scw.lang.Ignore;

@Ignore
public class KeyValuePair<K, V> implements Serializable {
	private static final long serialVersionUID = 1L;
	private K key;
	private V value;

	public KeyValuePair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int code = 0;
		if (key != null) {
			code += key.hashCode();
		}

		if (value != null) {
			code += value.hashCode();
		}
		return code;
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

		if (obj instanceof KeyValuePair) {
			return ObjectUtils.nullSafeEquals(((KeyValuePair) obj).getKey(), getKey())
					&& ObjectUtils.nullSafeEquals(((KeyValuePair) obj).getValue(), getValue());
		}
		return false;
	}

	@Override
	public String toString() {
		return "{" + getKey() + ":" + getValue() + "}";
	}
}
