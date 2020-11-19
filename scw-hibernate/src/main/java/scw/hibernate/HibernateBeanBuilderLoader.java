package scw.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import scw.beans.DefaultBeanDefinition;
import scw.beans.BeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.core.instance.annotation.Configuration;
import scw.io.Resource;
import scw.io.ResourceUtils;

@Configuration(order = Integer.MIN_VALUE)
public class HibernateBeanBuilderLoader implements BeanBuilderLoader {

	public BeanDefinition loading(LoaderContext context, BeanBuilderLoaderChain serviceChain) {
		if (context.getTargetClass() == org.hibernate.cfg.Configuration.class) {
			return new ConfigurationBeanBuilder(context);
		} else if (context.getTargetClass() == SessionFactory.class) {
			return new SessionFactoryBeanBuilder(context);
		}
		return serviceChain.loading(context);
	}

	private static final class SessionFactoryBeanBuilder extends DefaultBeanDefinition {

		public SessionFactoryBeanBuilder(LoaderContext context) {
			super(context);
		}

		public boolean isInstance() {
			return beanFactory.isInstance(org.hibernate.cfg.Configuration.class);
		}

		public Object create() throws Exception {
			org.hibernate.cfg.Configuration configuration = beanFactory
					.getInstance(org.hibernate.cfg.Configuration.class);
			if (beanFactory.isInstance(ServiceRegistry.class)) {
				return configuration.buildSessionFactory(beanFactory.getInstance(ServiceRegistry.class));
			} else {
				return configuration.buildSessionFactory();
			}
		}

		@Override
		public void destroy(Object instance) throws Throwable {
			if (instance instanceof SessionFactory) {
				((SessionFactory) instance).close();
			}
			super.destroy(instance);
		}
	}

	private static final class ConfigurationBeanBuilder extends DefaultBeanDefinition {
		private final boolean isExist = ResourceUtils.getResourceOperations()
				.isExist(StandardServiceRegistryBuilder.DEFAULT_CFG_RESOURCE_NAME);
		
		public ConfigurationBeanBuilder(LoaderContext context) {
			super(context);
		}

		public boolean isInstance() {
			return isExist;
		}

		public Object create() throws Exception {
			Resource resource = ResourceUtils.getResourceOperations()
					.getResource(StandardServiceRegistryBuilder.DEFAULT_CFG_RESOURCE_NAME);
			return new org.hibernate.cfg.Configuration().configure(resource.getURL());
		}
	}

}
