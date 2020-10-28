package scw.data;

import java.util.Collection;
import java.util.Map;

import scw.aop.annotation.AopEnable;

/**
 * 缓存<br/>
 * 只是定义为缓存，并不表示缓存是否是会过期的,由实现方决定
 * @author shuchaowen
 *
 */

@AopEnable(false)
public interface Cache {
	<T> T get(String key);

	<T> Map<String, T> get(Collection<String> keys);

	boolean add(String key, Object value);

	void set(String key, Object value);

	boolean isExist(String key);
	
	boolean delete(String key);

	void delete(Collection<String> keys);
}
