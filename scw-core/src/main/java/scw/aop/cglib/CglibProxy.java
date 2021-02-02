package scw.aop.cglib;

import java.util.Arrays;

import net.sf.cglib.proxy.Enhancer;
import scw.aop.MethodInterceptor;
import scw.aop.support.AbstractProxy;

public class CglibProxy extends AbstractProxy {
	private Enhancer enhancer;

	public CglibProxy(Class<?> clazz, Class<?>[] interfaces, MethodInterceptor methodInterceptor) {
		super(clazz, interfaces, methodInterceptor);
		this.enhancer = CglibUtils.createEnhancer(clazz, interfaces);
		this.enhancer.setCallback(new CglibMethodInterceptor(clazz, methodInterceptor));
	}

	public Object create() {
		return enhancer.create();
	}

	public Object createInternal(Class<?>[] parameterTypes, Object[] arguments) {
		try {
			return enhancer.create(parameterTypes, arguments);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(Arrays.toString(parameterTypes) + " " + Arrays.toString(arguments), e);
		}
	}
}