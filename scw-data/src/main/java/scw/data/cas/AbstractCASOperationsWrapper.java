package scw.data.cas;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractCASOperationsWrapper implements CASOperations {

	public abstract CASOperations getTargetCasOperations();

	public boolean cas(String key, Object value, int exp, long cas) {
		return getTargetCasOperations().cas(key, value, exp, cas);
	}

	public boolean delete(String key, long cas) {
		return getTargetCasOperations().delete(key, cas);
	}

	public <T> CAS<T> get(String key) {
		return getTargetCasOperations().get(key);
	}

	public void set(String key, Object value, int exp) {
		getTargetCasOperations().set(key, value, exp);
	}

	public boolean delete(String key) {
		return getTargetCasOperations().delete(key);
	}

	public boolean add(String key, Object value, int exp) {
		return getTargetCasOperations().add(key, value, exp);
	}

	public <T> Map<String, CAS<T>> get(Collection<String> keys) {
		return getTargetCasOperations().get(keys);
	}

}
