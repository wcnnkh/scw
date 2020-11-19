package scw.hikari;

import scw.beans.BeanDefinition;
import scw.beans.DefaultBeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.core.instance.annotation.Configuration;
import scw.db.DB;
import scw.db.DBUtils;
import scw.io.ResourceUtils;

@Configuration(order = Integer.MIN_VALUE)
public class HikariBeanBuilderLoader implements BeanBuilderLoader {

	public BeanDefinition loading(LoaderContext context, BeanBuilderLoaderChain loaderChain) {
		if (context.getTargetClass() == HikariDB.class) {
			return new HikariCPDBBeanDefinitaion(context);
		} else if (DB.class == context.getTargetClass()) {
			return context.getBeanFactory().getDefinition(HikariDB.class);
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
			return new HikariDB(DBUtils.DEFAULT_CONFIGURATION);
		}
	}
}
