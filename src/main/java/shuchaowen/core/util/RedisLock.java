package shuchaowen.core.util;

import java.util.Collections;

import shuchaowen.core.cache.Redis;

public class RedisLock {
	private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
	private static final Long UNLOCK_SUCCESS_RESULT = 1L;
	private final Redis redis;
	private final String key;
	private final int timeout;
	private final String id;
	
	public RedisLock(Redis redis, String key, String id,  int timeout){
		this.redis = redis;
		this.key = key;
		this.timeout = timeout;
		this.id = id;
	}
	
	/**
	 * 尝试获取锁，会立刻得到结果
	 * @return
	 */
	public boolean lock(){
		return redis.set(key, id, "NX", "EX", timeout);
	}
	
	/**
	 * 尝试获取锁，如果无法获取会一直阻塞直到获取到锁
	 */
	public void lockWait(){
		while(!lock()){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return 返回值是可以忽略的，如果返回fasle可能是key已经失效或已经解锁
	 */
	public boolean unLock(){
        Object result = redis.eval(UNLOCK_SCRIPT, Collections.singletonList(key), Collections.singletonList(id));
        return UNLOCK_SUCCESS_RESULT.equals(result);
	}
}
