package scw.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import scw.aop.ProxyUtils;
import scw.core.utils.ClassUtils;

public class ProxyObjectInputStream extends ObjectInputStream {
	private ClassLoader classLoader;

	public ProxyObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
		// Use the specified ClassLoader to resolve local classes.
		return ClassUtils.forName(classDesc.getName(), getClassLoader());
	}

	@Override
	protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
		// Use the specified ClassLoader to resolve local proxy classes.
		Class<?>[] resolvedInterfaces = new Class<?>[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			try {
				resolvedInterfaces[i] = ClassUtils.forName(interfaces[i], getClassLoader());
			} catch (ClassNotFoundException ex) {
				resolvedInterfaces[i] = resolveFallbackIfPossible(interfaces[i], ex);
			}
		}
		return ProxyUtils.getProxyFactory().getProxyClass(resolvedInterfaces[0], resolvedInterfaces);
	}

	protected Class<?> resolveFallbackIfPossible(String className, ClassNotFoundException ex)
			throws IOException, ClassNotFoundException {

		throw ex;
	}
}