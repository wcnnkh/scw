package scw.beans;

import java.lang.reflect.AnnotatedElement;

import scw.beans.annotation.Singleton;
import scw.core.annotation.AnnotationUtils;
import scw.core.instance.InstanceUtils;
import scw.value.ValueFactory;

public final class BeanUtils {
	private BeanUtils() {
	};

	public static boolean isSingleton(Class<?> type, AnnotatedElement annotatedElement) {
		Singleton singleton = annotatedElement.getAnnotation(Singleton.class);
		if (singleton != null) {
			return singleton.value();
		}

		for (Class<?> interfaceClass : type.getInterfaces()) {
			if (!isSingleton(interfaceClass, annotatedElement)) {
				return false;
			}
		}
		// 默认是单例
		return true;
	}

	public static String getScanAnnotationPackageName(ValueFactory<String> propertyFactory) {
		return propertyFactory.getValue("scw.scan.beans.package", String.class,
				InstanceUtils.getScanAnnotationPackageName(propertyFactory));
	}

	public static Class<?> getServiceInterface(Class<?> clazz) {
		Class<?> classToUse = clazz;
		while (classToUse != null && classToUse != Object.class) {
			for (Class<?> i : classToUse.getInterfaces()) {
				if (AnnotationUtils.isIgnore(classToUse) || i.getMethods().length == 0) {
					continue;
				}

				return i;
			}
			classToUse = classToUse.getSuperclass();
		}
		return null;
	}

	public static void init(Object init) throws Throwable {
		if (init == null) {
			return;
		}

		if (init instanceof Init) {
			((Init) init).init();
		}
	}

	public static void destroy(Object destroy) throws Throwable {
		if (destroy == null) {
			return;
		}

		if (destroy instanceof Destroy) {
			((Destroy) destroy).destroy();
		}
	}

	public static void aware(Object instance, BeanFactory beanFactory, BeanDefinition beanDefinition) {
		if (instance instanceof BeanFactoryAware) {
			((BeanFactoryAware) instance).setBeanFactory(beanFactory);
		}

		if (instance instanceof BeanDefinitionAware) {
			((BeanDefinitionAware) instance).setBeanDefinition(beanDefinition);
		}
	}

	public static RuntimeBean getRuntimeBean(Object instance) {
		if (instance == null) {
			return null;
		}

		if (instance instanceof RuntimeBean) {
			return ((RuntimeBean) instance);
		}

		return null;
	}
}
