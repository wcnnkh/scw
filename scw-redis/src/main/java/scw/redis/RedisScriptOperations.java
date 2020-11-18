package scw.redis;

import java.util.List;

import scw.value.AnyValue;

public interface RedisScriptOperations<K, V> {
	AnyValue[] eval(K script, List<K> keys, List<V> args);
}
