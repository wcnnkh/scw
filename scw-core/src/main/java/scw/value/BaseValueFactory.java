package scw.value;

public interface BaseValueFactory<K> {
	Value getValue(K key);
}
