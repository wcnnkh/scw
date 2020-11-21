package scw.http.server;

import java.io.IOException;

import scw.http.HttpMethod;
import scw.http.server.cors.Cors;
import scw.http.server.cors.CorsRegistry;
import scw.http.server.cors.CorsUtils;
import scw.http.server.jsonp.JsonpUtils;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.web.WebUtils;

public abstract class AbstractHttpService implements HttpService {
	private static Logger logger = LoggerFactory.getLogger(HttpService.class);
	private final HttpServiceHandlerAccessor handlerAccessor = new HttpServiceHandlerAccessor();
	private HttpServiceConfigAccessor httpServiceConfigAccessor;
	private CorsRegistry corsRegistry;

	public void service(ServerHttpRequest request, ServerHttpResponse response) throws IOException {
		CorsRegistry corsRegistry = getCorsRegistry();
		if (corsRegistry != null) {
			if (CorsUtils.isCorsRequest(request)) {
				Cors cors = corsRegistry.getConfig(request);
				if (cors != null) {
					cors.write(request, response.getHeaders());
				}
			}
		}

		ServerHttpRequest requestToUse = wrapperRequest(request, getHttpServiceConfigAccessor());
		ServerHttpResponse responseToUse = wrapperResponse(requestToUse, response, getHttpServiceConfigAccessor());

		if (logger.isDebugEnabled()) {
			logger.debug(requestToUse);
		}

		WebUtils.setLocalServerHttpRequest(requestToUse);
		Iterable<? extends HttpServiceInterceptor> interceptors = getHttpServiceInterceptors();
		HttpService service = new HttpServiceInterceptorChain(interceptors == null ? null : interceptors.iterator(),
				handlerAccessor);
		try {
			service.service(requestToUse, responseToUse);
		} finally {
			try {
				if (!responseToUse.isCommitted()) {
					if (requestToUse.isSupportAsyncControl()) {
						ServerHttpAsyncControl serverHttpAsyncControl = requestToUse.getAsyncControl(responseToUse);
						if (serverHttpAsyncControl.isStarted()) {
							serverHttpAsyncControl.addListener(new ServerHttpResponseCompleteAsyncListener(responseToUse));
							return;
						}
					}
				}
				responseToUse.close();
			} catch (Exception e) {
				WebUtils.setLocalServerHttpRequest(null);
			}
		}
	}

	public HttpServiceHandlerAccessor getHandlerAccessor() {
		return handlerAccessor;
	}

	protected ServerHttpRequest wrapperRequest(ServerHttpRequest request, HttpServiceConfigAccessor configAccessor)
			throws IOException {
		if (request.getMethod() == HttpMethod.GET) {
			return request;
		}

		ServerHttpRequest requestToUse = WebUtils.wrapperServerJsonRequest(request, configAccessor);
		requestToUse = WebUtils.wrapperServerMultipartFormRequest(requestToUse, configAccessor);
		return requestToUse;
	}

	protected ServerHttpResponse wrapperResponse(ServerHttpRequest request, ServerHttpResponse response,
			HttpServiceConfigAccessor configAccessor) throws IOException {
		return JsonpUtils.wrapper(request, response, configAccessor);
	}

	public HttpServiceConfigAccessor getHttpServiceConfigAccessor() {
		return httpServiceConfigAccessor;
	}

	public void setHttpServiceConfigAccessor(HttpServiceConfigAccessor httpServiceConfigAccessor) {
		this.httpServiceConfigAccessor = httpServiceConfigAccessor;
	}

	public CorsRegistry getCorsRegistry() {
		return corsRegistry;
	}

	public void setCorsRegistry(CorsRegistry corsRegistry) {
		this.corsRegistry = corsRegistry;
	}

	public abstract Iterable<? extends HttpServiceInterceptor> getHttpServiceInterceptors();
}
