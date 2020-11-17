package scw.compatible.map;

import java.util.Map;

import scw.lang.RequiredJavaVersion;

@RequiredJavaVersion(8)
public class Java8MapWrapper implements MapCompatible {

	public <K, V> CompatibleMap<K, V> wrapper(Map<K, V> map) {
		return new Java8CompatibleMap<K, V>(map);
	}

	public static final class Java8CompatibleMap<K, V> extends DefaultCompatibleMap<K, V> {

		public Java8CompatibleMap(Map<K, V> targetMap) {
			super(targetMap);
		}

		@Override
		public V putIfAbsent(K key, V value) {
			return getTargetMap().putIfAbsent(key, value);
		}
	}
}
