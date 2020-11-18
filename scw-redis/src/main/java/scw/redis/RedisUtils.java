package scw.redis;

import java.util.Arrays;
import java.util.List;

import scw.core.Constants;
import scw.io.ResourceUtils;
import scw.value.AnyValue;

public final class RedisUtils {
	private static final String INCR_AND_INIT_SCRIPT = ResourceUtils.getContent("classpath:/scw/data/redis/incr.script",
			Constants.UTF_8);
	private static final String DECR_AND_INIT_SCRIPT = ResourceUtils.getContent("classpath:/scw/data/redis/decr.script",
			Constants.UTF_8);

	public static String notNullScript(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" ~= nil");
		sb.append(" or (type(").append(name).append(") == 'boolean' and ").append(name).append(")");
		return sb.toString();
	}

	public static String isNullScript(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" == nil");
		sb.append(" or (type(").append(name).append(") == 'boolean' and ").append(name).append(" == false)");
		return sb.toString();
	}

	public static long incr(RedisScriptOperations<String, String> redisScriptOperations, String key, long delta,
			long initialValue, int exp) {
		AnyValue[] values = redisScriptOperations.eval(INCR_AND_INIT_SCRIPT, Arrays.asList(key),
				Arrays.asList(String.valueOf(delta), String.valueOf(initialValue), String.valueOf(exp)));
		return values.length == 0 ? 0 : values[0].getAsLongValue();
	}

	public static long decr(RedisScriptOperations<String, String> redisScriptOperations, String key, long delta,
			long initialValue, int exp) {
		AnyValue[] values = redisScriptOperations.eval(DECR_AND_INIT_SCRIPT, Arrays.asList(key),
				Arrays.asList(String.valueOf(delta), String.valueOf(initialValue), String.valueOf(exp)));
		return values.length == 0 ? 0 : values[0].getAsLongValue();
	}

	@SuppressWarnings("rawtypes")
	public static AnyValue[] wrapper(Object value) {
		if (value == null) {
			return new AnyValue[0];
		}

		if (value instanceof List) {
			List list = (List) value;
			if (list.isEmpty()) {
				return new AnyValue[0];
			}

			AnyValue[] values = new AnyValue[list.size()];
			for (int i = 0; i < values.length; i++) {
				values[i] = new AnyValue(list.get(i));
			}
			return values;
		} else {
			return new AnyValue[] { new AnyValue(value) };
		}
	}
}
