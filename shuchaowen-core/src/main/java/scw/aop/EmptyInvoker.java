package scw.aop;

import java.lang.reflect.Method;

import scw.lang.NotSupportedException;

public class EmptyInvoker implements Invoker {
	private final Method method;

	public EmptyInvoker(Method method) {
		this.method = method;
	}

	public Object invoke(Object... args) throws Throwable {
		throw new NotSupportedException(method.toString());
	}
	
	@Override
	public String toString() {
		return method.toString();
	}
}
