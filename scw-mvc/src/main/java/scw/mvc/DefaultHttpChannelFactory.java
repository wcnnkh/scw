package scw.mvc;

import java.io.IOException;

import scw.beans.BeanFactory;
import scw.net.message.multipart.MultipartMessageResolver;
import scw.web.ServerHttpRequest;
import scw.web.ServerHttpResponse;
import scw.web.WebUtils;
import scw.web.jsonp.JsonpUtils;
import scw.web.message.WebMessageConverters;
import scw.web.message.support.DefaultWebMessageConverters;
import scw.web.pattern.HttpPatterns;

public class DefaultHttpChannelFactory implements HttpChannelFactory {
	protected final BeanFactory beanFactory;
	private MultipartMessageResolver multipartMessageResolver;
	private final HttpPatterns<Boolean> jsonpSupportConfig = new HttpPatterns<Boolean>();
	private final HttpPatterns<Boolean> jsonSupportWrapperConfig = new HttpPatterns<Boolean>();
	private final HttpPatterns<Boolean> multipartFormSupportWrapperConfig = new HttpPatterns<Boolean>();
	private final WebMessageConverters webMessageConverters;

	public DefaultHttpChannelFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		webMessageConverters = new DefaultWebMessageConverters(beanFactory.getEnvironment().getConversionService(),
				beanFactory);
	}

	public MultipartMessageResolver getMultipartMessageResolver() {
		return multipartMessageResolver;
	}

	public void setMultipartMessageResolver(MultipartMessageResolver multipartMessageResolver) {
		this.multipartMessageResolver = multipartMessageResolver;
	}

	public final HttpPatterns<Boolean> getJsonpSupportConfig() {
		return jsonpSupportConfig;
	}

	public final HttpPatterns<Boolean> getJsonSupportWrapperConfig() {
		return jsonSupportWrapperConfig;
	}

	public final HttpPatterns<Boolean> getMultipartFormSupportWrapperConfig() {
		return multipartFormSupportWrapperConfig;
	}

	public boolean isSupportJsonWrapper(ServerHttpRequest request) {
		return jsonSupportWrapperConfig.get(request, true);
	}

	public boolean isSupportJsonp(ServerHttpRequest request) {
		return jsonSupportWrapperConfig.get(request, true);
	}

	public boolean isSupportMultipartFormWrapper(ServerHttpRequest request) {
		return multipartFormSupportWrapperConfig.get(request, true);
	}

	public WebMessageConverters getWebMessageConverters() {
		return webMessageConverters;
	}

	public HttpChannel create(ServerHttpRequest request, ServerHttpResponse response) throws IOException {
		ServerHttpRequest requestToUse = request;
		if (isSupportJsonWrapper(requestToUse)) {
			requestToUse = WebUtils.wrapperServerJsonRequest(requestToUse);
		}

		if (isSupportMultipartFormWrapper(requestToUse)) {
			requestToUse = WebUtils.wrapperServerMultipartFormRequest(requestToUse, getMultipartMessageResolver());
		}

		ServerHttpResponse responseToUse = response;
		if (isSupportJsonp(requestToUse)) {
			responseToUse = JsonpUtils.wrapper(requestToUse, responseToUse);
		}
		return new DefaultHttpChannel(beanFactory, requestToUse, responseToUse, webMessageConverters);
	}
}
