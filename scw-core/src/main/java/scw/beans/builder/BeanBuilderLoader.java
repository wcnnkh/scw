package scw.beans.builder;

import scw.aop.annotation.AopEnable;
import scw.beans.BeanDefinition;

@AopEnable(false)
public interface BeanBuilderLoader {
	BeanDefinition loading(LoaderContext context,
			BeanBuilderLoaderChain loaderChain);
}