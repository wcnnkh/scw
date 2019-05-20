package scw.data.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisCommands<K, V> {

	V get(K key);

	List<V> mget(K... keys);

	/**
	 * 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。
	 * 对于某个原本带有生存时间（TTL）的键来说， 当 SET 命令成功在这个键上执行时，
	 * 这个键原有的 TTL 将被清除。
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	void set(K key, V value);

	/**
	 * 只在键 key 不存在的情况下， 将键 key 的值设置为 value 。
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	boolean setnx(K key, V value);

	/**
	 * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)。
	 * 
	 * 如果 key 已经存在， SETEX 命令将覆写旧值。
	 * 
	 * 这个命令类似于以下两个命令：
	 * 
	 * SET key value EXPIRE key seconds # 设置生存时间
	 * 
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 */
	void setex(K key, int seconds, V value);

	boolean exists(K key);

	Long expire(K key, int seconds);

	boolean del(K key);

	Long hset(K key, K field, V value);

	Long hsetnx(K key, K field, V value);

	Long hdel(K key, K... fields);

	Long hlen(K key);

	Boolean hexists(K key, K field);

	Long ttl(K key);

	Long incr(K key);

	Long decr(K key);

	Collection<V> hvals(K key);

	V hget(K key, K field);

	Collection<V> hmget(K key, K... fields);

	Long lpush(K key, V... values);

	Long rpush(K key, V... values);

	V rpop(K key);

	V lpop(K key);

	Set<V> smembers(K key);

	Long srem(K key, V... members);

	Long sadd(K key, V... members);

	Long zadd(K key, long score, V member);

	/**
	 * EX second ：设置键的过期时间为 second 秒。 SET key value EX second 效果等同于 SETEX key
	 * second value 。 PX millisecond ：设置键的过期时间为 millisecond 毫秒。 SET key value PX
	 * millisecond 效果等同于 PSETEX key millisecond value 。 NX ：只在键不存在时，才对键进行设置操作。
	 * SET key value NX 效果等同于 SETNX key value 。 XX ：只在键已经存在时，才对键进行设置操作。
	 * 
	 * @param key
	 * @param value
	 * @param nxxx
	 * @param expe
	 * @param time
	 * @return
	 */
	Boolean set(K key, V value, K nxxx, K expx, long time);

	Boolean sIsMember(K key, V member);

	V lindex(K key, int index);

	Long llen(K key);

	Object eval(K script, List<K> keys, List<V> args);

	Map<K, V> hgetAll(K key);

	List<V> brpop(int timeout, K key);

	List<V> blpop(int timeout, K key);

	Boolean hmset(K key, Map<K, V> hash);
}
