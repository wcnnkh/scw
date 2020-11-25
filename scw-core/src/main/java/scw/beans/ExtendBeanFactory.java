package scw.beans;

import java.util.LinkedHashMap;

import scw.beans.BeanDefinition;
import scw.beans.BeanFactory;
import scw.beans.BeanUtils;
import scw.beans.BeansException;
import scw.beans.Destroy;
import scw.core.instance.NoArgsInstanceFactory;
import scw.core.parameter.ParameterDescriptors;
import scw.core.parameter.ParameterFactory;
import scw.logger.Logger;
import scw.logger.LoggerUtils;

public class ExtendBeanFactory implements NoArgsInstanceFactory, Destroy {
	private static Logger logger = LoggerUtils.getLogger(ExtendBeanFactory.class);
	private final ParameterFactory parameterFactory;
	private final BeanFactory beanFactory;
	private volatile LinkedHashMap<String, Object> instanceMap;

	public ExtendBeanFactory(ParameterFactory parameterFactory, BeanFactory beanFactory) {
		this.parameterFactory = parameterFactory;
		this.beanFactory = beanFactory;
	}

	public <T> T getInstance(Class<? extends T> clazz) {
		return getInstance(clazz.getName());
	}

	private Object getInstanceByCache(String id) {
		return instanceMap == null ? null : instanceMap.get(id);
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(String name) {
		Object instance = getInstanceByCache(name);
		if (instance != null) {
			return (T) instance;
		}
		
		BeanDefinition beanDefinition = beanFactory.getDefinition(name);
		if (beanDefinition == null) {
			return null;
		}
		
		if(beanDefinition.isSingleton() && beanDefinition.isInstance()){
			return beanFactory.getInstance(name);
		}

		instance = getInstanceByCache(beanDefinition.getId());
		if (instance != null) {
			return (T) instance;
		}

		for (ParameterDescriptors parameterDescriptors : beanDefinition) {
			if (parameterFactory.isAccept(parameterDescriptors)) {
				if (beanDefinition.isSingleton()) {
					if (instanceMap == null) {
						synchronized (this) {
							if (instanceMap == null) {
								instanceMap = new LinkedHashMap<String, Object>(8);
							} else {
								instance = getInstanceByCache(beanDefinition.getId());
								if (instance != null) {
									return (T) instance;
								}
							}

							try {
								return (T) createInstance(beanDefinition, parameterDescriptors);
							} catch (Throwable e) {
								throw new BeansException(beanDefinition.getId(), e);
							}
						}
					} else {
						instance = getInstanceByCache(beanDefinition.getId());
						if (instance != null) {
							return (T) instance;
						}

						try {
							return (T) createInstance(beanDefinition, parameterDescriptors);
						} catch (Throwable e) {
							throw new BeansException(beanDefinition.getId(), e);
						}
					}
				} else {
					try {
						return (T) createInstance(beanDefinition, parameterDescriptors);
					} catch (Throwable e) {
						throw new BeansException(beanDefinition.getId(), e);
					}
				}
			}
		}
		return beanFactory.getInstance(name);
	}

	private Object createInstance(BeanDefinition beanDefinition, ParameterDescriptors parameterDescriptors)
			throws Throwable {
		Object instance = beanDefinition.create(parameterDescriptors.getTypes(),
				parameterFactory.getParameters(parameterDescriptors));
		if (beanDefinition.isSingleton()) {
			instanceMap.put(beanDefinition.getId(), instance);
		}

		beanDefinition.dependence(instance);
		beanDefinition.init(instance);
		return instance;
	}

	public boolean isInstance(String name) {
		if (instanceMap != null && instanceMap.containsKey(name)) {
			return true;
		}

		BeanDefinition beanDefinition = beanFactory.getDefinition(name);
		if (beanDefinition == null) {
			return false;
		}
		
		if(beanDefinition.isSingleton() && beanDefinition.isInstance()){
			return true;
		}

		if (instanceMap != null && instanceMap.containsKey(beanDefinition.getId())) {
			return true;
		}

		for (ParameterDescriptors parameterDescriptors : beanDefinition) {
			if (parameterFactory.isAccept(parameterDescriptors)) {
				return true;
			}
		}
		
		return beanDefinition.isInstance();
	}

	public boolean isInstance(Class<?> clazz) {
		return isInstance(clazz.getName());
	}

	public boolean isSingleton(String name) {
		return beanFactory.isInstance(name);
	}

	public boolean isSingleton(Class<?> clazz) {
		return isInstance(clazz.getName());
	}

	public ClassLoader getClassLoader() {
		return beanFactory.getClassLoader();
	}

	public void destroy() {
		synchronized (this) {
			if (instanceMap == null) {
				return;
			}

			synchronized (instanceMap) {
				BeanUtils.destroy(beanFactory, instanceMap, logger);
			}
			instanceMap = null;
		}
	}

}
