package scw.data;

import java.util.Collection;
import java.util.Map;

public class TemporaryExpiredCache implements ExpiredCache {
	private TemporaryCache temporaryCache;
	private int exp;

	public TemporaryExpiredCache(TemporaryCache temporaryCache, int exp) {
		this.temporaryCache = temporaryCache;
		this.exp = exp;
	}

	public <T> T get(String key) {
		return temporaryCache.get(key);
	}

	public <T> Map<String, T> get(Collection<String> keys) {
		return temporaryCache.get(keys);
	}

	public boolean add(String key, Object value) {
		return temporaryCache.add(key, getMaxExpirationDate(), value);
	}

	public void set(String key, Object value) {
		temporaryCache.set(key, getMaxExpirationDate(), value);
	}

	public boolean isExist(String key) {
		return temporaryCache.isExist(key);
	}

	public boolean delete(String key) {
		return temporaryCache.delete(key);
	}

	public void delete(Collection<String> keys) {
		temporaryCache.delete(keys);
	}

	public <T> T getAndTouch(String key) {
		return temporaryCache.getAndTouch(key, getMaxExpirationDate());
	}

	public boolean touch(String key) {
		return temporaryCache.touch(key, getMaxExpirationDate());
	}

	public int getMaxExpirationDate() {
		return exp;
	}

}
