package scw.data;

/**
 * 临时缓存
 * 
 * @author shuchaowen
 *
 */
public interface TemporaryCache extends Cache {
	<T> T getAndTouch(String key, int exp);

	boolean touch(String key, int exp);

	/**
	 * 如果数据不存在就添加
	 * @param key
	 * @param exp 过期时间(秒)
	 * @param value
	 * @return
	 */
	boolean add(String key, int exp, Object value);

	/**
	 * @param key
	 * @param exp 过期时间(秒)
	 * @param value
	 */
	void set(String key, int exp, Object value);
}
