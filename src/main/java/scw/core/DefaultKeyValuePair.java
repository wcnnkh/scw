package scw.core;

import java.io.Serializable;

public class DefaultKeyValuePair<K, V> implements Serializable, KeyValuePair<K, V> {
	private static final long serialVersionUID = 1L;
	private K key;
	private V value;

	public DefaultKeyValuePair() {
	};

	public DefaultKeyValuePair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public void setValue(V value) {
		this.value = value;
	}
}
