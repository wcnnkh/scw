package scw.data;

/**
 * 一个会过期的缓存，过期时间由实现者处理
 * @author shuchaowen
 *
 */
public interface ExpiredCache extends Cache {
	<T> T getAndTouch(String key);

	boolean touch(String key);

	/**
	 * 返回最大过期时间<br/>
	 * 单位:秒
	 * 
	 * @return
	 */
	int getMaxExpirationDate();
}
