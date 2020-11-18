package scw.data.cas;

import java.util.LinkedHashMap;
import java.util.Map;

import scw.core.Callable;

public final class CasUtils {
	private CasUtils() {
	};

	@SuppressWarnings("unchecked")
	private static <K, V> boolean casMapPut(CASOperations casOperations, String key, K field, V value,
			Callable<Map<K, V>> createCallable) {
		CAS<Object> cas = casOperations.get(key);
		if (cas == null) {
			Map<K, V> valueMap = createCallable.call();
			valueMap.put(field, value);
			return casOperations.cas(key, valueMap, 0, 0);
		} else {
			Map<K, V> valueMap = (Map<K, V>) cas.getValue();
			valueMap.put(field, value);
			return casOperations.cas(key, valueMap, 0, cas.getCas());
		}
	}

	@SuppressWarnings("unchecked")
	private static <K, V> boolean casMapRemove(CASOperations casOperations, String key, K field,
			Callable<Map<K, V>> createCallable) {
		CAS<Object> cas = casOperations.get(key);
		if (cas == null) {
			Map<K, V> valueMap = createCallable.call();
			return casOperations.cas(key, valueMap, 0, 0);
		} else {
			Map<K, V> valueMap = (Map<K, V>) cas.getValue();
			valueMap.remove(field);
			return casOperations.cas(key, valueMap, 0, cas.getCas());
		}
	}

	public static <K, V> void mapRemove(CASOperations casOperations, String key, K field,
			Callable<Map<K, V>> createCallable) {
		while (casMapRemove(casOperations, key, field, createCallable)) {
			break;
		}
	}

	public static <K, V> void mapPut(CASOperations casOperations, String key, K field, V value,
			Callable<Map<K, V>> createCallable) {
		while (casMapPut(casOperations, key, field, value, createCallable)) {
			break;
		}
	}

	public static <K, V> void mapRemove(CASOperations casOperations, String key, K field) {
		mapRemove(casOperations, key, field, new CreateLinkedHashMapCallable<K, V>());
	}

	public static <K, V> void mapPut(CASOperations casOperations, String key, K field, V value) {
		mapPut(casOperations, key, field, value, new CreateLinkedHashMapCallable<K, V>());
	}

	private static class CreateLinkedHashMapCallable<K, V> implements Callable<Map<K, V>> {

		public LinkedHashMap<K, V> call() {
			return new LinkedHashMap<K, V>();
		}
	}
}
