package scw.data;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MapCache extends AbstractMapCache implements Cache, Serializable {
	private static final long serialVersionUID = 1L;
	private Map<String, Object> map = new ConcurrentHashMap<String, Object>();

	@Override
	public Map<String, Object> getMap() {
		return map;
	}

	@Override
	protected Map<String, Object> createMap() {
		return map;
	}
}
