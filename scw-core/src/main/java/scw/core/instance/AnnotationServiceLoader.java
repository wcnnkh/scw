package scw.core.instance;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import scw.core.instance.annotation.Configuration;
import scw.core.utils.CollectionUtils;
import scw.util.ServiceLoader;

/**
 * 基于注解的spi机制
 * @see Configuration
 * @author shuchaowen
 *
 * @param <S>
 */
@SuppressWarnings("rawtypes")
public class AnnotationServiceLoader<S> implements ServiceLoader<S> {
	private NoArgsInstanceFactory instanceFactory;
	private Collection<? extends Class> excludeTypes;
	private Collection<String> packageNames;
	private Class<? extends S> serviceClass;
	private Iterable<S> iterable;

	public AnnotationServiceLoader(Class<? extends S> serviceClass, NoArgsInstanceFactory instanceFactory,
			Collection<? extends Class> excludeTypes, Collection<String> packageNames) {
		this.instanceFactory = instanceFactory;
		this.excludeTypes = excludeTypes;
		this.packageNames = packageNames;
		this.serviceClass = serviceClass;
		this.iterable = getIterable();
	}

	private Iterable<S> getIterable() {
		Collection<Class<S>> serviceClasses = InstanceUtils.getConfigurationClassList(serviceClass, excludeTypes,
				packageNames);
		if (CollectionUtils.isEmpty(serviceClasses)) {
			return Collections.emptyList();
		}

		String[] names = new String[serviceClasses.size()];
		Iterator<Class<S>> iterator = serviceClasses.iterator();
		for (int i = 0; iterator.hasNext(); i++) {
			names[i] = iterator.next().getName();
		}
		return new InstanceIterable<S>(instanceFactory, Arrays.asList(names));
	}

	public void reload() {
		iterable = getIterable();
	}

	public Iterator<S> iterator() {
		return iterable.iterator();
	}

}
