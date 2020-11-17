package scw.beans;

import scw.event.ObjectEvent;
import scw.value.property.PropertyFactory;

/**
 * 生命周期事件
 * @author shuchaowen
 *
 */
public class BeanLifeCycleEvent extends ObjectEvent<Object>{
	private static final long serialVersionUID = 1L;
	private transient final PropertyFactory propertyFactory;
	private transient final BeanFactory beanFactory;
	private final Step step;
	private final BeanDefinition beanDefinition;

	public BeanLifeCycleEvent(BeanDefinition beanDefinition, Object source, BeanFactory beanFactory, PropertyFactory propertyFactory, Step step) {
		super(source);
		this.beanFactory = beanFactory;
		this.beanDefinition = beanDefinition;
		this.propertyFactory = propertyFactory;
		this.step = step;
	}
	
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public PropertyFactory getPropertyFactory() {
		return propertyFactory;
	}
	
	public Step getStep() {
		return step;
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	public static enum Step{
		BEFORE_DEPENDENCE,
		AFTER_DEPENDENCE,
		BEFORE_INIT,
		AFTER_INIT,
		BEFORE_DESTROY,
		AFTER_DESTROY;
	}
}
