package scw.aop.support;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import scw.aop.AbsttractProxyAdapter;
import scw.aop.DefaultFilterChain;
import scw.aop.Filter;
import scw.aop.FilterChain;
import scw.aop.Invoker;
import scw.aop.Proxy;
import scw.aop.ProxyUtils;
import scw.core.cglib.proxy.Enhancer;
import scw.core.cglib.proxy.MethodInterceptor;
import scw.core.cglib.proxy.MethodProxy;
import scw.lang.NestedExceptionUtils;

public class BuiltInCglibProxyAdapter extends AbsttractProxyAdapter {
	public boolean isSupport(Class<?> clazz) {
		return !Modifier.isFinal(clazz.getModifiers());
	}

	public boolean isProxy(Class<?> clazz) {
		return Enhancer.isEnhanced(clazz);
	}

	public Class<?> getClass(Class<?> clazz, Class<?>[] interfaces) {
		return BuiltInCglibProxy.createEnhancer(clazz, getInterfaces(clazz, interfaces)).createClass();
	}

	public Proxy proxy(Class<?> clazz, Class<?>[] interfaces, Collection<? extends Filter> filters,
			FilterChain filterChain) {
		return new BuiltInCglibProxy(clazz, getInterfaces(clazz, interfaces),
				new FiltersConvertCglibMethodInterceptor(clazz, filters, filterChain));
	}

	private static final class FiltersConvertCglibMethodInterceptor implements MethodInterceptor {
		private final Collection<? extends Filter> filters;
		private final Class<?> targetClass;
		private final FilterChain filterChain;

		public FiltersConvertCglibMethodInterceptor(Class<?> targetClass, Collection<? extends Filter> filters,
				FilterChain filterChain) {
			this.filters = filters;
			this.targetClass = targetClass;
			this.filterChain = filterChain;
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			Object ignoreReturn = ProxyUtils.ignoreMethod(proxy, method, args);
			if (ignoreReturn != null) {
				return ignoreReturn;
			}

			DefaultFilterChain filterChain = new DefaultFilterChain(filters, this.filterChain);
			return filterChain.doFilter(new CglibInvoker(proxy, obj), obj, targetClass, method, args);
		}
	}

	private static final class CglibInvoker implements Invoker {
		private final MethodProxy proxy;
		private final Object obj;

		public CglibInvoker(MethodProxy proxy, Object obj) {
			this.proxy = proxy;
			this.obj = obj;
		}

		public Object invoke(Object... args) throws Throwable {
			try {
				return proxy.invokeSuper(obj, args);
			} catch (Throwable e) {
				throw NestedExceptionUtils.excludeInvalidNestedExcpetion(e);
			}
		}
	}
}