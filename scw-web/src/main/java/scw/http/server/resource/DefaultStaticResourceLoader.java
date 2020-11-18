package scw.http.server.resource;

import scw.core.Assert;
import scw.core.utils.ArrayUtils;
import scw.io.Resource;
import scw.io.ResourceLoader;
import scw.io.ResourceUtils;
import scw.value.property.PropertyFactory;

public class DefaultStaticResourceLoader extends AbstractStaticResourceLoader {
	private ResourceLoader resourceLoader = ResourceUtils.getResourceOperations();

	public DefaultStaticResourceLoader() {
	}

	public DefaultStaticResourceLoader(PropertyFactory propertyFactory) {
		String[] paths = propertyFactory.getObject("http.static.resource.path", String[].class);
		if (!ArrayUtils.isEmpty(paths)) {
			String resourceRoot = propertyFactory.getString("http.static.resource.root");
			resourceRoot = resourceRoot == null ? "" : resourceRoot;
			addMapping(resourceRoot, paths);
		}
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		Assert.requiredArgument(resourceLoader != null, "resourceLoader");
		this.resourceLoader = resourceLoader;
	}

	@Override
	protected Resource getResourceInternal(String location) {
		return resourceLoader.getResource(location);
	}
}
