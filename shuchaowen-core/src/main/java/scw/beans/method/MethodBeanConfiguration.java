package scw.beans.method;

import java.lang.reflect.Method;
import java.util.Arrays;

import scw.beans.AbstractBeanConfiguration;
import scw.beans.BeanDefinition;
import scw.beans.BeanFactory;
import scw.beans.BeanUtils;
import scw.beans.annotation.Bean;
import scw.core.Constants;
import scw.io.ResourceUtils;
import scw.value.property.PropertyFactory;

public class MethodBeanConfiguration extends AbstractBeanConfiguration {
	
	public void init(BeanFactory beanFactory, PropertyFactory propertyFactory)
			throws Exception {
		for (Class<?> clz : ResourceUtils.getPackageScan().getClasses(Arrays.asList(
				BeanUtils.getScanAnnotationPackageName(),
				Constants.SYSTEM_PACKAGE_NAME))) {
			for (Method method : clz.getDeclaredMethods()) {
				Bean bean = method.getAnnotation(Bean.class);
				if (bean == null) {
					continue;
				}

				BeanDefinition beanDefinition = new MethodBeanDefinition(
						beanFactory, propertyFactory, method.getReturnType(),
						clz, method);
				beanDefinitions.add(beanDefinition);
			}
		}
	}
}
