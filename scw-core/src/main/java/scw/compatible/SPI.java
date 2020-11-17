package scw.compatible;

import scw.util.ServiceLoader;

public interface SPI {
	<S> ServiceLoader<S> load(Class<S> service, ClassLoader loader);

	<S> ServiceLoader<S> load(Class<S> service);

	<S> ServiceLoader<S> loadInstalled(Class<S> service);
}
