package scw.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scw.application.Application;
import scw.core.instance.InstanceUtils;
import scw.core.utils.ClassUtils;
import scw.http.HttpCookie;
import scw.http.server.ServerHttpRequest;
import scw.http.server.ServerHttpResponse;
import scw.util.XUtils;

public final class ServletUtils {
	private static final boolean asyncSupport = ClassUtils.isPresent("javax.servlet.AsyncContext");// 是否支持异步处理
	public static final String ATTRIBUTE_FORWARD_REQUEST_URI = "javax.servlet.forward.request_uri";
	private static final ServletApplicationStartup SERVLET_APPLICATION_STARTUP = InstanceUtils.loadService(ServletApplicationStartup.class,
			"scw.servlet.Servlet3ApplicationStartup", "scw.servlet.DefaultServletApplicationStartup");

	private ServletUtils() {
	};

	/**
	 * 是否支持异步处理(实际是否支持还要判断request)
	 * 
	 * @return
	 */
	public static boolean isAsyncSupport() {
		return asyncSupport;
	}

	public static void jsp(ServletRequest request, ServletResponse response, String page)
			throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher(page);
		dispatcher.forward(request, response);
	}

	public static String getForwardRequestUri(ServletRequest servletRequest) {
		return (String) servletRequest.getAttribute(ATTRIBUTE_FORWARD_REQUEST_URI);
	}

	public static HttpCookie wrapper(Cookie cookie) {
		return new HttpCookie(cookie.getName(), cookie.getValue()).setDomain(cookie.getDomain())
				.setMaxAge(cookie.getMaxAge()).setSecure(cookie.getSecure()).setPath(cookie.getPath());
	}

	public static HttpServletRequest getHttpServletRequest(ServerHttpRequest request) {
		return XUtils.getTarget(request, HttpServletRequest.class);
	}

	public static HttpServletResponse getHttpServletResponse(ServerHttpResponse response) {
		return XUtils.getTarget(response, HttpServletResponse.class);
	}

	public static Application getApplication(ServletContext servletContext){
		return (Application) servletContext.getAttribute(Application.class.getName());
	}
	
	public static void setApplication(ServletContext servletContext, Application application){
		servletContext.setAttribute(Application.class.getName(), application);
	}

	public static ServletApplicationStartup getServletApplicationStartup() {
		return SERVLET_APPLICATION_STARTUP;
	}
}
