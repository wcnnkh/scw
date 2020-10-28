package scw.db.hikaricp;

import scw.beans.DefaultBeanDefinition;
import scw.beans.BeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.core.instance.annotation.Configuration;
import scw.db.DB;
import scw.db.DBUtils;
import scw.io.ResourceUtils;
import scw.memcached.Memcached;
import scw.redis.Redis;
import scw.value.property.PropertyFactory;

@Configuration(order = Integer.MIN_VALUE)
public class HikaricpBeanBuilderLoader implements BeanBuilderLoader {

	public BeanDefinition loading(LoaderContext context, BeanBuilderLoaderChain loaderChain) {
		if (context.getTargetClass() == HikariCPDB.class) {
			return new HikariCPDBBeanDefinitaion(context);
		} else if (DB.class == context.getTargetClass()) {
			return context.getBeanFactory().getDefinition(HikariCPDB.class);
		}
		return loaderChain.loading(context);
	}

	private static class HikariCPDBBeanDefinitaion extends DefaultBeanDefinition {
		private final boolean isInstance = ResourceUtils.getResourceOperations().isExist(DBUtils.DEFAULT_CONFIGURATION);

		public HikariCPDBBeanDefinitaion(LoaderContext loaderContext) {
			super(loaderContext);
		}

		public boolean isInstance() {
			return isInstance;
		}

		public Object create() throws Exception {
			PropertyFactory propertyFactory = new PropertyFactory(false, true);
			propertyFactory.loadProperties(DBUtils.DEFAULT_CONFIGURATION, "UTF-8").registerListener();
			if (beanFactory.isInstance(Memcached.class)) {
				return new HikariCPDB(propertyFactory, beanFactory.getInstance(Memcached.class));
			} else if (beanFactory.isInstance(Redis.class)) {
				return new HikariCPDB(propertyFactory, beanFactory.getInstance(Redis.class));
			} else {
				return new HikariCPDB(propertyFactory);
			}
		}
	}
}
