package scw.mvc.servlet.http;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scw.beans.BeanFactory;
import scw.json.JSONParseSupport;
import scw.mvc.MVCUtils;
import scw.mvc.http.HttpChannel;
import scw.mvc.http.HttpRequest;
import scw.mvc.http.HttpResponse;
import scw.mvc.parameter.ParameterFilter;

public final class DefaultHttpServletChannelFactory implements HttpServletChannelFactory {
	private BeanFactory beanFactory;
	private Collection<ParameterFilter> parameterFilters;
	private boolean cookieValue;
	private JSONParseSupport jsonParseSupport;
	private String jsonp;

	public DefaultHttpServletChannelFactory(BeanFactory beanFactory, Collection<ParameterFilter> parameterFilters,
			JSONParseSupport jsonParseSupport, boolean cookieValue, String jsonp) {
		this.beanFactory = beanFactory;
		this.parameterFilters = parameterFilters;
		this.cookieValue = cookieValue;
		this.jsonParseSupport = jsonParseSupport;
		this.jsonp = jsonp;
	}

	public HttpChannel getHttpChannel(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		HttpRequest httpRequest = new MyHttpServletRequest(httpServletRequest);
		HttpResponse httpResponse = new MyHttpServletResponse(httpServletResponse);
		if (MVCUtils.isJsonRequest(httpRequest)) {
			return new JsonHttpServletChannel(beanFactory, parameterFilters, jsonParseSupport, cookieValue, httpRequest,
					httpResponse, jsonp);
		} else {
			return new FormHttpServletChannel(beanFactory, parameterFilters, jsonParseSupport, cookieValue, httpRequest,
					httpResponse, jsonp);
		}
	}
}
