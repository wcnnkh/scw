package scw.core.instance;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import scw.core.utils.ArrayUtils;
import scw.core.utils.CollectionUtils;
import scw.util.MultiEnumeration;
import scw.util.ServiceLoader;
import scw.value.ValueFactory;

public class ConfigurableServiceLoader<S> implements ServiceLoader<S> {
	private Class<? extends S> clazz;
	private NoArgsInstanceFactory instanceFactory;
	private ValueFactory<String> propertyFactory;
	private String[] configNames;
	private String[] defaultNames;
	private ServiceLoader<? extends S> parentServiceLoader;

	public ConfigurableServiceLoader(ServiceLoader<? extends S> serviceLoader, Class<? extends S> clazz,
			NoArgsInstanceFactory instanceFactory, ValueFactory<String> propertyFactory, String... defaultNames) {
		this.clazz = clazz;
		this.propertyFactory = propertyFactory;
		this.instanceFactory = instanceFactory;
		this.defaultNames = defaultNames;
		this.parentServiceLoader = serviceLoader;

		this.configNames = propertyFactory.getObject(clazz.getName(), String[].class);
	}

	public void reload() {
		if (parentServiceLoader != null) {
			parentServiceLoader.reload();
		}
		this.configNames = propertyFactory.getObject(clazz.getName(), String[].class);
	}

	public Iterator<S> iterator() {
		List<Enumeration<S>> enumerations = new LinkedList<Enumeration<S>>();
		if (!ArrayUtils.isEmpty(configNames)) {
			InstanceIterable<S> instanceIterable = new InstanceIterable<S>(instanceFactory, Arrays.asList(configNames));
			enumerations.add(CollectionUtils.toEnumeration(instanceIterable.iterator()));
		}

		if (parentServiceLoader != null) {
			enumerations.add(CollectionUtils.toEnumeration(parentServiceLoader.iterator()));
		}

		if (!ArrayUtils.isEmpty(defaultNames)) {
			InstanceIterable<S> instanceIterable = new InstanceIterable<S>(instanceFactory,
					Arrays.asList(defaultNames));
			enumerations.add(CollectionUtils.toEnumeration(instanceIterable.iterator()));
		}

		Enumeration<S> enumeration = new MultiEnumeration<S>(enumerations);
		return CollectionUtils.toIterator(enumeration);
	}

}
