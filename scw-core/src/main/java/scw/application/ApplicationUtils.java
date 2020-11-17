package scw.application;

import java.util.ArrayList;
import java.util.List;

import scw.compatible.CompatibleUtils;
import scw.core.Constants;
import scw.core.GlobalPropertyFactory;
import scw.core.instance.InstanceUtils;
import scw.util.concurrent.ListenableFuture;
import scw.value.property.PropertyFactory;

public final class ApplicationUtils {

	public static ListenableFuture<Application> run(MainApplication application) {
		Thread run = new Thread(application);
		run.setContextClassLoader(application.getClassLoader());
		run.setName(application.getMainClass().getName());
		run.setDaemon(false);
		run.start();
		return application.getInitializationListenableFuture();
	}

	public static void main(Class<?> mainClass, MainArgs mainArgs) {
		BasePackage basePackage = mainClass.getAnnotation(BasePackage.class);
		if (basePackage == null) {
			Package p = mainClass.getPackage();
			if (p != null) {
				GlobalPropertyFactory.getInstance().setBasePackageName(p.getName());
			}
		} else {
			GlobalPropertyFactory.getInstance().setBasePackageName(basePackage.value());
		}
	}
	
	public static <T> List<T> loadAllService(Class<? extends T> clazz, Application application){
		return loadAllService(clazz, application, clazz.getName().startsWith(Constants.SYSTEM_PACKAGE_NAME));
	}

	public static <T> List<T> loadAllService(Class<? extends T> clazz, Application application, boolean spi) {
		List<T> list = new ArrayList<T>();
		for (T instance : InstanceUtils.getServiceLoader(clazz, application.getBeanFactory(),
				application.getPropertyFactory(), spi? CompatibleUtils.getSpi():null)) {
			list.add(instance);
		}

		for (T instance : InstanceUtils.getConfigurationList(clazz, application.getBeanFactory(),
				application.getPropertyFactory())) {
			list.add(instance);
		}

		return list;
	}
	
	public static String getApplicatoinName(PropertyFactory propertyFactory){
		return propertyFactory.getString("application.name");
	}
	
	public static int getApplicationPort(PropertyFactory propertyFactory, int defaultPort){
		return propertyFactory.getValue("server.port", int.class, propertyFactory.getValue("port", int.class, defaultPort));
	}
}
