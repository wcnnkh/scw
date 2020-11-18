package scw.servlet.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import scw.http.AbstractHttpOutputMessage;
import scw.http.HttpCookie;
import scw.http.HttpHeaders;
import scw.http.HttpStatus;
import scw.http.server.ServerHttpResponse;
import scw.util.Target;
import scw.util.XUtils;

public class ServletServerHttpResponse extends AbstractHttpOutputMessage implements ServerHttpResponse, Target {
	private HttpServletResponse httpServletResponse;
	private HttpServletResponseHeaders headers;
	private boolean bodyUse = false;

	public ServletServerHttpResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
		this.headers = new HttpServletResponseHeaders(httpServletResponse);
	}

	public <T> T getTarget(Class<T> targetType) {
		return XUtils.getTarget(httpServletResponse, targetType);
	}

	public OutputStream getBody() throws IOException {
		bodyUse = true;
		headers.write();
		return httpServletResponse.getOutputStream();
	}

	public PrintWriter getWriter() throws IOException {
		bodyUse = true;
		headers.write();
		return httpServletResponse.getWriter();
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public boolean isCommitted() {
		return httpServletResponse.isCommitted();
	}

	public void sendError(int sc, String msg) throws IOException {
		try {
			httpServletResponse.sendError(sc, msg);
		} catch (IllegalStateException ex) {
			// Possibly on Tomcat when called too late: fall back to silent
			// setStatus
			httpServletResponse.setStatus(sc);
		}
	}

	public void addCookie(HttpCookie cookie) {
		javax.servlet.http.Cookie c = new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
		if (cookie.getMaxAge() >= 0) {
			c.setMaxAge(cookie.getMaxAge());
		}

		if (cookie.getPath() != null) {
			c.setPath(cookie.getPath());
		}

		if (cookie.getDomain() != null) {
			c.setDomain(cookie.getDomain());
		}
		httpServletResponse.addCookie(c);
	}

	public void addCookie(String name, String value) {
		httpServletResponse.addCookie(new javax.servlet.http.Cookie(name, value));
	}

	public void sendError(int sc) throws IOException {
		httpServletResponse.sendError(sc);
	}

	public void sendRedirect(String location) throws IOException {
		httpServletResponse.sendRedirect(location);
	}

	public void setStatus(int sc) {
		httpServletResponse.setStatus(sc);
	}

	public void setStatusCode(HttpStatus httpStatus) {
		httpServletResponse.setStatus(httpStatus.value());
	}

	public int getStatus() {
		return httpServletResponse.getStatus();
	}

	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}

	public void flush() throws IOException {
		headers.write();
		if (bodyUse) {
			httpServletResponse.flushBuffer();
		}
	}

	public void close() throws IOException {
		flush();
	}
}
