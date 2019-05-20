package scw.servlet.http;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import scw.core.logger.Logger;
import scw.core.logger.LoggerFactory;
import scw.json.JSONObject;
import scw.json.JSONParseSupport;
import scw.servlet.beans.RequestBeanFactory;
import scw.servlet.parameter.Body;

public class JSONHttpRequest extends DefaultHttpRequest {
	private static Logger logger = LoggerFactory.getLogger(JSONHttpRequest.class);
	private JSONObject json;

	public JSONHttpRequest(RequestBeanFactory requestBeanFactory,
			HttpServletRequest httpServletRequest, boolean cookieValue, JSONParseSupport jsonParseSupport, boolean debug) throws IOException {
		super(requestBeanFactory, httpServletRequest, cookieValue);
		Body body = getBean(Body.class);
		if (debug) {
			logger.debug("servletPath=" + getServletPath() + ",method=" + getMethod() + "," + body.getBody());
		}
		json = jsonParseSupport.parseObject(body.getBody());
	}

	@Override
	public String getParameter(String name) {
		String v = json.getString(name);
		if (v == null) {
			v = super.getParameter(name);
		}
		return v;
	}
}
