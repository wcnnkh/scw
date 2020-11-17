package scw.application;

import scw.beans.BeanFactory;
import scw.beans.Destroy;
import scw.beans.Init;
import scw.event.BasicEventDispatcher;
import scw.logger.Logger;
import scw.util.concurrent.CountLatch;
import scw.value.property.PropertyFactory;

public interface Application extends Init, Destroy, BasicEventDispatcher<ApplicationEvent> {
	void init();
	
	void destroy();
	
	BeanFactory getBeanFactory();

	PropertyFactory getPropertyFactory();

	/**
	 * 是否已经初始化了
	 * 
	 * @return
	 */
	boolean isInitialized();
	
	/**
	 * 初始化时的计数锁，仅在未初始化完时有效
	 * @see #init()
	 * @see #isInitialized()
	 * @return
	 */
	CountLatch getInitializationLatch();

	Logger getLogger();

	ClassLoader getClassLoader();
}
