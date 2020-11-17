package scw.beans;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import scw.aop.Aop;
import scw.aop.DefaultAop;
import scw.aop.MethodInterceptor;
import scw.beans.annotation.AutoImpl;
import scw.beans.annotation.Proxy;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.IteratorBeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.beans.builder.ProxyBeanDefinition;
import scw.beans.ioc.Ioc;
import scw.beans.method.MethodBeanConfiguration;
import scw.beans.service.ServiceBeanConfiguration;
import scw.core.GlobalPropertyFactory;
import scw.core.annotation.AnnotationUtils;
import scw.core.instance.InstanceFactory;
import scw.core.instance.InstanceIterable;
import scw.core.instance.InstanceUtils;
import scw.core.instance.NoArgsInstanceFactory;
import scw.core.parameter.ConstructorParameterDescriptorsIterator;
import scw.core.parameter.ParameterDescriptors;
import scw.core.reflect.ReflectionUtils;
import scw.core.utils.ClassUtils;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.event.BasicEventDispatcher;
import scw.event.support.DefaultBasicEventDispatcher;
import scw.json.JSONUtils;
import scw.lang.AlreadyExistsException;
import scw.lang.NotSupportedException;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.util.Accept;
import scw.util.ClassScanner;
import scw.util.JavaVersion;
import scw.value.property.BasePropertyFactory;
import scw.value.property.PropertyFactory;

public class DefaultBeanFactory extends BeanLifecycle implements BeanFactory, Accept<Class<?>> {
	private static Logger logger = LoggerUtils.getLogger(DefaultBeanFactory.class);
	protected volatile LinkedHashMap<String, Object> singletonMap = new LinkedHashMap<String, Object>();
	private volatile Map<String, BeanDefinition> beanMap = new HashMap<String, BeanDefinition>();
	private volatile Map<String, String> nameMappingMap = new HashMap<String, String>();
	private final PropertyFactory propertyFactory;
	private List<String> filterNameList = new ArrayList<String>();
	private List<BeanBuilderLoader> beanBuilderLoaders = new ArrayList<BeanBuilderLoader>();
	private final DefaultBasicEventDispatcher<BeanLifeCycleEvent> beanLifeCycleEventDispatcher = new DefaultBasicEventDispatcher<BeanLifeCycleEvent>(
			true);
	private ClassLoader classLoader;

	public DefaultBeanFactory(PropertyFactory propertyFactory) {
		this.propertyFactory = propertyFactory;
		afterStructure();
	}

	protected void afterStructure() {
		propertyFactory.addFirstBasePropertyFactory(GlobalPropertyFactory.getInstance());
		addInternalSingleton(BeanFactory.class, this, InstanceFactory.class.getName(),
				NoArgsInstanceFactory.class.getName());
		addInternalSingleton(PropertyFactory.class, propertyFactory);
	}

	public ClassLoader getClassLoader() {
		return classLoader == null ? ClassUtils.getDefaultClassLoader() : classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public final BasicEventDispatcher<BeanLifeCycleEvent> getBeanLifeCycleEventDispatcher() {
		return beanLifeCycleEventDispatcher;
	}

	public BeanDefinition getDefinitionByCache(String name) {
		BeanDefinition beanDefinition = beanMap.get(name);
		if (beanDefinition == null) {
			String v = nameMappingMap.get(name);
			if (v != null) {
				beanDefinition = beanMap.get(v);
			}
		}
		return beanDefinition;
	}

	public BeanDefinition getDefinition(String name) {
		BeanDefinition beanDefinition = getDefinitionByCache(name);
		if (beanDefinition == null) {
			String aliasName = nameMappingMap.get(name);
			Class<?> clazz;
			if (aliasName == null) {
				clazz = ClassUtils.forNameNullable(name);
			} else {
				// 先使用别名尝试
				clazz = ClassUtils.forNameNullable(aliasName);
				if (clazz == null) {
					clazz = ClassUtils.forNameNullable(name);
				}
			}

			if (clazz == null) {
				return null;
			}

			if (!accept(clazz)) {
				return null;
			}

			synchronized (beanMap) {
				synchronized (nameMappingMap) {
					beanDefinition = getDefinitionByCache(name);
					if (beanDefinition == null) {
						beanDefinition = loading(new LoaderContext(clazz, this, getPropertyFactory(), null));
						if (beanDefinition == null) {
							return null;
						}

						if (!beanDefinition.getId().equals(name) && !beanDefinition.getNames().contains(name)) {
							addBeanNameMapping(Arrays.asList(name), beanDefinition.getId(), true);
						}

						if (beanDefinition instanceof DefaultBeanDefinition) {
							DefaultBeanDefinition definition = (DefaultBeanDefinition) beanDefinition;
							if (definition.isNew()) {
								beanDefinition = definition.clone();
								addBeanDefinition(beanDefinition, true);
							}
						} else {
							addBeanDefinition(beanDefinition, true);
						}
					}
				}
			}
		}
		return beanDefinition;
	}

	private static List<String> getProxyNames(Proxy proxy) {
		if (proxy == null) {
			return Collections.emptyList();
		}

		List<String> list = new ArrayList<String>();
		for (String name : proxy.names()) {
			list.add(name);
		}

		for (Class<? extends MethodInterceptor> c : proxy.value()) {
			list.add(c.getName());
		}

		return Arrays.asList(list.toArray(new String[0]));
	}

	private BeanDefinition loading(LoaderContext context) {
		if (!accept(context.getTargetClass())) {
			return null;
		}

		AutoImpl autoImpl = context.getTargetClass().getAnnotation(AutoImpl.class);
		Collection<Class<?>> autoImpls = autoImpl == null ? null : getAutoImplClass(autoImpl, context);
		if (!CollectionUtils.isEmpty(autoImpls)) {
			for (Class<?> impl : autoImpls) {
				BeanDefinition definition = getDefinitionByCache(impl.getName());
				if (definition == null) {
					definition = new DefaultBeanDefinition(new LoaderContext(impl, context));
				}
				if (definition != null && definition.isInstance()) {

					logger.info("Auto {} impl {}", context.getTargetClass(), impl);
					return definition;
				}
			}
		}

		Collection<Class<Object>> configurationClassList = InstanceUtils
				.getConfigurationClassList(context.getTargetClass(), propertyFactory);
		for (Class<?> impl : configurationClassList) {
			BeanDefinition definition = getDefinitionByCache(impl.getName());
			if (definition == null) {
				definition = new DefaultBeanDefinition(new LoaderContext(impl, context));
			}

			if (definition.isInstance()) {
				logger.info("Configuration {} impl {}", context.getTargetClass(), impl);
				return definition;
			}
		}

		BeanDefinition definition = new IteratorBeanBuilderLoaderChain(beanBuilderLoaders).loading(context);
		if (definition == null) {
			// 未注解service时接口默认实现
			if (context.getTargetClass().isInterface()) {
				String name = context.getTargetClass().getName() + "Impl";
				if (ClassUtils.isPresent(name) && context.getBeanFactory().isInstance(name)) {
					logger.info("{} reference {}", context.getTargetClass().getName(), name);
					definition = new DefaultBeanDefinition(context.getBeanFactory(), context.getPropertyFactory(),
							ClassUtils.forNameNullable(name));
				} else {
					int index = context.getTargetClass().getName().lastIndexOf(".");
					name = index == -1 ? (context.getTargetClass().getName() + "Impl")
							: (context.getTargetClass().getName().substring(0, index) + ".impl."
									+ context.getTargetClass().getSimpleName() + "Impl");
					if (ClassUtils.isPresent(name) && context.getBeanFactory().isInstance(name)) {
						logger.info("{} reference {}", context.getTargetClass().getName(), name);
						definition = context.getBeanFactory().getDefinition(name);
					}
				}
			}
		}

		if (definition == null) {
			if (context.getTargetClass().isInterface()
					|| Modifier.isAbstract(context.getTargetClass().getModifiers())) {
				Proxy proxy = context.getTargetClass().getAnnotation(Proxy.class);
				if (proxy != null) {
					definition = new ProxyBeanDefinition(context, getProxyNames(proxy));
				}
			}

			if (context.getTargetClass().isInterface()
					|| Modifier.isAbstract(context.getTargetClass().getModifiers())) {
				// 如果是接口或抽象类
				if (!CollectionUtils.isEmpty(autoImpls)) {
					for (Class<?> impl : autoImpls) {
						definition = getDefinitionByCache(impl.getName());
						if (definition == null) {
							definition = new DefaultBeanDefinition(new LoaderContext(impl, context));
						}

						logger.debug("Auto {} impl {}", context.getTargetClass(), impl);
						return definition;
					}
				}

				for (Class<?> impl : configurationClassList) {
					definition = getDefinitionByCache(impl.getName());
					if (definition == null) {
						definition = new DefaultBeanDefinition(new LoaderContext(impl, context));
					}
					logger.debug("Configuration {} impl {}", context.getTargetClass(), impl);
					return definition;
				}
			} else {
				definition = new DefaultBeanDefinition(context);
			}
		}
		return definition;
	}

	public boolean accept(Class<?> clazz) {
		return !ClassUtils.isPrimitiveOrWrapper(clazz) && !AnnotationUtils.isIgnore(clazz)
				&& JavaVersion.isSupported(clazz) && ReflectionUtils.isPresent(clazz);
	}

	public BeanDefinition getDefinition(Class<?> clazz) {
		if (!accept(clazz)) {
			return null;
		}
		return getDefinition(clazz.getName());
	}

	public boolean isSingleton(Class<?> clazz) {
		return isSingleton(clazz.getName());
	}

	public boolean isSingleton(String name) {
		if (singletonMap.containsKey(name)) {
			return true;
		}

		BeanDefinition definition = getDefinition(name);
		if (definition == null) {
			return false;
		}

		return singletonMap.containsKey(definition.getId()) || definition.isSingleton();
	}

	public <T> T getInstance(Class<? extends T> clazz) {
		return getInstance(clazz.getName());
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(String name) {
		Object object = singletonMap.get(name);
		if (object != null) {
			return (T) object;
		}

		BeanDefinition definition = getDefinition(name);
		if (definition == null) {
			return null;
		}

		if (definition.isSingleton()) {
			object = singletonMap.get(definition.getId());
			if (object == null) {
				synchronized (singletonMap) {
					object = singletonMap.get(definition.getId());
					if (object == null) {
						object = createInternal(definition);
					}
				}
			}
		} else {
			object = createInternal(definition);
		}
		return (T) object;
	}

	protected void init(long createTime, BeanDefinition definition, Object object) throws Throwable {
		if (definition.isSingleton()) {
			singletonMap.put(definition.getId(), object);
		}
		definition.dependence(object);
		definition.init(object);

		if (logger.isDebugEnabled()) {
			logger.debug("create instance [{}] by definition [{}] use time {}ms", object, definition.getId(),
					System.currentTimeMillis() - createTime);
		}
	}

	public PropertyFactory getPropertyFactory() {
		return propertyFactory;
	}

	private Object createInternal(BeanDefinition definition) {
		long t = System.currentTimeMillis();
		Object obj;
		try {
			obj = definition.create();
			init(t, definition, obj);
		} catch (Throwable e) {
			throw new BeansException(definition.getId(), e);
		}
		return obj;
	}

	public boolean isInstance(String name) {
		if (singletonMap.containsKey(name)) {
			return true;
		}

		BeanDefinition definition = getDefinition(name);
		if (definition == null) {
			return false;
		}

		return singletonMap.containsKey(definition.getId()) || definition.isInstance();
	}

	public boolean isInstance(Class<?> clazz) {
		return accept(clazz) && isInstance(clazz.getName());
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(String name, Object... params) {
		Object obj = singletonMap.get(name);
		if (obj != null) {
			return (T) obj;
		}

		BeanDefinition definition = getDefinition(name);
		if (definition == null) {
			return null;
		}

		if (definition.isSingleton()) {
			obj = singletonMap.get(definition.getId());
			if (obj == null) {
				synchronized (singletonMap) {
					obj = singletonMap.get(definition.getId());
					if (obj == null) {
						obj = createInternal(definition, params);
					}
				}
			}
		} else {
			obj = createInternal(definition, params);
		}
		return (T) obj;
	}

	private Object createInternal(BeanDefinition definition, Object... params) {
		long t = System.currentTimeMillis();
		Object obj;
		try {
			obj = definition.create(params);
			init(t, definition, obj);
		} catch (Throwable e) {
			throw new BeansException(definition.getId(), e);
		}
		return obj;
	}

	public <T> T getInstance(Class<? extends T> type, Object... params) {
		return getInstance(type.getName(), params);
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(String name, Class<?>[] parameterTypes, Object... params) {
		Object obj = singletonMap.get(name);
		if (obj != null) {
			return (T) obj;
		}

		BeanDefinition definition = getDefinition(name);
		if (definition == null) {
			return null;
		}

		if (definition.isSingleton()) {
			obj = singletonMap.get(definition.getId());
			if (obj == null) {
				synchronized (singletonMap) {
					obj = singletonMap.get(definition.getId());
					if (obj == null) {
						obj = createInternal(definition, parameterTypes, params);
					}
				}
			}
		} else {
			obj = createInternal(definition, parameterTypes, params);
		}
		return (T) obj;
	}

	private Object createInternal(BeanDefinition definition, Class<?>[] parameterTypes, Object... params) {
		long t = System.currentTimeMillis();
		Object obj;
		try {
			obj = definition.create(parameterTypes, params);
			init(t, definition, obj);
		} catch (Throwable e) {
			throw new BeansException(definition.getId(), e);
		}
		return obj;
	}

	public <T> T getInstance(Class<? extends T> type, Class<?>[] parameterTypes, Object... params) {
		return getInstance(type.getName(), parameterTypes, params);
	}

	public void addBeanDefinition(BeanDefinition beanDefinition, boolean throwExistError) {
		BeanDefinition definition = getDefinitionByCache(beanDefinition.getId());
		if (definition != null) {
			logger.warn("Already exist definition:\r\n{}\r\n---\r\n{}", JSONUtils.toJSONString(beanDefinition),
					JSONUtils.toJSONString(definition));
			if (throwExistError) {
				throw new AlreadyExistsException("存在相同ID的映射:" + JSONUtils.toJSONString(beanDefinition));
			}

			return;
		}

		if (addBeanNameMapping(beanDefinition.getNames(), beanDefinition.getId(), throwExistError)) {
			beanMap.put(beanDefinition.getId(), beanDefinition);
		}
	}

	public boolean addBeanNameMapping(Collection<String> names, String id, boolean throwExistError) {
		if (CollectionUtils.isEmpty(names)) {
			return true;
		}

		for (String name : names) {
			BeanDefinition definition = getDefinitionByCache(name);
			if (definition != null) {
				logger.warn("Already exist name:{}, definition:{}", name, JSONUtils.toJSONString(definition));
				if (throwExistError) {
					throw new AlreadyExistsException("存在相同名称的映射:" + JSONUtils.toJSONString(definition));
				}
				return false;
			}
		}

		for (String name : names) {
			nameMappingMap.put(name, id);
		}
		return true;
	}

	public void addBeanConfiguration(BeanConfiguration beanConfiguration) throws Exception {
		beanConfiguration.init(this, propertyFactory);
		Collection<BeanDefinition> beanDefinitions = beanConfiguration.getBeans();
		if (!CollectionUtils.isEmpty(beanDefinitions)) {
			for (BeanDefinition beanDefinition : beanDefinitions) {
				synchronized (beanMap) {
					synchronized (nameMappingMap) {
						addBeanDefinition(beanDefinition, false);
					}
				}
			}
		}

		Map<String, String> nameMapping = beanConfiguration.getNameMappingMap();
		if (!CollectionUtils.isEmpty(nameMapping)) {
			for (Entry<String, String> entry : nameMapping.entrySet()) {
				addBeanNameMapping(Arrays.asList(entry.getKey()), entry.getValue(), false);
			}
		}
	}

	public <T> void addInternalSingleton(Class<? extends T> type, T instance, String... names) {
		singletonMap.put(type.getName(), instance);
		addBeanDefinition(new InternalBeanDefinition(instance, type, Arrays.asList(names)), false);
	}

	private Aop aop = new DefaultAop(new InstanceIterable<MethodInterceptor>(this, filterNameList));

	public Aop getAop() {
		return aop;
	}

	@Override
	protected void initInternal() throws Throwable {
		for (Class<MethodInterceptor> filter : InstanceUtils.getConfigurationClassList(MethodInterceptor.class,
				propertyFactory)) {
			filterNameList.add(filter.getName());
		}

		addBeanConfiguration(new MethodBeanConfiguration());
		addBeanConfiguration(new ServiceBeanConfiguration());
		beanBuilderLoaders.addAll(InstanceUtils.getConfigurationList(BeanBuilderLoader.class, this, propertyFactory));

		propertyFactory.addLastBasePropertyFactory(
				InstanceUtils.getConfigurationList(BasePropertyFactory.class, this, propertyFactory));
		for (BeanConfiguration configuration : InstanceUtils.getConfigurationList(BeanConfiguration.class, this,
				propertyFactory)) {
			addBeanConfiguration(configuration);
		}
		super.initInternal();
	}

	@Override
	public void afterInit() throws Throwable {
		super.afterInit();
		for (Class<?> clazz : ClassScanner.getInstance()
				.getClasses(BeanUtils.getScanAnnotationPackageName(propertyFactory))) {
			if (!accept(clazz)) {
				continue;
			}

			for (Ioc ioc : Ioc.forClass(clazz)) {
				ioc.getDependence().process(null, null, this, propertyFactory);
				ioc.getInit().process(null, null, this, propertyFactory);
			}
		}
	}

	@Override
	protected void destroyInternal() throws Throwable {
		synchronized (singletonMap) {
			List<String> beanKeyList = new ArrayList<String>();
			for (Entry<String, Object> entry : singletonMap.entrySet()) {
				beanKeyList.add(entry.getKey());
			}

			ListIterator<String> keyIterator = beanKeyList.listIterator(beanKeyList.size());
			while (keyIterator.hasPrevious()) {
				BeanDefinition beanDefinition = getDefinitionByCache(keyIterator.previous());
				if (beanDefinition == null) {
					continue;
				}

				Object obj = singletonMap.get(beanDefinition.getId());
				try {
					beanDefinition.destroy(obj);
				} catch (Throwable e) {
					logger.error(e, "destroy error: {}", beanDefinition.getId());
				}
			}
		}
		super.destroyInternal();
	}

	@Override
	public void afterDestroy() throws Throwable {
		super.afterDestroy();
		for (Class<?> clazz : ClassScanner.getInstance()
				.getClasses(BeanUtils.getScanAnnotationPackageName(propertyFactory))) {
			for (Ioc ioc : Ioc.forClass(clazz)) {
				ioc.getDestroy().process(null, null, this, propertyFactory);
			}
		}

		singletonMap.clear();
		beanMap.clear();
		nameMappingMap.clear();
		propertyFactory.clear();
		filterNameList.clear();
		beanBuilderLoaders.clear();
		afterStructure();
	}

	private final class InternalBeanDefinition implements BeanDefinition {
		private final Object instance;
		private final Class<?> targetClass;
		private final Collection<String> names;

		public InternalBeanDefinition(Object instance, Class<?> targetClass, Collection<String> names) {
			this.instance = instance;
			this.targetClass = targetClass;
			this.names = names;
		}

		public String getId() {
			return getTargetClass().getName();
		}

		public Collection<String> getNames() {
			return names;
		}

		public Class<?> getTargetClass() {
			return targetClass;
		}

		public boolean isSingleton() {
			return true;
		}

		public boolean isInstance() {
			return true;
		}

		public Object create() throws Exception {
			return instance;
		}

		public Object create(Object... params) throws Exception {
			throw new NotSupportedException(getId());
		}

		public Object create(Class<?>[] parameterTypes, Object... params) throws Exception {
			throw new NotSupportedException(getId());
		}

		public void dependence(Object instance) throws Exception {
			if (instance != null) {
				for (Ioc ioc : Ioc.forClass(instance.getClass())) {
					ioc.getDependence().process(this, instance, DefaultBeanFactory.this, propertyFactory);
				}
			}
		}

		public void init(Object bean) throws Exception {
		}

		public void destroy(Object bean) throws Exception {
		}

		public AnnotatedElement getAnnotatedElement() {
			return getTargetClass();
		}

		public Iterator<ParameterDescriptors> iterator() {
			return new ConstructorParameterDescriptorsIterator(getTargetClass());
		}
	}

	private Collection<Class<?>> getAutoImplClass(AutoImpl autoConfig, LoaderContext context) {
		List<Class<?>> list = new ArrayList<Class<?>>();
		for (String name : autoConfig.className()) {
			if (StringUtils.isEmpty(name)) {
				continue;
			}

			name = context.getPropertyFactory().format(name);
			Class<?> clz = ClassUtils.forNameNullable(name);
			if (clz == null) {
				continue;
			}

			if (!accept(clz)) {
				logger.debug("{} not present", clz);
				continue;
			}

			if (context.getTargetClass().isAssignableFrom(clz)) {
				list.add(clz);
			} else {
				logger.warn("{} not is assignable from name {}", context.getTargetClass(), clz);
			}
		}

		for (Class<?> clz : autoConfig.value()) {
			if (context.getTargetClass().isAssignableFrom(clz)) {
				list.add(clz);
			} else {
				logger.warn("{} not is assignable from {}", context.getTargetClass(), clz);
			}
		}
		return list;
	}
}
