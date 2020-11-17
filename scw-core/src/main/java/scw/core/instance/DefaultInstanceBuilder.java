package scw.core.instance;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import scw.core.parameter.ConstructorParameterDescriptorsIterator;
import scw.core.parameter.ParameterDescriptors;
import scw.core.reflect.ReflectionUtils;
import scw.lang.NotFoundException;
import scw.lang.NotSupportedException;
import scw.util.ServiceLoader;
import scw.value.property.PropertyFactory;

public class DefaultInstanceBuilder<T> extends DefaultParameterFactory implements InstanceBuilder<T> {
	private Class<? extends T> targetClass;
	private NoArgsInstanceFactory instanceFactory;
	private PropertyFactory propertyFactory;
	private ServiceLoader<T> serviceLoader;

	public DefaultInstanceBuilder(NoArgsInstanceFactory instanceFactory, PropertyFactory propertyFactory,
			Class<? extends T> targetClass) {
		this.targetClass = targetClass;
		this.instanceFactory = instanceFactory;
		this.propertyFactory = propertyFactory;
	}

	@Override
	public NoArgsInstanceFactory getInstanceFactory() {
		return instanceFactory;
	}

	@Override
	public PropertyFactory getPropertyFactory() {
		return propertyFactory;
	}

	public Class<? extends T> getTargetClass() {
		return targetClass;
	}

	protected T createInternal(Class<?> targetClass, Constructor<? extends T> constructor, Object[] params)
			throws Exception {
		try {
			return constructor.newInstance(params);
		} catch (Exception e) {
			ReflectionUtils.handleReflectionException(e);
		}
		throw new IllegalStateException("Should never get here");
	}

	public T create(Object... params) throws Exception {
		Constructor<? extends T> constructor = ReflectionUtils.findConstructorByParameters(getTargetClass(), false,
				params);
		if (constructor == null) {
			throw new NotFoundException(getTargetClass() + "找不到指定的构造方法");
		}

		return createInternal(getTargetClass(), constructor, params);
	}

	public T create(Class<?>[] parameterTypes, Object... params) throws Exception {
		Constructor<? extends T> constructor = ReflectionUtils.getConstructor(getTargetClass(), false, parameterTypes);
		if (constructor == null) {
			throw new NotFoundException(getTargetClass() + "找不到指定的构造方法");
		}

		return createInternal(getTargetClass(), constructor, params);
	}

	public Iterator<ParameterDescriptors> iterator() {
		return new ConstructorParameterDescriptorsIterator(getTargetClass());
	}

	public boolean isInstance() {
		return isInstance(false);
	}

	private volatile AtomicBoolean init = new AtomicBoolean(false);
	private ParameterDescriptors parameterDescriptors;

	public boolean isInstance(boolean supportAbstract) {
		if (init.get()) {
			if (serviceLoader != null && serviceLoader.iterator().hasNext()) {
				return true;
			}

			return parameterDescriptors != null;
		}

		if (init.compareAndSet(false, true)) {
			if (serviceLoader == null) {
				serviceLoader = InstanceUtils.getServiceLoader(targetClass, instanceFactory, propertyFactory);
			}
			
			if(serviceLoader.iterator().hasNext()){
				return true;
			}
			
			if (!supportAbstract && Modifier.isAbstract(getTargetClass().getModifiers())) {
				return false;
			}

			for (ParameterDescriptors parameterDescriptors : this) {
				if (isAccept(parameterDescriptors)) {
					this.parameterDescriptors = parameterDescriptors;
					return true;
				}
			}
		}
		return false;
	}

	public T create() throws Exception {
		if (!isInstance()) {
			throw new NotSupportedException(getTargetClass().getName());
		}

		if (serviceLoader != null) {
			for (T instance : serviceLoader) {
				return instance;
			}
		}

		return create(parameterDescriptors.getTypes(), getParameters(parameterDescriptors));
	}
}
