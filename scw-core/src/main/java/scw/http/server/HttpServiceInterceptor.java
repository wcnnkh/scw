package scw.http.server;

import java.io.IOException;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface HttpServiceInterceptor {
	void intercept(ServerHttpRequest request, ServerHttpResponse response, HttpService httpService) throws IOException;
}
