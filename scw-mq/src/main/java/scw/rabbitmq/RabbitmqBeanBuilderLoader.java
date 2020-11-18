package scw.rabbitmq;

import java.util.Properties;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConnectionFactoryConfigurator;

import scw.amqp.Exchange;
import scw.amqp.ExchangeDeclare;
import scw.beans.DefaultBeanDefinition;
import scw.beans.BeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.complete.CompleteService;
import scw.core.instance.annotation.Configuration;
import scw.io.ResourceUtils;
import scw.io.SerializerUtils;
import scw.util.ConfigUtils;

@Configuration(order = Integer.MIN_VALUE)
public class RabbitmqBeanBuilderLoader implements BeanBuilderLoader {
	public static final String DEFAULT_CONFIG = ResourceUtils.CLASSPATH_URL_PREFIX + "/rabbitmq/rabbitmq.properties";

	public BeanDefinition loading(LoaderContext context, BeanBuilderLoaderChain loaderChain) {
		if (context.getTargetClass() == ConnectionFactory.class) {
			return new ConnectionFactoryBeanBuilder(context);
		} else if (context.getTargetClass() == Connection.class) {
			return new ConnectionBeanBuilder(context);
		} else if (Exchange.class == context.getTargetClass() || RabbitmqExchange.class == context.getTargetClass()) {
			return new ExchangeBeanBuilder(context);
		} else if (context.getTargetClass() == ExchangeDeclare.class) {
			return new ExchangeDeclareBeanBuilder(context);
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
			return beanFactory.getInstance(ConnectionFactory.class).newConnection();
		}

		@Override
		public void destroy(Object instance) throws Throwable {
			super.destroy(instance);
			if (instance instanceof Connection) {
				((Connection) instance).close();
			}
		}
	}

	private static class ConnectionFactoryBeanBuilder extends DefaultBeanDefinition {
		private final boolean exist = ResourceUtils.getResourceOperations().isExist(DEFAULT_CONFIG);

		public ConnectionFactoryBeanBuilder(LoaderContext context) {
			super(context);
		}

		public boolean isInstance() {
			return exist;
		}

		public Object create() throws Exception {
			ConnectionFactory connectionFactory = new ConnectionFactory();
			Properties properties = ResourceUtils.getResourceOperations().getProperties(DEFAULT_CONFIG).getResource();
			ConnectionFactoryConfigurator.load(connectionFactory, properties, null);
			ConnectionFactoryConfigurator.load(connectionFactory, properties);
			return connectionFactory;
		}
	}

	private static class ExchangeBeanBuilder extends DefaultBeanDefinition {

		public ExchangeBeanBuilder(LoaderContext context) {
			super(context);
		}

		public boolean isInstance() {
			return beanFactory.isInstance(Connection.class) && beanFactory.isInstance(ExchangeDeclare.class)
					&& beanFactory.isInstance(CompleteService.class);
		}

		public Object create() throws Exception {
			return new RabbitmqExchange(SerializerUtils.DEFAULT_SERIALIZER, beanFactory.getInstance(Connection.class),
					beanFactory.getInstance(ExchangeDeclare.class), true);
		}
	}

	private final class ExchangeDeclareBeanBuilder extends DefaultBeanDefinition {
		private final boolean isExist = ResourceUtils.getResourceOperations().isExist(DEFAULT_CONFIG);

		public ExchangeDeclareBeanBuilder(LoaderContext context) {
			super(context);
		}

		@Override
		public boolean isInstance() {
			return isExist;
		}

		@Override
		public Object create() throws Exception {
			Properties properties = ResourceUtils.getResourceOperations().getProperties(DEFAULT_CONFIG).getResource();
			ExchangeDeclare exchangeDeclare = new ExchangeDeclare(null);
			ConfigUtils.loadProperties(exchangeDeclare, properties, null, "exchange.");
			return exchangeDeclare;
		}
	}
}
