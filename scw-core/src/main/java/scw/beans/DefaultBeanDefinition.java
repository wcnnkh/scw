package scw.beans;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import scw.aop.MethodInterceptor;
import scw.aop.MethodInterceptors;
import scw.aop.Proxy;
import scw.aop.ProxyUtils;
import scw.beans.BeanLifeCycleEvent.Step;
import scw.beans.annotation.Bean;
import scw.beans.annotation.ConfigurationProperties;
import scw.beans.builder.LoaderContext;
import scw.beans.ioc.Ioc;
import scw.core.ResolvableType;
import scw.core.instance.DefaultInstanceBuilder;
import scw.core.utils.ArrayUtils;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.mapper.Field;
import scw.mapper.Fields;
import scw.mapper.FilterFeature;
import scw.mapper.MapperUtils;
import scw.util.StringMatcher;
import scw.value.Value;
import scw.value.property.PropertyFactory;

public class DefaultBeanDefinition extends DefaultInstanceBuilder<Object> implements BeanDefinition, Cloneable {
	protected final Logger logger = LoggerUtils.getLogger(getClass());
	protected final BeanFactory beanFactory;
	protected final PropertyFactory propertyFactory;
	protected Ioc ioc = new Ioc();
	private boolean isNew = true;

	public DefaultBeanDefinition(BeanFactory beanFactory, PropertyFactory propertyFactory, Class<?> targetClass) {
		super(beanFactory, propertyFactory, targetClass);
		this.beanFactory = beanFactory;
		this.propertyFactory = propertyFactory;
	}

	public DefaultBeanDefinition(LoaderContext loaderContext) {
		this(loaderContext.getBeanFactory(), loaderContext.getPropertyFactory(), loaderContext.getTargetClass());
	}

	public void dependence(Object instance) throws Throwable {
		scw.beans.RuntimeBean runtimeBean = BeanUtils.getRuntimeBean(instance);
		if (runtimeBean != null && !runtimeBean._dependence()) {
			return;
		}
		beanFactory.getBeanLifeCycleEventDispatcher().publishEvent(
				new BeanLifeCycleEvent(this, instance, beanFactory, propertyFactory, Step.BEFORE_DEPENDENCE));
		if (instance != null) {
			for (Ioc ioc : Ioc.forClass(instance.getClass())) {
				ioc.getDependence().process(this, instance, beanFactory, propertyFactory);
			}
			ioc.getDependence().process(this, instance, beanFactory, propertyFactory);

			// @ConfigurationProperties
			ConfigurationProperties configurationProperties = instance.getClass()
					.getAnnotation(ConfigurationProperties.class);
			if (configurationProperties != null) {
				configurationProperties(configurationProperties, instance);
			}

			configurationProperties = getAnnotatedElement().getAnnotation(ConfigurationProperties.class);
			if (configurationProperties != null) {
				configurationProperties(configurationProperties, instance);
			}

			BeanUtils.aware(instance, beanFactory, this);
		}
		beanFactory.getBeanLifeCycleEventDispatcher().publishEvent(
				new BeanLifeCycleEvent(this, instance, beanFactory, propertyFactory, Step.AFTER_DEPENDENCE));
	}
	
	private void appendMapFieldValue(Map<String, Object> mapValue, String key, ResolvableType resolvableType){
		for(Entry<String, Value> entry : propertyFactory.getByMatcher(key, StringMatcher.PREFIX_MATCHER).entrySet()){
			if(entry.getKey().equals(key)){
				continue;
			}
			
			String fieldKey = entry.getKey().substring(key.length() + 1);
			mapValue.put(fieldKey, entry.getValue().getAsObject(resolvableType.getGeneric(1).getType()));
		}
	}

	protected void configurationProperties(ConfigurationProperties configurationProperties, Object instance) {
		String prefix = configurationProperties.prefix();
		if(StringUtils.isEmpty(prefix)){
			prefix = configurationProperties.value();
		}
		
		prefix = StringUtils.isEmpty(prefix)? "":(prefix + ".");
		
		Fields fields = MapperUtils.getMapper().getFields(instance.getClass(), FilterFeature.SETTER_IGNORE_STATIC, FilterFeature.EXISTING_SETTER_FIELD);
		for(Field field : fields){
			if(field.getSetter().getType() == Map.class){
				ResolvableType resolvableType = ResolvableType.forType(field.getSetter().getGenericType());
				if(resolvableType.getGeneric(0).getRawClass() == String.class){
					//这是一个key是String的map
					Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
					appendMapFieldValue(valueMap, prefix + field.getSetter().getName(), resolvableType);
					appendMapFieldValue(valueMap, prefix + StringUtils.humpNamingReplacement(field.getSetter().getName(), "-"), resolvableType);
					if(!valueMap.isEmpty()){
						field.getSetter().set(instance, valueMap);
						continue;
					}
				}
			}
			
			Value value = propertyFactory.getValue(prefix + field.getSetter().getName());
			if(value == null || value.isEmpty()){
				value = propertyFactory.getValue(prefix + StringUtils.humpNamingReplacement(field.getSetter().getName(), "-"));
			}
			
			if(value != null && !value.isEmpty()){
				field.getSetter().set(instance, value.getAsObject(field.getSetter().getGenericType()));
			}
		}
	}

	public void init(Object instance) throws Throwable {
		scw.beans.RuntimeBean runtimeBean = BeanUtils.getRuntimeBean(instance);
		if (runtimeBean != null && !runtimeBean._init()) {
			return;
		}
		beanFactory.getBeanLifeCycleEventDispatcher()
				.publishEvent(new BeanLifeCycleEvent(this, instance, beanFactory, propertyFactory, Step.BEFORE_INIT));
		if (instance != null) {
			for (Ioc ioc : Ioc.forClass(instance.getClass())) {
				ioc.getInit().process(this, instance, beanFactory, propertyFactory);
			}
			ioc.getInit().process(this, instance, beanFactory, propertyFactory);
			BeanUtils.init(instance);
		}
		beanFactory.getBeanLifeCycleEventDispatcher()
				.publishEvent(new BeanLifeCycleEvent(this, instance, beanFactory, propertyFactory, Step.AFTER_INIT));
	}

	public void destroy(Object instance) throws Throwable {
		scw.beans.RuntimeBean runtimeBean = BeanUtils.getRuntimeBean(instance);
		if (runtimeBean != null && !runtimeBean._destroy()) {
			return;
		}

		beanFactory.getBeanLifeCycleEventDispatcher().publishEvent(
				new BeanLifeCycleEvent(this, instance, beanFactory, propertyFactory, Step.BEFORE_DESTROY));
		if (instance != null) {
			BeanUtils.destroy(instance);
			ioc.getDestroy().process(this, instance, beanFactory, propertyFactory);
			for (Ioc ioc : Ioc.forClass(instance.getClass())) {
				ioc.getDestroy().process(this, instance, beanFactory, propertyFactory);
			}
		}
		beanFactory.getBeanLifeCycleEventDispatcher()
				.publishEvent(new BeanLifeCycleEvent(this, instance, beanFactory, propertyFactory, Step.AFTER_DESTROY));
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

	protected boolean isProxy() {
		return ProxyUtils.isAopEnable(getTargetClass(), getTargetClass());
	}

	public boolean isSingleton() {
		return BeanUtils.isSingleton(getTargetClass(), getAnnotatedElement());
	}

	public AnnotatedElement getAnnotatedElement() {
		return getTargetClass();
	}

	public Iterable<? extends MethodInterceptor> getFilters() {
		return null;
	}

	@Override
	public boolean isInstance() {
		return super.isInstance(isProxy() && !CollectionUtils.isEmpty(getFilters()));
	}

	@Override
	protected Object createInternal(Class<?> targetClass, Constructor<? extends Object> constructor, Object[] params)
			throws Exception {
		if (isProxy()) {
			return createProxyInstance(targetClass, constructor.getParameterTypes(), params);
		}
		return super.createInternal(targetClass, constructor, params);
	}

	protected Proxy createProxy(Class<?> targetClass, Class<?>[] interfaces) {
		Class<?>[] interfacesToUse = interfaces;
		if (ArrayUtils.isEmpty(interfacesToUse)) {
			interfacesToUse = scw.beans.RuntimeBean.PROXY_INTERFACES;
		} else {
			interfacesToUse = ArrayUtils.merge(interfacesToUse, scw.beans.RuntimeBean.PROXY_INTERFACES);
		}

		MethodInterceptors methodInterceptors = new MethodInterceptors();
		methodInterceptors.addLast(new RuntimeBean.RuntimeBeanMethodInterceptor(this));
		methodInterceptors.addLast(getFilters());
		return beanFactory.getAop().getProxy(targetClass, interfacesToUse, methodInterceptors);
	}

	protected Proxy createInstanceProxy(Object instance, Class<?> targetClass, Class<?>[] interfaces) {
		Class<?>[] interfacesToUse = interfaces;
		if (ArrayUtils.isEmpty(interfacesToUse)) {
			interfacesToUse = RuntimeBean.PROXY_INTERFACES;
		} else {
			interfacesToUse = ArrayUtils.merge(interfacesToUse, RuntimeBean.PROXY_INTERFACES);
		}

		MethodInterceptors methodInterceptors = new MethodInterceptors();
		methodInterceptors.addLast(new RuntimeBean.RuntimeBeanMethodInterceptor(this));
		methodInterceptors.addLast(getFilters());
		return beanFactory.getAop().getProxyInstance(targetClass, instance, interfaces, methodInterceptors);
	}

	protected Object createProxyInstance(Class<?> targetClass, Class<?>[] parameterTypes, Object[] args) {
		if (getTargetClass().isInterface() && CollectionUtils.isEmpty(getFilters())) {
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
}
