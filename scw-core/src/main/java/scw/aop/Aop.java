package scw.aop;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

import scw.core.instance.InstanceIterable;
import scw.core.instance.NoArgsInstanceFactory;
import scw.core.utils.ArrayUtils;
import scw.lang.NotSupportedException;
import scw.util.MultiIterable;

public abstract class Aop implements ProxyFactory {

	protected abstract Iterable<MethodInterceptor> getFilters();

	protected ProxyFactory getProxyFactory() {
		return ProxyUtils.getProxyFactory();
	}

	public final boolean isSupport(Class<?> clazz) {
		return getProxyFactory().isSupport(clazz);
	}

	@SuppressWarnings("unchecked")
	public final Proxy getProxy(Class<?> clazz, Class<?>[] interfaces, Iterable<? extends MethodInterceptor> filters) {
		return getProxyFactory().getProxy(clazz, interfaces,
				new MultiIterable<MethodInterceptor>(getFilters(), filters));
	}

	public final Proxy getProxy(Class<?> clazz, Class<?>[] interfaces, MethodInterceptor... filters) {
		return getProxy(clazz, interfaces, Arrays.asList(filters));
	}

	public final Proxy getProxy(Class<?> clazz, Class<?>[] interfaces, Collection<MethodInterceptor> filters) {
		return getProxy(clazz, interfaces, (Iterable<MethodInterceptor>) filters);
	}

	public final Proxy getProxy(Class<?> clazz, Class<?>[] interfaces, NoArgsInstanceFactory instanceFactory,
			Collection<String> filterNames) {
		return getProxy(clazz, interfaces, new InstanceIterable<MethodInterceptor>(instanceFactory, filterNames));
	}

	public final Class<?> getProxyClass(Class<?> clazz, Class<?>[] interfaces) {
		return getProxyFactory().getProxyClass(clazz, interfaces);
	}

	public final boolean isProxy(Class<?> clazz) {
		return getProxyFactory().isProxy(clazz);
	}

	public final Class<?> getUserClass(Class<?> clazz) {
		return getProxyFactory().getUserClass(clazz);
	}

	public final Class<?> getUserClass(String className, boolean initialize, ClassLoader classLoader)
			throws ClassNotFoundException {
		return getProxyFactory().getUserClass(className, initialize, classLoader);
	}

	public final boolean isProxy(String className, ClassLoader classLoader) {
		return getProxyFactory().isProxy(className, classLoader);
	}

	public final <T> Proxy getProxyInstance(Class<? extends T> clazz, T instance, Class<?>[] interfaces,
			Iterable<? extends MethodInterceptor> filters) {
		MethodInterceptors interceptors = new MethodInterceptors();
		interceptors.addLast(new InstanceMethodInterceptor(instance));
		interceptors.addLast(filters);
		return getProxy(clazz, ArrayUtils.merge(interfaces, ProxyInstanceTarget.CLASSES), interceptors);
	}

	public final <T> Proxy getProxyInstance(Class<? extends T> clazz, T instance, Class<?>[] interfaces,
			MethodInterceptor... filters) {
		return getProxyInstance(clazz, instance, interfaces, Arrays.asList(filters));
	}

	@SuppressWarnings("unchecked")
	private final MethodInvoker wrapper(MethodInvoker invoker, Iterable<? extends MethodInterceptor> filters,
			boolean singletion) {
		if (isProxyMethod(invoker.getInstance(), invoker.getMethod())) {
			return new ProxyMethodInvoker(invoker, filters, singletion);
		}
		return new ProxyMethodInvoker(invoker, new MultiIterable<MethodInterceptor>(getFilters(), filters), singletion);
	}

	/**
	 * 代理一个方法 ，如果被{@see #isProxyMethod(Object, Method)}方法判定为true那么就不会被代理
	 * 
	 * @param targetClass
	 * @param instance
	 * @param method
	 * @param filters
	 * @return
	 */
	public final <T> MethodInvoker getProxyMethod(Class<? extends T> targetClass, T instance, Method method,
			Iterable<? extends MethodInterceptor> filters) {
		MethodInvoker proxyInvoker = new DefaultMethodInvoker(instance, targetClass, method);
		return wrapper(proxyInvoker, filters, true);
	}

	public final <T> MethodInvoker getProxyMethod(Class<? extends T> targetClass, T instance, Method method,
			MethodInterceptor... filters) {
		return getProxyMethod(targetClass, instance, method, Arrays.asList(filters));
	}

	/**
	 * 代理一个方法 ，如果被{@see #isProxyMethod(Object, Method)}方法判定为true那么就不会被代理
	 * 
	 * @param instanceFactory
	 * @param instanceName
	 * @param targetClass
	 * @param method
	 * @param filters
	 * @return
	 */
	public final MethodInvoker getProxyMethod(NoArgsInstanceFactory instanceFactory, String instanceName,
			Class<?> targetClass, Method method, Iterable<? extends MethodInterceptor> filters) {
		if (Modifier.isStatic(method.getModifiers())) {
			return getProxyMethod(targetClass, null, method, filters);
		}

		if (!instanceFactory.isInstance(instanceName)) {
			throw new NotSupportedException("instanceName:" + instanceName + ", method:" + method.toString());
		}

		if (instanceFactory.isSingleton(instanceName)) {
			return getProxyMethod(targetClass, instanceFactory.getInstance(instanceName), method, filters);
		}

		MethodInvoker methodInvoker = new DefaultMethodInvoker(instanceFactory, instanceName, targetClass, method,
				true);
		return wrapper(methodInvoker, filters, false);
	}

	public final MethodInvoker getProxyMethod(NoArgsInstanceFactory instanceFactory, String instanceName,
			Class<?> targetClass, Method method, MethodInterceptor... filters) {
		return getProxyMethod(instanceFactory, instanceName, targetClass, method, Arrays.asList(filters));
	}

	/**
	 * 代理一个方法 ，如果被{@see #isProxyMethod(Object, Method)}方法判定为true那么就不会被代理
	 * 
	 * @param noArgsInstanceFactory
	 * @param targetClass
	 * @param method
	 * @param filters
	 * @return
	 */
	public final MethodInvoker getProxyMethod(NoArgsInstanceFactory noArgsInstanceFactory, Class<?> targetClass,
			Method method, Iterable<? extends MethodInterceptor> filters) {
		return getProxyMethod(noArgsInstanceFactory, targetClass.getName(), targetClass, method, filters);
	}

	public final MethodInvoker getProxyMethod(NoArgsInstanceFactory noArgsInstanceFactory, Class<?> targetClass,
			Method method, MethodInterceptor... filters) {
		return getProxyMethod(noArgsInstanceFactory, targetClass, method, Arrays.asList(filters));
	}

	public boolean isProxyMethod(Object instance, Method method) {
		boolean isProxy = !(Modifier.isPrivate(method.getModifiers()) || Modifier.isStatic(method.getModifiers())
				|| Modifier.isFinal(method.getModifiers()) || Modifier.isNative(method.getModifiers()));
		if (isProxy) {
			isProxy = instance != null && isProxy(instance.getClass());
		}
		return isProxy;
	}

	private static final class InstanceMethodInterceptor implements MethodInterceptor, Serializable {
		private static final long serialVersionUID = 1L;
		private final Object instance;

		public InstanceMethodInterceptor(Object instance) {
			this.instance = instance;
		}

		public Object intercept(MethodInvoker invoker, Object[] args, MethodInterceptorChain filterChain)
				throws Throwable {
			if(ArrayUtils.isEmpty(args) && invoker.getMethod().getName().equals(ProxyInstanceTarget.class)){
				return instance;
			}
			MethodInvoker proxyInvoker = new DefaultMethodInvoker(instance, invoker.getSourceClass(),
					invoker.getMethod(), true);
			return filterChain.intercept(proxyInvoker, args);
		}
	}

	private static class ProxyMethodInvoker extends MethodInvokerWrapper implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Iterable<? extends MethodInterceptor> filters;
		private final boolean singletion;

		public ProxyMethodInvoker(MethodInvoker source, Iterable<? extends MethodInterceptor> filters,
				boolean singletion) {
			super(source);
			this.filters = filters;
			this.singletion = singletion;
		}

		@Override
		public Object invoke(Object... args) throws Throwable {
			MethodInvoker invoker = singletion ? getSource()
					: new DefaultMethodInvoker(getInstance(), getSourceClass(), getMethod());
			if (filters == null) {
				return invoker.invoke(args);
			}
			return new MethodInterceptorChain(filters.iterator()).intercept(invoker, args);
		}
	}
}
