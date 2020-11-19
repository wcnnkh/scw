package scw.core.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import scw.core.Constants;
import scw.core.GlobalPropertyFactory;
import scw.core.reflect.ReflectionUtils;
import scw.core.utils.ClassUtils;
import scw.core.utils.CollectionUtils;
import scw.lang.NotSupportedException;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.util.ClassScanner;
import scw.util.JavaVersion;
import scw.util.ServiceLoader;
import scw.value.ValueFactory;
import scw.value.property.PropertyFactory;

@SuppressWarnings("rawtypes")
public final class InstanceUtils {
	private static Logger logger = LoggerUtils.getLogger(InstanceUtils.class);

	private InstanceUtils() {
	};

	public static final ConfigurationScanner CONFIGURATION_SCANNER;

	/**
	 * 默认的实例工厂
	 */
	public static final InstanceFactory INSTANCE_FACTORY = new DefaultInstanceFactory(
			GlobalPropertyFactory.getInstance());

	/**
	 * 不调用构造方法实例化对象
	 */
	public static final NoArgsInstanceFactory NO_ARGS_INSTANCE_FACTORY;

	static {
		ConfigurationScanner configurationScanner = loadService(ConfigurationScanner.class);
		CONFIGURATION_SCANNER = configurationScanner == null ? new ConfigurationScanner() : configurationScanner;

		NoArgsInstanceFactory noArgsInstanceFactory = loadService(NoArgsInstanceFactory.class,
				"scw.core.instance.SunNoArgsInstanceFactory", "scw.core.instance.UnsafeNoArgsInstanceFactory");
		NO_ARGS_INSTANCE_FACTORY = noArgsInstanceFactory;
		if (NO_ARGS_INSTANCE_FACTORY == null) {
			throw new NotSupportedException(NoArgsInstanceFactory.class.getName());
		}
	}

	public static <T> T loadService(Class<? extends T> clazz, String... defaultNames) {
		return loadService(clazz, INSTANCE_FACTORY, GlobalPropertyFactory.getInstance(), defaultNames);
	}

	public static <T> List<T> loadAllService(Class<? extends T> clazz, String... defaultNames) {
		ServiceLoader<T> serviceLoader = getServiceLoader(clazz, INSTANCE_FACTORY, GlobalPropertyFactory.getInstance(),
				defaultNames);
		return Collections.list(CollectionUtils.toEnumeration(serviceLoader.iterator()));
	}

	public static <T> T loadService(Class<? extends T> clazz, NoArgsInstanceFactory instanceFactory,
			ValueFactory<String> propertyFactory, String... defaultNames) {
		ServiceLoader<T> serviceLoader = getServiceLoader(clazz, instanceFactory, propertyFactory, defaultNames);
		Iterator<T> iterator = serviceLoader.iterator();
		while (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}

	public static <S> ServiceLoader<S> getServiceLoader(Class<? extends S> clazz, NoArgsInstanceFactory instanceFactory,
			ValueFactory<String> propertyFactory, String... defaultNames) {
		return new ConfigurableServiceLoader<S>(clazz.getName().startsWith(Constants.SYSTEM_PACKAGE_NAME)? new SpiServiceLoader<S>(clazz, instanceFactory):null, clazz, instanceFactory, propertyFactory,
				defaultNames);
	}

	public static <T> List<T> loadAllService(Class<? extends T> clazz, NoArgsInstanceFactory instanceFactory,
			ValueFactory<String> propertyFactory, String... defaultNames) {
		ServiceLoader<T> serviceLoader = getServiceLoader(clazz, instanceFactory, propertyFactory, defaultNames);
		return Collections.list(CollectionUtils.toEnumeration(serviceLoader.iterator()));
	}

	public static <T> Collection<Class<T>> getConfigurationClassList(Class<? extends T> type,
			ValueFactory<String> propertyFactory, Collection<? extends Class> excludeTypes) {
		return getConfigurationClassList(type, excludeTypes, getScannerClassPackages(propertyFactory));
	}

	public static <T> Collection<Class<T>> getConfigurationClassList(Class<? extends T> type,
			ValueFactory<String> propertyFactory, Class... excludeTypes) {
		return getConfigurationClassList(type, propertyFactory, Arrays.asList(excludeTypes));
	}

	public static <T> Collection<Class<T>> getConfigurationClassList(Class<? extends T> type,
			Collection<? extends Class> excludeTypes, Collection<String> packageNames) {
		return CONFIGURATION_SCANNER.scan(type, excludeTypes, packageNames);
	}

	public static <T> List<T> getConfigurationList(Class<? extends T> type, NoArgsInstanceFactory instanceFactory,
			ValueFactory<String> propertyFactory, Collection<? extends Class> excludeTypes) {
		return getConfigurationList(type, instanceFactory, excludeTypes, getScannerClassPackages(propertyFactory));
	}

	public static <T> List<T> getConfigurationList(Class<? extends T> type, NoArgsInstanceFactory instanceFactory,
			Collection<? extends Class> excludeTypes, Collection<String> packageNames) {
		List<T> list = new ArrayList<T>();
		for (Class<? extends T> clazz : getConfigurationClassList(type, excludeTypes, packageNames)) {
			if (!instanceFactory.isInstance(clazz)) {
				logger.debug("factory [{}] not create {} in instance: {}", instanceFactory.getClass(), type, clazz);
				continue;
			}

			list.add(instanceFactory.getInstance(clazz));
		}
		return list;
	}

	public static <T> List<T> getConfigurationList(Class<? extends T> type, NoArgsInstanceFactory instanceFactory,
			ValueFactory<String> propertyFactory, Class... excludeTypes) {
		return getConfigurationList(type, instanceFactory, propertyFactory, Arrays.asList(excludeTypes));
	}

	public static String getScanAnnotationPackageName(ValueFactory<String> propertyFactory) {
		return propertyFactory.getValue("scw.scan.annotation.package", String.class,
				GlobalPropertyFactory.getInstance().getBasePackageName());
	}

	public static <T> T getConfiguration(Class<? extends T> type, NoArgsInstanceFactory instanceFactory,
			Collection<? extends Class> excludeTypes, Collection<String> packageNames) {
		for (Class<? extends T> clazz : getConfigurationClassList(type, excludeTypes, packageNames)) {
			if (!instanceFactory.isInstance(clazz)) {
				logger.debug("factory [{}] not create {} in instance: {}", instanceFactory.getClass(), type, clazz);
				continue;
			}

			return instanceFactory.getInstance(clazz);
		}
		return null;
	}

	public static <T> T getConfiguration(Class<? extends T> type, NoArgsInstanceFactory instanceFactory,
			ValueFactory<String> propertyFactory, Collection<? extends Class> excludeTypes) {
		return getConfiguration(type, instanceFactory, excludeTypes, getScannerClassPackages(propertyFactory));
	}

	public static <T> T getConfiguration(Class<? extends T> type, NoArgsInstanceFactory instanceFactory,
			ValueFactory<String> propertyFactory, Class... excludeTypes) {
		return getConfiguration(type, instanceFactory, propertyFactory, Arrays.asList(excludeTypes));
	}

	public static boolean isSupport(Class<?> clazz) {
		return !ClassUtils.isPrimitiveOrWrapper(clazz) && JavaVersion.isSupported(clazz)
				&& ReflectionUtils.isPresent(clazz);
	}

	public static <T> ServiceLoader<T> getConfigurationServiceLoader(Class<? extends T> serviceClass,
			NoArgsInstanceFactory instanceFactory, Collection<? extends Class> excludeTypes,
			Collection<String> packageNames) {
		return new AnnotationServiceLoader<T>(serviceClass, instanceFactory, excludeTypes, packageNames);
	}

	public static <T> ServiceLoader<T> getConfigurationServiceLoader(Class<? extends T> serviceClass,
			NoArgsInstanceFactory instanceFactory, ValueFactory<String> propertyFactory,
			Collection<? extends Class> excludeTypes) {
		return getConfigurationServiceLoader(serviceClass, instanceFactory, excludeTypes,
				getScannerClassPackages(propertyFactory));
	}
	
	private static Collection<String> getScannerClassPackages(ValueFactory<String> propertyFactory){
		return Arrays.asList(Constants.SYSTEM_PACKAGE_NAME, getScanAnnotationPackageName(propertyFactory));
	}
	
	public static Set<Class<?>> getClasses(PropertyFactory propertyFactory){
		return ClassScanner.getInstance().getClasses(getScannerClassPackages(propertyFactory));
	}
}
