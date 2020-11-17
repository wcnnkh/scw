package scw.compatible;

import java.util.Iterator;

import scw.lang.RequiredJavaVersion;
import scw.util.ServiceLoader;

@RequiredJavaVersion(6)
public class DefaultSPI implements SPI {

	public <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
		java.util.ServiceLoader<S> serviceLoader = java.util.ServiceLoader.load(service, loader);
		return new DefaultServiceLoader<S>(serviceLoader);
	}

	public <S> ServiceLoader<S> load(Class<S> service) {
		java.util.ServiceLoader<S> serviceLoader = java.util.ServiceLoader.load(service);
		return new DefaultServiceLoader<S>(serviceLoader);
	}

	public <S> ServiceLoader<S> loadInstalled(Class<S> service) {
		java.util.ServiceLoader<S> serviceLoader = java.util.ServiceLoader.loadInstalled(service);
		return new DefaultServiceLoader<S>(serviceLoader);
	}

	public final class DefaultServiceLoader<S> implements ServiceLoader<S> {
		private java.util.ServiceLoader<S> serviceLoader;

		public DefaultServiceLoader(java.util.ServiceLoader<S> serviceLoader) {
			this.serviceLoader = serviceLoader;
		}

		public void reload() {
			serviceLoader.reload();
		}

		public Iterator<S> iterator() {
			return serviceLoader.iterator();
		}
	}
}
