package scw.data;

import scw.aop.annotation.AopEnable;

/**
 * 计数器
 * @author shuchaowen
 *
 */

@AopEnable(false)
public interface Counter {
	boolean isExist(String key);
	
	long incr(String key, long delta);

	long incr(String key, long delta, long initialValue);

	long decr(String key, long delta);

	long decr(String key, long delta, long initialValue);
}
