package scw.complete;

import java.io.IOException;

import scw.aop.annotation.AopEnable;
import scw.beans.Init;

/**
 * 一个确认服务，保证一定会执行,但不保证会重复执行
 * 
 * @author shuchaowen
 *
 */
@AopEnable(false)
public interface CompleteService extends Init {

	/**
	 * 多次调用会重复执行(由实现方决定，默认的实现会这样@see {@link LocalCompleteService})
	 */
	void init() throws Exception;

	/**
	 * 注册一个任务
	 * 
	 * @see CompleteService#init() 这个任务如果在调用run之前没有取消那么在此方法调用时时尝试执行
	 * 
	 * @param completeTask
	 * @return
	 * @throws IOException
	 */
	Complete register(CompleteTask completeTask) throws Exception;
}
