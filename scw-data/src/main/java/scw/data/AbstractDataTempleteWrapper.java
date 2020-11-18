package scw.data;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractDataTempleteWrapper implements DataTemplete {
	public abstract DataTemplete getDataTemplete();

	public <T> T get(String key) {
		return getDataTemplete().get(key);
	}

	public <T> T getAndTouch(String key, int newExp) {
		return getDataTemplete().getAndTouch(key, newExp);
	}

	public void set(String key, Object value) {
		getDataTemplete().set(key, value);
	}

	public void set(String key, int exp, Object value) {
		getDataTemplete().set(key, exp, value);
	}

	public boolean add(String key, Object value) {
		return getDataTemplete().add(key, value);
	}

	public boolean add(String key, int exp, Object value) {
		return getDataTemplete().add(key, exp, value);
	}

	public boolean touch(String key, int exp) {
		return getDataTemplete().touch(key, exp);
	}

	public <T> Map<String, T> get(Collection<String> keyCollections) {
		return getDataTemplete().get(keyCollections);
	}

	public boolean delete(String key) {
		return getDataTemplete().delete(key);
	}

	public boolean isExist(String key) {
		return getDataTemplete().isExist(key);
	}

	public long incr(String key, long delta) {
		return getDataTemplete().incr(key, delta);
	}

	public long incr(String key, long delta, long initialValue) {
		return getDataTemplete().incr(key, delta, initialValue);
	}

	public long incr(String key, long delta, long initialValue, int exp) {
		return getDataTemplete().incr(key, delta, initialValue, exp);
	}

	public long decr(String key, long delta) {
		return getDataTemplete().decr(key, delta);
	}

	public long decr(String key, long delta, long initialValue) {
		return getDataTemplete().decr(key, delta, initialValue);
	}

	public long decr(String key, long delta, long initialValue, int exp) {
		return getDataTemplete().decr(key, delta, initialValue, exp);
	}

	public void delete(Collection<String> keys) {
		getDataTemplete().delete(keys);
	}
}
