package scw.beans.loader;

import scw.beans.builder.BeanBuilder;

public interface BeanBuilderLoaderChain {
	BeanBuilder loading(LoaderContext context);
}