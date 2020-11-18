package scw.http.server.jsonp;

import scw.core.utils.StringUtils;
import scw.http.HttpMethod;
import scw.http.server.HttpServiceConfigAccessor;
import scw.http.server.ServerHttpRequest;
import scw.http.server.ServerHttpResponse;
import scw.lang.Nullable;
import scw.util.XUtils;

public final class JsonpUtils {
	private JsonpUtils() {
	};

	public static final String JSONP_RESP_PREFIX = "(";
	public static final String JSONP_RESP_SUFFIX = ");";
	public static final String JSONP_CALLBACK = "callback";
	private static final String JSONP_CALLBACK_VALID = "^[a-zA-Z_]+[\\w0-9_]*$";

	public static ServerHttpResponse wrapper(ServerHttpRequest request, ServerHttpResponse response,
			@Nullable HttpServiceConfigAccessor configAccessor) {
		if (request.getMethod() != HttpMethod.GET) {
			return response;
		}

		String jsonp = request.getParameterMap().getFirst(JSONP_CALLBACK);
		if (StringUtils.isEmpty(jsonp) || !validCallbackName(jsonp)) {
			return response;
		}

		JsonpServerHttpResponse jsonpServerHttpResponse = XUtils.getTarget(request, JsonpServerHttpResponse.class);
		if (jsonpServerHttpResponse != null) {
			return response;
		}

		if (configAccessor != null && !configAccessor.isSupportJsonp(request)) {
			return response;
		}

		return new JsonpServerHttpResponse(jsonp, response);
	}

	public static boolean validCallbackName(String name) {
		if (name.matches(JSONP_CALLBACK_VALID)) {
			return true;
		}

		return false;
	}
}
