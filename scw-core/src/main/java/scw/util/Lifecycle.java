package scw.util;

public interface Lifecycle {
	/**
	 * 初始化前执行
	 * 
	 * @throws Throwable
	 */
	void beforeInit() throws Throwable;

	/**
	 * 初始化后执行
	 * 
	 * @throws Throwable
	 */
	void afterInit() throws Throwable;

	/**
	 * 初始化完成执行(无论成功失败)
	 * 
	 */
	void initComplete() throws Throwable;

	/**
	 * 销毁前执行
	 * 
	 * @throws Throwable
	 */
	void beforeDestroy() throws Throwable;

	/**
	 * 销毁后执行
	 * 
	 * @throws Throwable
	 */
	void afterDestroy() throws Throwable;

	/**
	 * 销毁完成执行(无论成功失败)
	 * 
	 */
	void destroyComplete() throws Throwable;
}
