package scw.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import scw.aop.MethodInterceptor;
import scw.aop.MethodInterceptorChain;
import scw.aop.MethodInvoker;
import scw.core.utils.ArrayUtils;

public interface RuntimeBean {
	static final Class<?>[] PROXY_INTERFACES = new Class<?>[] { RuntimeBean.class };

	BeanDefinition getBeanDefinition();

	boolean _dependence();

	boolean _init();

	boolean _destroy();

	static class RuntimeBeanMethodInterceptor implements MethodInterceptor {
		private boolean _dependence;
		private boolean _init;
		private boolean _destroy;
		private final BeanDefinition beanDefinition;

		public RuntimeBeanMethodInterceptor(BeanDefinition beanDefinition) {
			this.beanDefinition = beanDefinition;
		}

		public Object intercept(MethodInvoker invoker, Object[] args, MethodInterceptorChain chain) throws Throwable {
			if (ArrayUtils.isEmpty(args)) {
				Method method = invoker.getMethod();
				if ((Modifier.isAbstract(method.getModifiers()) || Modifier.isInterface(method.getModifiers()))) {
					if (method.getName().equals("getBeanDefinition")) {
						return beanDefinition;
					}

					if (method.getName().equals("_dependence")) {
						if (_dependence) {
							return false;
						}

						_dependence = true;
						return true;
					}

					if (method.getName().equals("_init")) {
						if (_init) {
							return false;
						}

						_init = true;
						return true;
					}

					if (method.getName().equals("_destroy")) {
						if (_destroy) {
							return false;
						}

						_destroy = true;
						return true;
					}
				}
			}
			return chain.intercept(invoker, args);
		}
	}
}
