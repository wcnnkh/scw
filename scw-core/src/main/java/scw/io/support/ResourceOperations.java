package scw.io.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import scw.core.GlobalPropertyFactory;
import scw.core.utils.ArrayUtils;
import scw.core.utils.CollectionUtils;
import scw.io.DefaultResourceLoader;
import scw.io.Resource;
import scw.io.event.NonexistentObservableResource;
import scw.io.event.ObservableResource;
import scw.io.event.ObservableResourceUtils;
import scw.util.ConcurrentReferenceHashMap;

public class ResourceOperations extends DefaultResourceLoader {
	private static final String CONFIG_SUFFIX = "SHUCHAOWEN_CONFIG_SUFFIX";
	private static final String RESOURCE_SUFFIX = "scw_res_suffix";
	private final boolean cacheEnable;
	private final ConcurrentMap<String, Resource> resourceCache;

	public ResourceOperations(boolean cacheEnable) {
		this.cacheEnable = cacheEnable;
		this.resourceCache = cacheEnable ? new ConcurrentReferenceHashMap<String, Resource>() : null;
	}

	public boolean isCacheEnable() {
		return cacheEnable;
	}

	/**
	 * 可使用的资源别名，使用优先级从左到右
	 * 
	 * @return
	 */
	protected String[] getResourceEnvironmentalNames() {
		return GlobalPropertyFactory.getInstance().getValue(RESOURCE_SUFFIX, String[].class,
				GlobalPropertyFactory.getInstance().getObject(CONFIG_SUFFIX, String[].class));
	}

	/**
	 * 可使用的资源列表，使用优先级从左到右
	 * 
	 * @param resourceName
	 * @return
	 */
	public List<String> getEnvironmentalResourceNameList(String resourceName) {
		String[] suffixs = getResourceEnvironmentalNames();
		String resourceNameToUse = GlobalPropertyFactory.getInstance().format(resourceName);
		if (ArrayUtils.isEmpty(suffixs)) {
			return Arrays.asList(resourceNameToUse);
		}

		List<String> list = new ArrayList<String>(suffixs.length + 1);
		for (int i = suffixs.length - 1; i >= 0; i--) {
			list.add(getEnvironmentalResourceName(resourceNameToUse, suffixs[i]));
		}
		list.add(resourceNameToUse);
		return list;
	}

	protected String getEnvironmentalResourceName(String resourceName, String evnironmental) {
		int index = resourceName.lastIndexOf(".");
		if (index == -1) {// 不存在
			return resourceName + evnironmental;
		} else {
			return resourceName.substring(0, index) + evnironmental + resourceName.substring(index);
		}
	};

	protected Resource getResourceInternal(String location) {
		if (isCacheEnable()) {
			Resource resource = resourceCache.get(location);
			if (resource == null) {
				resource = super.getResource(location);
				Resource cache = resourceCache.putIfAbsent(location, resource);
				if (cache != null) {
					resource = cache;
				}

				if (resource == null) {
					resourceCache.putIfAbsent(location, Resource.NONEXISTENT_RESOURCE);
				}
			}
			return resource;
		}

		return super.getResource(location);
	}

	/**
	 * 可使用的资源列表，使用优先级从左到右,从高到低
	 * 
	 * @param resource
	 * @return
	 */
	public List<Resource> getResources(String resource) {
		List<String> nameList = getEnvironmentalResourceNameList(resource);
		List<Resource> resources = new ArrayList<Resource>(nameList.size());
		for (String name : nameList) {
			Resource res = getResourceInternal(name);
			if (res == null) {
				continue;
			}
			resources.add(res);
		}
		return resources;
	}

	@Override
	public Resource getResource(String location) {
		List<String> nameList = getEnvironmentalResourceNameList(location);
		Iterator<String> iterator = nameList.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			Resource resource = getResourceInternal(name);
			if (!iterator.hasNext() || (resource != null && resource.exists())) {
				return resource;
			}
		}
		return null;
	}

	public ObservableResource<Properties> getProperties(String resource) {
		return getProperties(resource, null);
	}

	public ObservableResource<Properties> getProperties(String resource, String charsetName) {
		List<Resource> resources = getResources(resource);
		if (CollectionUtils.isEmpty(resources)) {
			return new NonexistentObservableResource<Properties>();
		}

		return ObservableResourceUtils.getProperties(CollectionUtils.reversal(resources), charsetName);
	}

	public ObservableResource<Properties> getProperties(Collection<String> resources, String charsetName) {
		List<Resource> list = new ArrayList<Resource>(resources.size());
		for (String resource : resources) {
			Resource res = getResource(resource);
			if (res == null) {
				continue;
			}

			list.add(res);
		}
		return ObservableResourceUtils.getProperties(list, charsetName);
	}

	public boolean isExist(String resource) {
		Resource res = getResource(resource);
		return res != null && res.exists();
	}
}
