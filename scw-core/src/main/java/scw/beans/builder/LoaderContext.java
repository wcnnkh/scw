package scw.beans.builder;

import scw.beans.BeanFactory;
import scw.util.attribute.DefaultAttributes;
import scw.value.property.PropertyFactory;

public class LoaderContext extends DefaultAttributes<Object, Object> {
	private final Class<?> targetClass;
	private final BeanFactory beanFactory;
	private final PropertyFactory propertyFactory;
	private final LoaderContext parentContext;

	public LoaderContext(Class<?> targetClass, BeanFactory beanFactory,
			PropertyFactory propertyFactory, LoaderContext parentContext) {
		this.targetClass = targetClass;
		this.beanFactory = beanFactory;
		this.propertyFactory = propertyFactory;
		this.parentContext = parentContext;
	}

	public LoaderContext(Class<?> targetClass, LoaderContext parentContext) {
		this(targetClass, parentContext.getBeanFactory(), parentContext
				.getPropertyFactory(), parentContext);
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public PropertyFactory getPropertyFactory() {
		return propertyFactory;
	}

	public LoaderContext getParentContext() {
		return parentContext;
	}
}
