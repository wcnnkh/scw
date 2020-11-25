package scw.mvc.security;

import scw.http.server.HttpServiceConfig;
import scw.http.server.ServerHttpRequest;

public class LoginRequiredRegistry extends HttpServiceConfig<Boolean> {

	public boolean isLoginRequried(ServerHttpRequest request) {
		Boolean v = getConfig(request);
		if (v == null) {
			return false;
		}
		return v;
	}
}
