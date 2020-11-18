package scw.http.server.resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import scw.core.Assert;
import scw.core.utils.ArrayUtils;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.io.Resource;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.net.FileMimeTypeUitls;
import scw.net.MimeType;
import scw.util.DefaultStringMatcher;
import scw.util.StringMatcher;
import scw.util.XUtils;

public abstract class AbstractStaticResourceLoader implements StaticResourceLoader {
	private static Logger logger = LoggerUtils.getLogger(StaticResourceLoader.class);
	private final TreeMap<String, TreeSet<String>> mappingMap = new TreeMap<String, TreeSet<String>>();
	private StringMatcher matcher = DefaultStringMatcher.getInstance();
	private String defaultFileName = "index.html";

	public StringMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(StringMatcher matcher) {
		Assert.requiredArgument(matcher != null, "matcher");
		this.matcher = matcher;
	}

	public String getDefaultFileName() {
		return defaultFileName;
	}

	public void setDefaultFileName(String defaultFileName) {
		Assert.requiredArgument(StringUtils.isNotEmpty(defaultFileName), "defaultFileName");
		this.defaultFileName = defaultFileName;
	}

	public void addMapping(String location, Collection<String> mappings) {
		Assert.requiredArgument(!CollectionUtils.isEmpty(mappings), "mappings");
		Assert.requiredArgument(location != null, "location");

		TreeSet<String> paths = mappingMap.get(location);
		if (paths == null) {
			paths = new TreeSet<String>(XUtils.getComparator(getMatcher()));
		}

		for (String mapping : mappings) {
			paths.add(mapping);
		}
		mappingMap.put(location, paths);
		logger.info("add mapping {} -> {}", location, mappings);
	}

	public final void addMapping(String location, String... mappings) {
		Assert.requiredArgument(!ArrayUtils.isEmpty(mappings) && StringUtils.isNotEmpty(mappings), "mapping");
		addMapping(location, Arrays.asList(mappings));
	}

	public MimeType getMimeType(Resource resource) {
		return FileMimeTypeUitls.getMimeType(resource);
	}

	public Resource getResource(String location) {
		if (mappingMap.isEmpty()) {
			return null;
		}

		String locationToUse = location.endsWith("/") ? (location + getDefaultFileName()) : location;
		for (Entry<String, TreeSet<String>> entry : mappingMap.entrySet()) {
			for (String path : entry.getValue()) {
				boolean accept = matcher.isPattern(path) ? matcher.match(path, location) : path.equals(location);
				if (accept) {
					String resourceRoot = entry.getKey();
					if (resourceRoot.endsWith("/")) {
						locationToUse = resourceRoot
								+ (locationToUse.startsWith("/") ? locationToUse.substring(1) : locationToUse);
					} else {
						locationToUse = resourceRoot
								+ (locationToUse.startsWith("/") ? locationToUse : ("/" + locationToUse));
					}

					locationToUse = StringUtils.cleanPath(locationToUse);
					return getResourceInternal(locationToUse);
				}
			}
		}
		return null;
	}

	protected abstract Resource getResourceInternal(String location);

	@Override
	public String toString() {
		return mappingMap.toString();
	}
}
