package scw.core.instance;

import scw.core.utils.ClassUtils;
import scw.util.ConcurrentReferenceHashMap;
import scw.value.property.PropertyFactory;

public class DefaultInstanceFactory extends AbstractInstanceFactory {
	private final ConcurrentReferenceHashMap<String, InstanceBuilder<?>> builderMap = new ConcurrentReferenceHashMap<String, InstanceBuilder<?>>();
	private final PropertyFactory propertyFactory;
	private ClassLoader classLoader;

	public DefaultInstanceFactory(PropertyFactory propertyFactory) {
		this.propertyFactory = propertyFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> InstanceBuilder<T> getInstanceBuilder(String name) {
		InstanceBuilder<T> instanceBuilder = (InstanceBuilder<T>) builderMap.get(name);
		if (instanceBuilder == null) {
			Class<T> clazz = (Class<T>) ClassUtils.forNameNullable(name);
			if (clazz == null) {
				return null;
			}

			if (ClassUtils.isAssignableValue(clazz, this)) {
				return new InternalInstanceBuilder<T>(this, propertyFactory, clazz, clazz.cast(this));
			}

			if (PropertyFactory.class == clazz) {
				return new InternalInstanceBuilder<T>(this, propertyFactory, clazz, clazz.cast(propertyFactory));
			}

			if (isIgnoreClass(clazz)) {
				return null;
			}

			instanceBuilder = new DefaultInstanceBuilder<T>(this, propertyFactory, clazz);
		}

		return instanceBuilder;
	}

	private static final class InternalInstanceBuilder<T> extends DefaultInstanceBuilder<T> {
		private final T instance;

		public InternalInstanceBuilder(NoArgsInstanceFactory instanceFactory, PropertyFactory propertyFactory,
				Class<T> targetClass, T instance) {
			super(instanceFactory, propertyFactory, targetClass);
			this.instance = instance;
		}

		public boolean isInstance() {
			return true;
		}

		public T create() throws Exception {
			return instance;
		}
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return classLoader == null ? ClassUtils.getDefaultClassLoader() : classLoader;
	}
}
