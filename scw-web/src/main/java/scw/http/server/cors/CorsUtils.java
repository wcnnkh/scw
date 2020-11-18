package scw.http.server.cors;

import scw.http.HttpHeaders;
import scw.http.HttpMethod;
import scw.http.server.ServerHttpRequest;

public class CorsUtils {
	/**
	 * 是否是一个有效的cors请求
	 */
	public static boolean isCorsRequest(ServerHttpRequest request) {
		return request.getHeaders().getOrigin() != null;
	}

	/**
	 * 是否是一个cors前置请求(OPTIONS请求)
	 */
	public static boolean isPreFlightRequest(ServerHttpRequest request) {
		return (isCorsRequest(request) && HttpMethod.OPTIONS.matches(request.getMethod())
				&& request.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null);
	}
}