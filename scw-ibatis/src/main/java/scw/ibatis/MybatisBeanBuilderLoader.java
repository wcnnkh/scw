package scw.ibatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import scw.beans.DefaultBeanDefinition;
import scw.beans.BeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.core.instance.annotation.Configuration;
import scw.io.Resource;
import scw.io.ResourceUtils;

@Configuration(order = Integer.MIN_VALUE, value = BeanBuilderLoader.class)
public class MybatisBeanBuilderLoader implements BeanBuilderLoader {

	public BeanDefinition loading(LoaderContext context, BeanBuilderLoaderChain loaderChain) {
		if (context.getTargetClass() == SqlSessionFactory.class) {
			return new SqlSessionFactoryBeanBuilder(context);
		}
		return loaderChain.loading(context);
	}

	private static final class SqlSessionFactoryBeanBuilder extends DefaultBeanDefinition {

		public SqlSessionFactoryBeanBuilder(LoaderContext context) {
			super(context);
		}

		public boolean isInstance() {
			return true;
		}

		public Object create() throws Exception {
			Resource resource = ResourceUtils.getResourceOperations().getResource("mybatis-config.xml");
			if (resource.exists()) {
				return new SqlSessionFactoryBuilder().build(ResourceUtils.getInputStream(resource));
			} else {
				return new SqlSessionFactoryBuilder()
						.build(beanFactory.getInstance(org.apache.ibatis.session.Configuration.class));
			}
		}
	}
}
