package scw.value;

public interface BaseValueFactory<K> {
	Value get(K key);
}