package scw.redis.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;
import scw.redis.enums.EXPX;
import scw.redis.enums.NXXX;

public class JedisUtils {
	private static final String OK = "OK";

	public static boolean isOK(String value) {
		return OK.equals(value);
	}

	public static SetParams parseSetParams(NXXX nxxx, EXPX expx, long time) {
		SetParams setParams = new SetParams();
		if (nxxx == NXXX.NX) {
			setParams.nx();
		} else if (nxxx == NXXX.XX) {
			setParams.xx();
		}

		if (expx == EXPX.EX) {
			setParams.ex((int) time);
		} else if (expx == EXPX.PX) {
			setParams.px(time);
		}
		return setParams;
	}

	public static void flushAll(JedisResourceFactory jedisResourceFactory) {
		Jedis jedis = null;
		try {
			jedis = jedisResourceFactory.getResource();
			jedis.flushAll();
		} catch (Exception e) {
			jedisResourceFactory.release(jedis);
		}
	}
}
