package scw.data;

import scw.core.utils.StringUtils;

public final class WrapperTemporaryCache extends AbstractWrapperTemporaryCache<TemporaryCache> {
	private String keyPrefix;
	private TemporaryCache cache;
	private boolean transaction;

	public WrapperTemporaryCache(TemporaryCache cache, boolean transaction, String keyPrefix) {
		this.cache = cache;
		this.transaction = transaction;
		this.keyPrefix = keyPrefix;
	}

	@Override
	public TemporaryCache getCache() {
		return cache;
	}

	@Override
	public boolean isTransaction() {
		return transaction;
	}

	@Override
	public String formatKey(String key) {
		return key == null ? key : (keyPrefix + key);
	}

	public <T> java.util.Map<String, T> get(java.util.Collection<String> keys) {
		if (StringUtils.isEmpty(keyPrefix)) {
			return getCache().get(keys);
		}
		return super.get(keys);
	};
}
