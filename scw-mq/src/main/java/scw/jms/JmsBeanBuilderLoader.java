package scw.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;

import scw.beans.DefaultBeanDefinition;
import scw.beans.BeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.core.instance.annotation.Configuration;

@Configuration(order = Integer.MIN_VALUE)
public class JmsBeanBuilderLoader implements BeanBuilderLoader {

	public BeanDefinition loading(LoaderContext context,
			BeanBuilderLoaderChain loaderChain) {
		if (Connection.class == context.getTargetClass()) {
			return new ConnectionBeanBuilder(context);
		}
		
		return loaderChain.loading(context);
	}

	private static class ConnectionBeanBuilder extends DefaultBeanDefinition {

		public ConnectionBeanBuilder(LoaderContext context) {
			super(context);
		}

		public boolean isInstance() {
			return beanFactory.isInstance(ConnectionFactory.class);
		}

		public Object create() throws Exception {
			return beanFactory.getInstance(ConnectionFactory.class)
					.createConnection();
		}

		@Override
		public void destroy(Object instance) throws Throwable {
			if (instance instanceof Connection) {
				((Connection) instance).close();
			}
			super.destroy(instance);
		}
	}
}
