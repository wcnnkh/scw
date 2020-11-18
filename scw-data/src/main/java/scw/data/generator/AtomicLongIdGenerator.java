package scw.data.generator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 生成一个Long类型的ID
 * @author shuchaowen
 *
 */
public final class AtomicLongIdGenerator implements LongIdGenerator{
	private int serverId;
	private int serverCount;
	private AtomicLong maxId;
	
	/**
	 * 初始化ID的值为0
	 * @param initValue
	 */
	public AtomicLongIdGenerator(){
		this(0, 1, 0);
	}
	
	/**
	 * 初始化ID值
	 * @param initValue
	 */
	public AtomicLongIdGenerator(long initValue){
		this(0, 1, initValue);
	}
	
	/**
	 * 初始化
	 * @param serverId 当前服务器ID
	 * @param serverCount 服务器数量
	 * @param initValue 初始值
	 */
	public AtomicLongIdGenerator(int serverId, int serverCount,  long initValue) {
		if(serverId < 0){
			throw new RuntimeException("当前服务ID错误");
		}
		
		if(serverCount < 1){
			throw new RuntimeException("最大服务数量错误");
		}
		
		if(initValue < 0){
			throw new RuntimeException("初始值错误");
		}
		
		this.serverId = serverId;
		this.serverCount = serverCount;
		if(initValue < serverCount + serverId){
			this.maxId = new AtomicLong(initValue);
		}else{
			this.maxId = new AtomicLong((initValue - serverId)/serverCount);
		}
	}
	
	public Long next() {
		return maxId.incrementAndGet() * serverCount + serverId;
	}
}
