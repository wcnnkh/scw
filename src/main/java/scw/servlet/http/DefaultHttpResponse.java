package scw.servlet.http;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import scw.core.logger.DebugLogger;
import scw.core.logger.Logger;
import scw.core.logger.LoggerFactory;
import scw.core.net.http.ContentType;
import scw.core.utils.ClassUtils;
import scw.json.JSONParseSupport;
import scw.servlet.View;

public class DefaultHttpResponse extends HttpServletResponseWrapper implements HttpResponse, DebugLogger {
	private static Logger logger = LoggerFactory.getLogger(DefaultHttpResponse.class);
	private static final String JSONP_CALLBACK = "callback";
	private static final String JSONP_RESP_PREFIX = "(";
	private static final String JSONP_RESP_SUFFIX = ");";
	private HttpRequest httpRequest;
	private JSONParseSupport jsonParseSupport;
	private boolean debug;

	public DefaultHttpResponse(JSONParseSupport jsonParseSupport, HttpRequest httpRequest,
			HttpServletResponse httpServletResponse, boolean debug) {
		super(httpServletResponse);
		this.jsonParseSupport = jsonParseSupport;
		this.httpRequest = httpRequest;
		this.debug = debug;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public void write(Object obj) throws Exception {
		if (obj != null) {
			if (obj instanceof View) {
				((View) obj).render(httpRequest, this);
			} else {
				String content;
				if ((obj instanceof String) || (ClassUtils.isPrimitiveOrWrapper(obj.getClass()))) {
					content = obj.toString();
				} else {
					content = jsonParseSupport.toJSONString(obj);
				}

				String callback = null;
				try {
					callback = httpRequest.getParameter(String.class, JSONP_CALLBACK);
				} catch (Throwable e) {
					e.printStackTrace();
				}

				if (callback != null && callback.length() != 0) {
					setContentType(ContentType.TEXT_JAVASCRIPT);
					StringBuilder sb = new StringBuilder();
					sb.append(callback);
					sb.append(JSONP_RESP_PREFIX);
					sb.append(content);
					sb.append(JSONP_RESP_SUFFIX);
					content = sb.toString();
				} else {
					if (getContentType() == null) {
						setContentType(ContentType.TEXT_HTML);
					}
				}

				debug(content);
				getWriter().write(content);
			}
		}
	}

	public boolean isDebugEnabled() {
		return debug;
	}

	public void debug(String msg) {
		if (debug) {
			logger.debug(msg);
		}

	}

	public void debug(String format, Object... args) {
		if (debug) {
			logger.debug(format, args);
		}
	}

	public void debug(String msg, Throwable t) {
		if (debug) {
			logger.debug(msg, t);
		}
	}
}
