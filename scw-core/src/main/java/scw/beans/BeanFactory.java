package scw.beans;

import scw.aop.Aop;
import scw.core.instance.InstanceFactory;
import scw.event.BasicEventDispatcher;

public interface BeanFactory extends InstanceFactory {
	BeanDefinition getDefinition(String name);

	BeanDefinition getDefinition(Class<?> clazz);

	Aop getAop();

	/**
	 * bean的生命周期事件
	 * 
	 * @return
	 */
	BasicEventDispatcher<BeanLifeCycleEvent> getBeanLifeCycleEventDispatcher();
}
