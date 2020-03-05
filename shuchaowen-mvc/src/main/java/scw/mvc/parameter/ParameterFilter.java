package scw.mvc.parameter;

import scw.core.parameter.ParameterConfig;
import scw.mvc.Channel;

public interface ParameterFilter {
	Object doFilter(Channel channel, ParameterConfig parameterConfig, ParameterFilterChain chain)
			throws Throwable;
}