package scw.data.generator;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生成一个int类型的ID
 * @author shuchaowen
 *
 */
public final class IntegerIdGenerator implements IdGenerator<Integer>{
	private int serverId;
	private int serverCount;
	private AtomicInteger maxId;

	/**
	 * 初始化ID的值为0
	 * @param initValue
	 */
	public IntegerIdGenerator(){
		this(0, 1, 0);
	}
	
	/**
	 * 初始化ID值
	 * @param initValue
	 */
	public IntegerIdGenerator(int initValue){
		this(0, 1, initValue);
	}
	
	/**
	 * 初始化
	 * @param serverId 当前服务器ID
	 * @param serverCount 服务器数量
	 * @param initValue 初始值
	 */
	public IntegerIdGenerator(int serverId, int serverCount,  int initValue) {
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
			this.maxId = new AtomicInteger(initValue);
		}else{
			this.maxId = new AtomicInteger((initValue - serverId)/serverCount);
		}
	}
	
	public Integer next() {
		return maxId.incrementAndGet() * serverCount + serverId;
	}
}
