package scw.application;

import java.util.List;

import scw.beans.BeanUtils;
import scw.core.GlobalPropertyFactory;
import scw.util.ServiceLoader;
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
		return BeanUtils.loadAllService(clazz, application.getBeanFactory(), application.getPropertyFactory());
	}
	
	public static <T> ServiceLoader<T> getServiceLoader(Class<? extends T> clazz, Application application){
		return BeanUtils.getServiceLoader(clazz, application.getBeanFactory(), application.getPropertyFactory());
	}
	
	public static String getApplicatoinName(PropertyFactory propertyFactory){
		return propertyFactory.getString("application.name");
	}
	
	public static int getApplicationPort(PropertyFactory propertyFactory, int defaultPort){
		return propertyFactory.getValue("server.port", int.class, propertyFactory.getValue("port", int.class, defaultPort));
	}
}
