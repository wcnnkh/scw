package scw.druid;

import scw.beans.BeanDefinition;
import scw.beans.DefaultBeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.core.instance.annotation.Configuration;
import scw.db.DB;
import scw.db.DBUtils;
import scw.io.ResourceUtils;

@Configuration(order = Integer.MIN_VALUE + 1)
public class DruidDBBeanBuilderLoader implements BeanBuilderLoader {

	public BeanDefinition loading(LoaderContext context, BeanBuilderLoaderChain loaderChain) {
		if (context.getTargetClass() == DruidDB.class) {
			return new DruidDBBeanDefinition(context);
		} else if (DB.class == context.getTargetClass()) {
			return context.getBeanFactory().getDefinition(DruidDB.class);
		}
		return loaderChain.loading(context);
	}

	private static class DruidDBBeanDefinition extends DefaultBeanDefinition {
		private final boolean isInstance = ResourceUtils.getResourceOperations().isExist(DBUtils.DEFAULT_CONFIGURATION);

		public DruidDBBeanDefinition(LoaderContext loaderContext) {
			super(loaderContext);
		}

		public boolean isInstance() {
			return isInstance;
		}

		public Object create() throws Exception {
			return new DruidDB(DBUtils.DEFAULT_CONFIGURATION);
		}
	}
}
