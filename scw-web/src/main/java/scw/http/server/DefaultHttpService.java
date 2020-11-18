package scw.http.server;

import java.util.ArrayList;
import java.util.List;

import scw.beans.BeanFactory;
import scw.core.instance.InstanceUtils;
import scw.http.server.cors.CorsRegistry;
import scw.http.server.resource.DefaultStaticResourceLoader;
import scw.http.server.resource.StaticResourceHttpServiceHandler;
import scw.http.server.resource.StaticResourceLoader;
import scw.value.property.PropertyFactory;

public class DefaultHttpService extends AbstractHttpService {
	private final List<HttpServiceInterceptor> interceptors = new ArrayList<HttpServiceInterceptor>();

	public DefaultHttpService(BeanFactory beanFactory, PropertyFactory propertyFactory) {
		if (beanFactory.isInstance(CorsRegistry.class)) {
			setCorsRegistry(beanFactory.getInstance(CorsRegistry.class));
		}
		StaticResourceLoader staticResourceLoader = beanFactory.isInstance(StaticResourceLoader.class)
				? beanFactory.getInstance(StaticResourceLoader.class)
				: new DefaultStaticResourceLoader(propertyFactory);
		StaticResourceHttpServiceHandler resourceHandler = new StaticResourceHttpServiceHandler();
		resourceHandler.setResourceLoader(staticResourceLoader);
		getHandlerAccessor().bind(resourceHandler);
		interceptors
				.addAll(InstanceUtils.getConfigurationList(HttpServiceInterceptor.class, beanFactory, propertyFactory));

		List<HttpServiceHandler> httpServiceHandlers = InstanceUtils.getConfigurationList(HttpServiceHandler.class,
				beanFactory, propertyFactory);
		getHandlerAccessor().bind(httpServiceHandlers);

		if (beanFactory.isInstance(HttpServiceConfigAccessor.class)) {
			setHttpServiceConfigAccessor(beanFactory.getInstance(HttpServiceConfigAccessor.class));
		}
	}

	public List<HttpServiceInterceptor> getHttpServiceInterceptors() {
		return interceptors;
	}
}
