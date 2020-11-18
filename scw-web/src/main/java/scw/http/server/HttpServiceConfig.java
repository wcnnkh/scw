package scw.http.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import scw.aop.annotation.AopEnable;
import scw.core.Assert;
import scw.core.utils.StringUtils;
import scw.util.DefaultStringMatcher;
import scw.util.StringMatcher;
import scw.util.XUtils;

@AopEnable(false)
public class HttpServiceConfig<V> {
	private volatile Map<String, V> configMap;
	private volatile Map<String, V> patternConfigMap;
	private StringMatcher matcher;

	public final StringMatcher getMatcher() {
		return matcher == null ? DefaultStringMatcher.getInstance() : matcher;
	}

	public void setMatcher(StringMatcher matcher) {
		Assert.requiredArgument(matcher != null, "matcher");
		if (matcher.equals(getMatcher())) {
			return;
		}

		this.matcher = matcher;
		synchronized (this) {
			Map<String, V> map = new HashMap<String, V>();
			if (configMap != null) {
				map.putAll(configMap);
			}

			if (patternConfigMap != null) {
				map.putAll(patternConfigMap);
			}

			clear();
			for (Entry<String, V> entry : map.entrySet()) {
				addMapping(entry.getKey(), entry.getValue());
			}
		}
	}

	public void addMapping(String pattern, V value) {
		Assert.requiredArgument(StringUtils.isNotEmpty(pattern), "pattern");

		synchronized (this) {
			if (getMatcher().isPattern(pattern)) {
				if (patternConfigMap == null) {
					patternConfigMap = new TreeMap<String, V>(XUtils.getComparator(getMatcher()));
				}
				patternConfigMap.put(pattern, value);
			} else {
				if (configMap == null) {
					configMap = new HashMap<String, V>();
				}
				configMap.put(pattern, value);
			}
		}
	}

	public void removeMapping(String pattern) {
		synchronized (this) {
			if (getMatcher().isPattern(pattern)) {
				if (patternConfigMap != null) {
					for (String key : patternConfigMap.keySet()) {
						if (getMatcher().match(pattern, key)) {
							patternConfigMap.remove(key);
						}
					}
				}
			} else {
				if (configMap != null) {
					configMap.remove(pattern);
				}
			}
		}
	}

	public void clear() {
		synchronized (this) {
			if (configMap != null) {
				configMap.clear();
			}

			if (patternConfigMap != null) {
				patternConfigMap.clear();
			}
		}
	}

	public V getConfig(ServerHttpRequest request) {
		V value = null;
		if (configMap != null) {
			value = configMap.get(request.getPath());
		}

		if (value != null) {
			return value;
		}

		if (patternConfigMap != null) {
			for (Entry<String, V> entry : patternConfigMap.entrySet()) {
				if (getMatcher().match(entry.getKey(), request.getPath())) {
					return entry.getValue();
				}
			}
		}
		return value;
	}
}
