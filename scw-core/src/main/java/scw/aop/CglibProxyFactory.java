package scw.aop;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodProxy;
import scw.core.instance.annotation.Configuration;
import scw.core.utils.ClassUtils;
import scw.lang.NestedExceptionUtils;

@Configuration(order = Integer.MIN_VALUE)
public class CglibProxyFactory implements ProxyFactory {
	public boolean isSupport(Class<?> clazz) {
		return !Modifier.isFinal(clazz.getModifiers());
	}

	public boolean isProxy(Class<?> clazz) {
		return Enhancer.isEnhanced(clazz);
	}

	private Class<?>[] getInterfaces(Class<?> clazz, Class<?>[] interfaces) {
		if (interfaces == null || interfaces.length == 0) {
			return new Class<?>[0];
		}

		Class<?>[] interfacesToUse = new Class<?>[interfaces.length];
		int index = 0;
		for (Class<?> i : interfaces) {
			if (i.isAssignableFrom(clazz) || Factory.class.isAssignableFrom(i)) {
				continue;
			}

			interfacesToUse[index++] = i;
		}
		return Arrays.copyOf(interfacesToUse, index);
	}

	public Class<?> getProxyClass(Class<?> clazz, Class<?>[] interfaces) {
		Enhancer enhancer = createEnhancer(clazz, getInterfaces(clazz, interfaces));
		enhancer.setCallbackType(CglibMethodInterceptor.class);
		return enhancer.createClass();
	}

	public Proxy getProxy(Class<?> clazz, Class<?>[] interfaces, Iterable<? extends MethodInterceptor> filters) {
		return new CglibProxy(clazz, getInterfaces(clazz, interfaces), new CglibMethodInterceptor(clazz, filters));
	}

	private static Enhancer createEnhancer(Class<?> clazz, Class<?>[] interfaces) {
		Enhancer enhancer = new Enhancer();
		if (Serializable.class.isAssignableFrom(clazz)) {
			enhancer.setSerialVersionUID(1L);
		}
		if (interfaces != null) {
			enhancer.setInterfaces(interfaces);
		}
		enhancer.setSuperclass(clazz);
		enhancer.setUseCache(true);
		return enhancer;
	}

	private static final class CglibMethodInterceptor implements net.sf.cglib.proxy.MethodInterceptor, Serializable {
		private static final long serialVersionUID = 1L;
		private final Class<?> targetClass;
		private final Iterable<? extends MethodInterceptor> filters;

		public CglibMethodInterceptor(Class<?> targetClass, Iterable<? extends MethodInterceptor> filters) {
			this.targetClass = targetClass;
			this.filters = filters;
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			CglibProxyInvoker invoker = new CglibProxyInvoker(obj, targetClass, method, methodProxy);
			if (filters == null) {
				return invoker.invoke(args);
			}

			return new MethodInterceptorChain(filters.iterator()).intercept(invoker, args);
		}
	}

	private static final class CglibProxyInvoker extends DefaultMethodInvoker {
		private static final long serialVersionUID = 1L;
		private final MethodProxy methodProxy;

		public CglibProxyInvoker(Object proxy, Class<?> targetClass, Method method, MethodProxy methodProxy) {
			super(proxy, targetClass, method);
			this.methodProxy = methodProxy;
		}

		public Object invoke(Object... args) throws Throwable {
			try {
				return methodProxy.invokeSuper(getInstance(), args);
			} catch (Throwable e) {
				throw NestedExceptionUtils.excludeInvalidNestedExcpetion(e);
			}
		}
	}

	public Class<?> getUserClass(Class<?> clazz) {
		Class<?> clz = clazz.getSuperclass();
		if (clz == null || clz == Object.class) {
			return clazz;
		}
		return clz;
	}

	public static final class CglibProxy extends AbstractProxy {
		private Enhancer enhancer;

		public CglibProxy(Class<?> clazz, Class<?>[] interfaces, net.sf.cglib.proxy.MethodInterceptor methodInterceptor) {
			super(clazz);
			this.enhancer = createEnhancer(clazz, interfaces);
			this.enhancer.setCallback(methodInterceptor);
		}

		public Object create() {
			return enhancer.create();
		}

		public Object createInternal(Class<?>[] parameterTypes, Object[] arguments) {
			return enhancer.create(parameterTypes, arguments);
		}
	}

	/** The CGLIB class separator: "$$" */
	public static final String CGLIB_CLASS_SEPARATOR = "$$";

	public boolean isProxy(String className, ClassLoader classLoader) {
		return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
	}

	public Class<?> getUserClass(String className, boolean initialize, ClassLoader classLoader)
			throws ClassNotFoundException {
		return ClassUtils.forName(className.substring(0, className.indexOf(CGLIB_CLASS_SEPARATOR)), initialize,
				classLoader);
	}
}
