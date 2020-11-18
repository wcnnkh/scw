package scw.data;

import scw.core.utils.StringUtils;

public final class WrapperCache extends AbstractWrapperCache<Cache> {
	private final Cache cache;
	private final boolean transaction;
	private final String keyPrefix;

	public WrapperCache(Cache cache, boolean transaction, String keyPrefix) {
		this.cache = cache;
		this.transaction = transaction;
		this.keyPrefix = keyPrefix;
	}

	@Override
	public Cache getCache() {
		return cache;
	}

	@Override
	public boolean isTransaction() {
		return transaction;
	}

	@Override
	public String formatKey(String key) {
		return keyPrefix == null ? key : (keyPrefix + key);
	}

	public <T> java.util.Map<String, T> get(java.util.Collection<String> keys) {
		if (StringUtils.isEmpty(keyPrefix)) {
			return getCache().get(keys);
		}
		return super.get(keys);
	};
}
