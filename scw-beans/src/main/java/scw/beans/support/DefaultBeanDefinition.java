package scw.beans.support;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import scw.aop.MethodInterceptor;
import scw.aop.Proxy;
import scw.aop.support.ConfigurableMethodInterceptor;
import scw.beans.AopEnableSpi;
import scw.beans.BeanDefinition;
import scw.beans.BeanUtils;
import scw.beans.BeanlifeCycleEvent;
import scw.beans.BeanlifeCycleEvent.Step;
import scw.beans.BeansException;
import scw.beans.ConfigurableBeanFactory;
import scw.beans.RuntimeBean;
import scw.beans.annotation.Bean;
import scw.beans.ioc.Ioc;
import scw.context.support.LifecycleAuxiliary;
import scw.core.parameter.ParameterDescriptors;
import scw.core.utils.ArrayUtils;
import scw.core.utils.StringUtils;
import scw.env.Sys;
import scw.instance.InstanceException;
import scw.instance.support.DefaultInstanceDefinition;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.mapper.FieldFeature;
import scw.mapper.MapperUtils;

public class DefaultBeanDefinition extends DefaultInstanceDefinition
		implements BeanDefinition, Cloneable, AopEnableSpi {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final ConfigurableBeanFactory beanFactory;
	protected Ioc ioc = new Ioc();
	private boolean isNew = true;
	private final ConfigurableMethodInterceptor methodInterceptors = new ConfigurableMethodInterceptor();

	public DefaultBeanDefinition(ConfigurableBeanFactory beanFactory, Class<?> sourceClass) {
		super(beanFactory, beanFactory.getEnvironment(), sourceClass, Sys.env);
		this.beanFactory = beanFactory;
	}

	public void dependence(Object instance) throws BeansException {
		scw.beans.RuntimeBean runtimeBean = BeanUtils.getRuntimeBean(instance);
		if (runtimeBean != null && !runtimeBean._dependence()) {
			return;
		}
		beanFactory.getLifecycleDispatcher().publishEvent(new BeanlifeCycleEvent(this, instance, beanFactory, Step.BEFORE_DEPENDENCE));
		if (instance != null) {
			for (Ioc ioc : Ioc.forClass(instance.getClass())) {
				ioc.getDependence().process(this, instance, beanFactory);
			}
			ioc.getDependence().process(this, instance, beanFactory);

			// @ConfigurationProperties
			configurationProperties(instance);

			BeanUtils.aware(instance, beanFactory, this);
		}
		beanFactory.getLifecycleDispatcher().publishEvent(new BeanlifeCycleEvent(this, instance, beanFactory, Step.AFTER_DEPENDENCE));
	}

	protected void configurationProperties(Object instance) {
		BeanUtils.configurationProperties(instance, getAnnotatedElement(), getEnvironment());
	}

	public void init(Object instance) throws BeansException {
		scw.beans.RuntimeBean runtimeBean = BeanUtils.getRuntimeBean(instance);
		if (runtimeBean != null && !runtimeBean._init()) {
			return;
		}
		beanFactory.getLifecycleDispatcher().publishEvent(new BeanlifeCycleEvent(this, instance, beanFactory, Step.BEFORE_INIT));
		if (instance != null) {
			for (Ioc ioc : Ioc.forClass(instance.getClass())) {
				ioc.getInit().process(this, instance, beanFactory);
			}
			ioc.getInit().process(this, instance, beanFactory);
			try {
				LifecycleAuxiliary.init(instance);
			} catch (Throwable e) {
				throw new BeansException(e);
			}
		}
		beanFactory.getLifecycleDispatcher().publishEvent(new BeanlifeCycleEvent(this, instance, beanFactory, Step.AFTER_INIT));
	}

	public void destroy(Object instance) throws BeansException {
		scw.beans.RuntimeBean runtimeBean = BeanUtils.getRuntimeBean(instance);
		if (runtimeBean != null && !runtimeBean._destroy()) {
			return;
		}

		beanFactory.getLifecycleDispatcher().publishEvent(new BeanlifeCycleEvent(this, instance, beanFactory, Step.BEFORE_DESTROY));
		if (instance != null) {
			try {
				LifecycleAuxiliary.destroy(instance);
			} catch (Throwable e) {
				throw new BeansException(e);
			}
			
			ioc.getDestroy().process(this, instance, beanFactory);
			for (Ioc ioc : Ioc.forClass(instance.getClass())) {
				ioc.getDestroy().process(this, instance, beanFactory);
			}
		}
		beanFactory.getLifecycleDispatcher().publishEvent(new BeanlifeCycleEvent(this, instance, beanFactory, Step.AFTER_DESTROY));
	}

	public String getId() {
		Bean bean = getAnnotatedElement().getAnnotation(Bean.class);
		if (bean == null) {
			return getTargetClass().getName();
		}

		return StringUtils.isEmpty(bean.value()) ? getTargetClass().getName() : bean.value();
	}

	public Collection<String> getNames() {
		Bean bean = getAnnotatedElement().getAnnotation(Bean.class);
		if (bean == null) {
			return Collections.emptyList();
		}

		return Arrays.asList(bean.names());
	}

	public boolean isAopEnable() {
		return isAopEnable(getTargetClass(), getAnnotatedElement());
	}

	public boolean isAopEnable(Class<?> clazz, AnnotatedElement annotatedElement) {
		return !methodInterceptors.isEmpty() || BeanUtils.isAopEnable(clazz, annotatedElement);
	}

	public boolean isSingleton() {
		return BeanUtils.isSingleton(getTargetClass(), getAnnotatedElement());
	}

	public AnnotatedElement getAnnotatedElement() {
		return getTargetClass();
	}

	public ConfigurableMethodInterceptor getMethodInterceptors() {
		return methodInterceptors;
	}

	@Override
	public boolean isInstance() {
		return canCreateInterfaceInsance() || isInstance(isAopEnable());
	}

	private boolean canCreateInterfaceInsance() {
		return getTargetClass().isInterface() && isAopEnable();
	}

	@Override
	public Object create() throws InstanceException {
		if (canCreateInterfaceInsance()) {
			return createProxy(getTargetClass(), null).create();
		}
		return super.create();
	}

	@Override
	protected Object createInternal(Class<?> targetClass, ParameterDescriptors parameterDescriptors, Object[] params) {
		if (isAopEnable(targetClass, getAnnotatedElement())) {
			return createProxyInstance(targetClass, parameterDescriptors.getTypes(), params);
		}
		return super.createInternal(targetClass, parameterDescriptors, params);
	}

	protected Proxy createProxy(Class<?> targetClass, Class<?>[] interfaces) {
		Class<?>[] interfacesToUse = interfaces;
		if (ArrayUtils.isEmpty(interfacesToUse)) {
			interfacesToUse = scw.beans.RuntimeBean.PROXY_INTERFACES;
		} else {
			interfacesToUse = ArrayUtils.merge(interfacesToUse, scw.beans.RuntimeBean.PROXY_INTERFACES);
		}

		MethodInterceptor interceptor = new RuntimeBean.RuntimeBeanMethodInterceptor(this);
		if (methodInterceptors.isEmpty()) {
			return beanFactory.getAop().getProxy(targetClass, interfacesToUse, interceptor);
		}

		ConfigurableMethodInterceptor interceptors = new ConfigurableMethodInterceptor();
		interceptors.addMethodInterceptor(interceptor);
		interceptors.addMethodInterceptor(getMethodInterceptors());
		return beanFactory.getAop().getProxy(targetClass, interfacesToUse, interceptors);
	}

	protected Proxy createInstanceProxy(Object instance, Class<?> targetClass, Class<?>[] interfaces) {
		Class<?>[] interfacesToUse = interfaces;
		if (ArrayUtils.isEmpty(interfacesToUse)) {
			interfacesToUse = RuntimeBean.PROXY_INTERFACES;
		} else {
			interfacesToUse = ArrayUtils.merge(interfacesToUse, RuntimeBean.PROXY_INTERFACES);
		}

		ConfigurableMethodInterceptor interceptors = new ConfigurableMethodInterceptor();
		interceptors.addMethodInterceptor(new RuntimeBean.RuntimeBeanMethodInterceptor(this));
		interceptors.addMethodInterceptor(methodInterceptors);
		return beanFactory.getAop().getProxy(targetClass, instance, interfaces, interceptors);
	}

	protected Object createProxyInstance(Class<?> targetClass, Class<?>[] parameterTypes, Object[] args) {
		if (getTargetClass().isInterface() && methodInterceptors.isEmpty()) {
			logger.warn("empty filter: {}", getTargetClass().getName());
		}

		Proxy proxy = createProxy(targetClass, null);
		return proxy.create(parameterTypes, args);
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	@Override
	public BeanDefinition clone() {
		try {
			DefaultBeanDefinition beanDefinition = (DefaultBeanDefinition) super.clone();
			beanDefinition.setNew(false);
			beanDefinition.ioc.readyOnly();
			return beanDefinition;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	protected String getStringDescribe() {
		return MapperUtils.getMapper().getFields(getClass()).accept(FieldFeature.EXISTING_GETTER_FIELD)
				.getValueMap(this).toString();
	}

	/**
	 * 如果要重写请重写 {@link #getStringDescribe()}
	 */
	@Override
	public final String toString() {
		return getClass().getName() + "[" + getStringDescribe() + "]";
	}
}
