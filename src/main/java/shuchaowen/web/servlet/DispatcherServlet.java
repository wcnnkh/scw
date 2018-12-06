package shuchaowen.web.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shuchaowen.core.connection.http.enums.ContentType;
import shuchaowen.core.util.ConfigUtils;
import shuchaowen.web.servlet.request.FormRequest;
import shuchaowen.web.servlet.request.JsonRequest;

public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = -8268337109249457358L;
	private HttpServerApplication httpServerApplication;

	public HttpServerApplication getHttpServerApplication() {
		return httpServerApplication;
	}
	
	@Override
	public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		if (httpServerApplication.getCharset() != null) {
			req.setCharacterEncoding(httpServerApplication.getCharset().name());
			res.setCharacterEncoding(httpServerApplication.getCharset().name());
		}
		super.service(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (httpServerApplication.isRpcEnabled() && req.getServletPath().equals(httpServerApplication.getRpcServletPath())) {
			try {
				httpServerApplication.rpc(req.getInputStream(), resp.getOutputStream());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else {
			controller(req, resp);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		controller(req, resp);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		controller(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		controller(req, resp);
	}
	
	@Override
	public final void init(ServletConfig servletConfig) throws ServletException {
		httpServerApplication = new HttpServerApplication(new ServletConfigFactory(servletConfig));
		httpServerApplication.getBeanFactory().registerNameMapping(servletConfig.getServletName(), this.getClass().getName());
		try {
			httpServerApplication.getBeanFactory().registerSingleton(this.getClass(), this);
		} catch (Exception e) {
			throw new ServletException(e);
		}
		
		super.init(servletConfig);
		httpServerApplication.init();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		httpServerApplication.destroy();
	}
	
	public final String getConfig(String key) {
		return getConfig(key, null);
	}

	public final String getConfig(String key, String defaultValue) {
		String value = getServletConfig().getInitParameter(key);
		value = value == null ? defaultValue : value;
		return value == null ? value : ConfigUtils.format(value);
	}
	
	/**
	 * request封装
	 * 
	 * @param httpServletRequest
	 * @return
	 * @throws IOException
	 */
	public Request wrapperRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
		if(httpServletRequest.getContentType() == null || httpServletRequest.getContentType().startsWith(ContentType.FORM.getValue())){
			return formRequestWrapper(httpServletRequest, httpServletResponse);
		}else if(httpServletRequest.getContentType().startsWith(ContentType.JSON.getValue())){
			return jsonRequestWrapper(httpServletRequest, httpServletResponse);
		}else{
			return formRequestWrapper(httpServletRequest, httpServletResponse);
		} 
	}
	
	protected Request formRequestWrapper(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException{
		return new FormRequest(httpServerApplication.getBeanFactory(), httpServletRequest, httpServletResponse, httpServerApplication.isDebug(), false);
	}
	
	protected Request jsonRequestWrapper(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException{
		return new JsonRequest(httpServerApplication.getBeanFactory(), httpServletRequest, httpServletResponse, httpServerApplication.isDebug());
	}
	
	public void controller(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws IOException, ServletException {
		Request request = wrapperRequest(httpServletRequest, httpServletResponse);
		try {
			if(!httpServerApplication.service(request, new Response(request, httpServletResponse))){
				if(!httpServletResponse.isCommitted()){
					httpServletResponse.sendError(404, request.getServletPath());
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			if(!httpServletResponse.isCommitted()){
				httpServletResponse.sendError(500, request.getServletPath());
			}
		}
	}
}
