package scw.memcached.x;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.rubyeye.xmemcached.GetsResponse;
import net.rubyeye.xmemcached.MemcachedClient;
import scw.data.cas.CAS;
import scw.data.cas.CASOperations;

public class XMemcachedCASOperations implements CASOperations {
	private final MemcachedClient memcachedClient;

	public XMemcachedCASOperations(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}

	public boolean cas(String key, Object value, int exp, long cas) {
		try {
			return memcachedClient.cas(key, exp, value, cas);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean delete(String key, long cas) {
		try {
			return memcachedClient.delete(key, cas, memcachedClient.getOpTimeout());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public <T> CAS<T> get(String key) {
		GetsResponse<T> cas;
		try {
			cas = memcachedClient.gets(key);
			if (cas == null) {
				return null;
			}

			return new CAS<T>(cas.getCas(), cas.getValue());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void set(String key, Object value, int exp) {
		if (value == null) {
			return;
		}

		try {
			memcachedClient.set(key, exp, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean delete(String key) {
		try {
			return memcachedClient.delete(key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean add(String key, Object value, int exp) {
		try {
			return memcachedClient.add(key, exp, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public <T> Map<String, CAS<T>> get(Collection<String> keys) {
		Map<String, GetsResponse<T>> map = null;
		try {
			map = memcachedClient.gets(keys);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (map != null) {
			Map<String, CAS<T>> casMap = new HashMap<String, CAS<T>>();
			for (Entry<String, GetsResponse<T>> entry : map.entrySet()) {
				GetsResponse<T> v = entry.getValue();
				casMap.put(entry.getKey(), new CAS<T>(v.getCas(), v.getValue()));
			}
			return casMap;
		}
		return null;
	}

}
