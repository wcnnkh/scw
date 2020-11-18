package scw.http.server.cors;

import scw.http.server.HttpServiceConfig;

/**
 * 跨域路径注册
 * 
 * @author shuchaowen
 *
 */
public class CorsRegistry extends HttpServiceConfig<Cors> {

	@Override
	public void addMapping(String pattern, Cors cors) {
		Cors corsToUse = cors.isReadyOnly() ? cors : cors.clone().readyOnly();
		super.addMapping(pattern, corsToUse);
	}
}
