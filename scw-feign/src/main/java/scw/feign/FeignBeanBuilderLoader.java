package scw.feign;

import scw.beans.BeanDefinition;
import scw.beans.builder.BeanBuilderLoader;
import scw.beans.builder.BeanBuilderLoaderChain;
import scw.beans.builder.LoaderContext;
import scw.core.instance.annotation.Configuration;
import scw.feign.annotation.FeignClient;

@Configuration(order=Integer.MIN_VALUE)
public class FeignBeanBuilderLoader implements BeanBuilderLoader {

	public BeanDefinition loading(LoaderContext context, BeanBuilderLoaderChain loaderChain) {
		FeignClient feignClient = context.getTargetClass().getAnnotation(FeignClient.class);
		if (feignClient != null) {
			return new FeignBeanDefinition(context, feignClient);
		}
		return loaderChain.loading(context);
	}
}
