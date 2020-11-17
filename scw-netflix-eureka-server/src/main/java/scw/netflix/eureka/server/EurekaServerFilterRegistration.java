package scw.netflix.eureka.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import scw.core.instance.annotation.Configuration;
import scw.netflix.eureka.EurekaConstants;
import scw.servlet.FilterRegistration;
import scw.util.ClassScanner;

@Configuration(order = Integer.MIN_VALUE)
public class EurekaServerFilterRegistration implements FilterRegistration {
	/**
	 * List of packages containing Jersey resources required by the Eureka
	 * server.
	 */
	private static final String[] EUREKA_PACKAGES = new String[] { "com.netflix.discovery", "com.netflix.eureka" };

	public static Application getApplication() {
		Set<Class<?>> classes = new HashSet<>();
		for (Class<?> clazz : ClassScanner.getInstance().getClasses(EUREKA_PACKAGES)) {
			if (clazz.getAnnotation(Path.class) != null
					|| clazz.getAnnotation(javax.ws.rs.ext.Provider.class) != null) {
				classes.add(clazz);
			}
		}

		// Construct the Jersey ResourceConfig
		Map<String, Object> propsAndFeatures = new HashMap<>();
		propsAndFeatures.put(
				// Skip static content used by the webapp
				ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX,
				EurekaConstants.DEFAULT_PREFIX + "/(fonts|images|css|js)/.*");
		DefaultResourceConfig rc = new DefaultResourceConfig(classes);
		rc.setPropertiesAndFeatures(propsAndFeatures);
		return rc;
	}

	@Override
	public Filter getFilter() {
		return new ServletContainer(getApplication());
	}

	@Override
	public Collection<String> getUrlPatterns() {
		return Collections.singletonList(EurekaConstants.DEFAULT_PREFIX + ALL);
	}

}
