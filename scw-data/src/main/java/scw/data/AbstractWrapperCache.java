package scw.data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import scw.core.utils.CollectionUtils;
import scw.transaction.DefaultTransactionLifeCycle;
import scw.transaction.TransactionManager;

public abstract class AbstractWrapperCache<C extends Cache> implements Cache {
	public abstract C getCache();

	public abstract boolean isTransaction();

	public abstract String formatKey(String key);

	public <T> T get(String key) {
		return getCache().get(formatKey(key));
	}

	public <T> Map<String, T> get(Collection<String> keys) {
		Map<String, String> keyMapping = new LinkedHashMap<String, String>();
		for (String key : keys) {
			keyMapping.put(formatKey(key), key);
		}

		Map<String, T> valueMap = getCache().get(keyMapping.keySet());
		Map<String, T> result = new LinkedHashMap<String, T>();
		for (Entry<String, T> entry : valueMap.entrySet()) {
			String key = keyMapping.get(entry.getKey());
			if (key == null) {
				continue;
			}

			result.put(key, entry.getValue());
		}
		return getCache().get(keys);
	}

	public boolean add(String key, Object value) {
		transactionDelete(key);
		return getCache().add(formatKey(key), value);
	}

	public void set(String key, Object value) {
		transactionDelete(key);
		getCache().set(formatKey(key), value);
	}

	public boolean isExist(String key) {
		return getCache().isExist(formatKey(key));
	}

	public boolean delete(String key) {
		return getCache().delete(formatKey(key));
	}

	public void delete(Collection<String> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return;
		}

		Collection<String> ks = new LinkedList<String>();
		for (String key : keys) {
			ks.add(formatKey(key));
		}
		getCache().delete(ks);
	}

	protected void transactionDelete(final String key) {
		if (isTransaction()) {
			TransactionManager.transactionLifeCycle(new DefaultTransactionLifeCycle() {
				@Override
				public void afterRollback() {
					getCache().delete(formatKey(key));
				}
			});
		}
	}
}
