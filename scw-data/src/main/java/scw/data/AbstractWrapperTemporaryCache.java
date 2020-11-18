package scw.data;

public abstract class AbstractWrapperTemporaryCache<C extends TemporaryCache> extends AbstractWrapperCache<C>
		implements TemporaryCache {

	public <T> T getAndTouch(String key, int exp) {
		return getCache().getAndTouch(formatKey(key), exp);
	}

	public boolean touch(String key, int exp) {
		return getCache().touch(formatKey(key), exp);
	}

	public boolean add(String key, int exp, Object value) {
		transactionDelete(key);
		return getCache().add(formatKey(key), exp, value);
	}

	public void set(String key, int exp, Object value) {
		transactionDelete(key);
		getCache().set(formatKey(key), exp, value);
	}

}
