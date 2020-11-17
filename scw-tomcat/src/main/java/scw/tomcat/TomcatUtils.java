package scw.tomcat;

import java.util.Properties;

import scw.application.ApplicationUtils;
import scw.core.utils.StringUtils;
import scw.io.ResourceUtils;
import scw.value.property.PropertyFactory;

public final class TomcatUtils {
	private TomcatUtils() {
	};

	private static String getProperty(PropertyFactory propertyFactory, String name) {
		return propertyFactory.getString("tomcat." + name);
	}

	public static int getPort(PropertyFactory propertyFactory) {
		//兼容老版本
		int defaultPort = StringUtils.parseInt(getProperty(propertyFactory, "port"), 8080);
		return ApplicationUtils.getApplicationPort(propertyFactory, defaultPort);
	}

	public static String getBaseDir(PropertyFactory propertyFactory) {
		return getProperty(propertyFactory, "basedir");
	}

	public static String getContextPath(PropertyFactory propertyFactory) {
		return getProperty(propertyFactory, "contextPath");
	}

	public static String getDefaultServletMapping(PropertyFactory propertyFactory) {
		return getProperty(propertyFactory, "source");
	}

	public static String getTomcatProtocol(PropertyFactory propertyFactory) {
		return getProperty(propertyFactory, "protocol");
	}

	public static String getTomcatConnectorName(PropertyFactory propertyFactory) {
		return getProperty(propertyFactory, "connector");
	}

	public static String getTomcatContextManager(PropertyFactory propertyFactory) {
		return getProperty(propertyFactory, "context.manager");
	}

	private static String getShutdownProperty(PropertyFactory propertyFactory, String name) {
		return getProperty(propertyFactory, "shutdown." + name);
	}

	public static String getShutdownPath(PropertyFactory propertyFactory) {
		return getShutdownProperty(propertyFactory, "path");
	}

	public static String getShutdownName(PropertyFactory propertyFactory) {
		return getShutdownProperty(propertyFactory, "name");
	}

	public static String getShutdownIp(PropertyFactory propertyFactory) {
		return getShutdownProperty(propertyFactory, "ip");
	}

	public static String getShutdownUserName(PropertyFactory propertyFactory) {
		return getShutdownProperty(propertyFactory, "username");
	}

	public static String getShutdownPassword(PropertyFactory propertyFactory) {
		return getShutdownProperty(propertyFactory, "password");
	}

	public static boolean tomcatScanTld(PropertyFactory propertyFactory) {
		return StringUtils.parseBoolean(getProperty(propertyFactory, "scan.tld"), true);
	}

	public static Properties getServletInitParametersConfig(String servletName, boolean loadOnStartup) {
		String path = servletName + "-servlet-init-params.properties";
		Properties properties = new Properties();
		if (loadOnStartup) {
			properties.put("load-on-startup", 1);
		}

		if (ResourceUtils.getResourceOperations().isExist(path)) {
			properties.putAll(ResourceUtils.getResourceOperations().getProperties(path).getResource());
		}
		return properties;
	}
}
