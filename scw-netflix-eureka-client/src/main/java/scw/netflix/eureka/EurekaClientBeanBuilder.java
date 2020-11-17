package scw.netflix.eureka;

import java.util.Map;

import scw.application.Application;
import scw.application.ApplicationUtils;
import scw.beans.BeanDefinition;
import scw.beans.DefaultBeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.commons.util.IdUtils;
import scw.commons.util.InetUtils;
import scw.commons.util.InetUtilsProperties;
import scw.core.instance.annotation.Configuration;
import scw.core.utils.StringUtils;
import scw.netflix.eureka.metadata.DefaultManagementMetadataProvider;
import scw.netflix.eureka.metadata.ManagementMetadata;
import scw.netflix.eureka.metadata.ManagementMetadataProvider;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;

@Configuration(order = Integer.MIN_VALUE)
public class EurekaClientBeanBuilder implements BeanBuilderLoader {

	@Override
	public BeanDefinition loading(LoaderContext context, BeanBuilderLoaderChain loaderChain) {
		if (context.getTargetClass() == EurekaClientConfig.class
				|| context.getTargetClass() == CloudEurekaInstanceConfig.class) {
			return new EurekaClientConfigBuilder(context);
		}

		if (context.getTargetClass() == EurekaInstanceConfig.class
				|| context.getTargetClass() == CloudEurekaInstanceConfig.class) {
			return new EurekaInstanceConfigBuilder(context);
		}

		if (context.getTargetClass() == ApplicationInfoManager.class) {
			return new ApplicationInfoManagerBuilder(context);
		}

		if (context.getTargetClass() == InetUtils.class) {
			return new InetUtilsBuilder(context);
		}
		
		if (context.getTargetClass() == EurekaClient.class) {
			return new EurekaClientBuilder(context);
		}

		return loaderChain.loading(context);
	}

	private static class ApplicationInfoManagerBuilder extends DefaultBeanDefinition {

		public ApplicationInfoManagerBuilder(LoaderContext loaderContext) {
			super(loaderContext);
		}

		@Override
		public boolean isInstance() {
			return beanFactory.isInstance(EurekaInstanceConfig.class);
		}

		@Override
		public Object create() throws Exception {
			EurekaInstanceConfig eurekaInstanceConfig = beanFactory.getInstance(EurekaInstanceConfig.class);
			InstanceInfo instanceInfo = beanFactory.getInstance(InstanceInfoFactory.class).create(eurekaInstanceConfig);
			return new ApplicationInfoManager(eurekaInstanceConfig, instanceInfo);
		}
	}

	private static class InetUtilsBuilder extends DefaultBeanDefinition {
		public InetUtilsBuilder(LoaderContext loaderContext) {
			super(loaderContext);
		}

		@Override
		public boolean isInstance() {
			return true;
		}

		@Override
		public Object create() throws Exception {
			InetUtilsProperties inetUtilsProperties = beanFactory.getInstance(InetUtilsProperties.class);
			return new InetUtils(inetUtilsProperties);
		}
	}

	private static class EurekaInstanceConfigBuilder extends DefaultBeanDefinition {

		public EurekaInstanceConfigBuilder(LoaderContext loaderContext) {
			super(loaderContext);
		}

		@Override
		public boolean isInstance() {
			return beanFactory.isInstance(InetUtils.class);
		}

		@Override	
		public Object create() throws Exception {
			InetUtils inetUtils = beanFactory.getInstance(InetUtils.class);
			ManagementMetadataProvider managementMetadataProvider = beanFactory.isInstance(
					ManagementMetadataProvider.class) ? beanFactory.getInstance(ManagementMetadataProvider.class)
							: new DefaultManagementMetadataProvider();
			String hostname = propertyFactory.getString("eureka.instance.hostname");
			boolean preferIpAddress = propertyFactory.getBooleanValue("eureka.instance.prefer-ip-address");
			String ipAddress = propertyFactory.getString("eureka.instance.ip-address");
			boolean isSecurePortEnabled = propertyFactory.getBooleanValue("eureka.instance.secure-port-enabled");

			String serverContextPath = propertyFactory.getValue("server.servlet.context-path", String.class, "/");
			int serverPort = ApplicationUtils.getApplicationPort(propertyFactory, 8080);

			Integer managementPort = propertyFactory.getValue("management.server.port", Integer.class, null);
			String managementContextPath = propertyFactory.getString("management.server.servlet.context-path");
			Integer jmxPort = propertyFactory.getValue("com.sun.management.jmxremote.port", Integer.class, null);
			EurekaInstanceConfigBean instance = new EurekaInstanceConfigBean(inetUtils);

			instance.setNonSecurePort(serverPort);
			instance.setInstanceId(IdUtils.getDefaultInstanceId(propertyFactory));
			instance.setPreferIpAddress(preferIpAddress);
			instance.setSecurePortEnabled(isSecurePortEnabled);
			if (StringUtils.hasText(ipAddress)) {
				instance.setIpAddress(ipAddress);
			}

			if (isSecurePortEnabled) {
				instance.setSecurePort(serverPort);
			}

			if (StringUtils.hasText(hostname)) {
				instance.setHostname(hostname);
			}
			String statusPageUrlPath = propertyFactory.getString("eureka.instance.status-page-url-path");
			String healthCheckUrlPath = propertyFactory.getString("eureka.instance.health-check-url-path");

			if (StringUtils.hasText(statusPageUrlPath)) {
				instance.setStatusPageUrlPath(statusPageUrlPath);
			}
			if (StringUtils.hasText(healthCheckUrlPath)) {
				instance.setHealthCheckUrlPath(healthCheckUrlPath);
			}

			ManagementMetadata metadata = managementMetadataProvider.get(instance, serverPort, serverContextPath,
					managementContextPath, managementPort);

			if (metadata != null) {
				instance.setStatusPageUrl(metadata.getStatusPageUrl());
				instance.setHealthCheckUrl(metadata.getHealthCheckUrl());
				if (instance.isSecurePortEnabled()) {
					instance.setSecureHealthCheckUrl(metadata.getSecureHealthCheckUrl());
				}
				Map<String, String> metadataMap = instance.getMetadataMap();
				metadataMap.computeIfAbsent("management.port", k -> String.valueOf(metadata.getManagementPort()));
			} else {
				// without the metadata the status and health check URLs will
				// not be set
				// and the status page and health check url paths will not
				// include the
				// context path so set them here
				if (StringUtils.hasText(managementContextPath)) {
					instance.setHealthCheckUrlPath(managementContextPath + instance.getHealthCheckUrlPath());
					instance.setStatusPageUrlPath(managementContextPath + instance.getStatusPageUrlPath());
				}
			}

			setupJmxPort(instance, jmxPort);
			return instance;
		}
	}
	
	private static void setupJmxPort(EurekaInstanceConfigBean instance, Integer jmxPort) {
		Map<String, String> metadataMap = instance.getMetadataMap();
		if (metadataMap.get("jmx.port") == null && jmxPort != null) {
			metadataMap.put("jmx.port", String.valueOf(jmxPort));
		}
	}
	
	private static class EurekaClientBuilder extends DefaultBeanDefinition {

		public EurekaClientBuilder(LoaderContext loaderContext) {
			super(loaderContext);
		}

		@Override
		public boolean isInstance() {
			return beanFactory.isInstance(ApplicationInfoManager.class)
					&& beanFactory.isInstance(EurekaClientConfig.class) && beanFactory.isInstance(Application.class);
		}

		@Override
		public Object create() throws Exception {
			ApplicationInfoManager applicationInfoManager = beanFactory.getInstance(ApplicationInfoManager.class);
			EurekaClientConfig eurekaClientConfig = beanFactory.getInstance(EurekaClientConfig.class);
			Application application = beanFactory.getInstance(Application.class);
			return new CloudEurekaClient(applicationInfoManager, eurekaClientConfig, application);
		}
	}

	private static class EurekaClientConfigBuilder extends DefaultBeanDefinition {

		public EurekaClientConfigBuilder(LoaderContext loaderContext) {
			super(loaderContext);
		}

		@Override
		public boolean isInstance() {
			return true;
		}

		@Override
		public Object create() throws Exception {
			return new EurekaClientConfigBean();
		}
	}
}
