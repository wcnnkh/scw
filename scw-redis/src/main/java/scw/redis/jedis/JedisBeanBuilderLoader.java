package scw.redis.jedis;

import java.util.Properties;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scw.beans.BeanDefinition;
import scw.beans.DefaultBeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.core.Constants;
import scw.core.instance.annotation.Configuration;
import scw.core.utils.StringUtils;
import scw.io.ResourceUtils;
import scw.util.ConfigUtils;
import scw.value.property.PropertyFactory;

@Configuration(order = Integer.MIN_VALUE, value = BeanBuilderLoader.class)
public class JedisBeanBuilderLoader implements BeanBuilderLoader {
	private static final String HOST_CONFIG_KEY = "redis.host";
	private static final String CONFIG_KEY = "redis.configuration";
	private static final String DEFAULT_CONFIG = "/redis/redis.properties";
	
	public BeanDefinition loading(LoaderContext context, BeanBuilderLoaderChain loaderChain) {
		if (context.getTargetClass() == JedisPool.class) {
			return new JedisPoolBeanDefinition(context);
		} else if (context.getTargetClass() == JedisPoolConfig.class) {
			return new JedisPoolConfigBeanDefinition(context);
		}
		return loaderChain.loading(context);
	}

	private static final class JedisPoolBeanDefinition extends DefaultBeanDefinition {
		private final String configName = propertyFactory.getValue(CONFIG_KEY, String.class, DEFAULT_CONFIG);
		private final boolean isExist = ResourceUtils.getResourceOperations().isExist(configName);

		public JedisPoolBeanDefinition(LoaderContext context) {
			super(context);
		}

		public boolean isInstance() {
			return getHost() != null;
		}

		private String getHost() {
			String host = propertyFactory.getString(HOST_CONFIG_KEY);
			if (host == null && isExist) {
				Properties properties = ResourceUtils.getResourceOperations()
						.getProperties(configName, Constants.DEFAULT_CHARSET_NAME).getResource();
				host = properties.getProperty(HOST_CONFIG_KEY);
				if (host == null) {
					host = properties.getProperty("host");// 兼容老版本
				}
			}
			return host;
		}

		public Object create() throws Exception {
			String host = getHost();
			if (beanFactory.isInstance(JedisPoolConfig.class)) {
				return new JedisPool(beanFactory.getInstance(JedisPoolConfig.class), host);
			} else {
				return new JedisPool(host);
			}
		}

		@Override
		public void destroy(Object instance) throws Throwable {
			super.destroy(instance);
			if (instance instanceof JedisPool) {
				if (!((JedisPool) instance).isClosed()) {
					((JedisPool) instance).close();
				}
			}
		}
	}

	private static final class JedisPoolConfigBeanDefinition extends DefaultBeanDefinition {
		private final String configName = propertyFactory.getValue(CONFIG_KEY, String.class, DEFAULT_CONFIG);
		private final boolean isExist = ResourceUtils.getResourceOperations().isExist(configName);

		public JedisPoolConfigBeanDefinition(LoaderContext context) {
			super(context);
		}

		public boolean isInstance() {
			return isExist;
		}

		public Object create() throws Exception {
			PropertyFactory propertyFactory = new PropertyFactory(false, true);
			propertyFactory.loadProperties(configName, Constants.DEFAULT_CHARSET_NAME);
			String host = propertyFactory.getString(HOST_CONFIG_KEY);
			if (StringUtils.isEmpty(host)) {
				host = "127.0.0.1";
			}

			JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
			// 兼容老版本
			ConfigUtils.loadProperties(jedisPoolConfig, propertyFactory, null, null);
			ConfigUtils.loadProperties(jedisPoolConfig, propertyFactory, null, "redis.");
			return jedisPoolConfig;
		}
	}
}
