package scw.data.cas;

import java.util.Collection;
import java.util.Map;

/**
 * 定义CAS的操作
 * 
 * @author shuchaowen
 *
 * @param <K>
 * @param <V>
 */

public interface CASOperations {
	boolean cas(String key, Object value, int exp, long cas);

	boolean delete(String key, long cas);

	<T> CAS<T> get(String key);

	void set(String key, Object value, int exp);

	boolean delete(String key);

	boolean add(String key, Object value, int exp);

	<T> Map<String, CAS<T>> get(Collection<String> keys);
}
