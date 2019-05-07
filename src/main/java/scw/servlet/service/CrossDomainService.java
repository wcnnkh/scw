package scw.servlet.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import scw.beans.annotation.Bean;
import scw.core.utils.StringUtils;
import scw.servlet.Filter;
import scw.servlet.FilterChain;
import scw.servlet.Request;
import scw.servlet.Response;

/**
 * 跨域
 * 
 * @author shuchaowen
 *
 */
@Bean(proxy = false)
public class CrossDomainService implements Filter {
	private static final CrossDomainDefinition DEFAULT = new CrossDomainDefinition(
			"*", "*", "*", false, -1);
	private Map<String, CrossDomainDefinition> crossDomainDefinitionMap = new HashMap<String, CrossDomainDefinition>();

	public synchronized void register(String matchPath, String origin,
			String methods, int maxAge, String headers, boolean credentials) {
		crossDomainDefinitionMap.put(matchPath, new CrossDomainDefinition(
				origin, headers, methods, credentials, maxAge));
	}

	public CrossDomainDefinition getCrossDomainDefinition(String requestPath) {
		if (crossDomainDefinitionMap.isEmpty()) {
			return null;
		}

		for (Entry<String, CrossDomainDefinition> entry : crossDomainDefinitionMap
				.entrySet()) {
			if (StringUtils.test(requestPath, entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	public void doFilter(Request request, Response response,
			FilterChain filterChain) throws Throwable {
		CrossDomainDefinition crossDomainDefinition = getCrossDomainDefinition(request
				.getServletPath());
		if (crossDomainDefinition == null) {
			DEFAULT.write(response);
		} else {
			crossDomainDefinition.write(response);
		}
		filterChain.doFilter(request, response);
	}
}