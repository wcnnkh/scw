package scw.servlet.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.http.AbstractHttpInputMessage;
import scw.http.HttpCookie;
import scw.http.HttpHeaders;
import scw.http.HttpMethod;
import scw.http.HttpUtils;
import scw.http.InvalidMediaTypeException;
import scw.http.MediaType;
import scw.http.server.ServerHttpAsyncControl;
import scw.http.server.ServerHttpRequest;
import scw.http.server.ServerHttpResponse;
import scw.json.JSONUtils;
import scw.net.InetAddress;
import scw.net.InetUtils;
import scw.net.RestfulParameterMapAware;
import scw.security.session.Session;
import scw.util.LinkedCaseInsensitiveMap;
import scw.util.MultiValueMap;

public class ServletServerHttpRequest extends AbstractHttpInputMessage
		implements ServerHttpRequest, RestfulParameterMapAware {
	private HttpHeaders headers;
	private HttpServletRequest httpServletRequest;
	private HttpServletAsyncControl asyncControl;
	private MultiValueMap<String, String> parameterMap;
	private MultiValueMap<String, String> restfulParameterMap;

	public ServletServerHttpRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public String getPath() {
		return httpServletRequest.getServletPath();
	}

	public HttpCookie[] getCookies() {
		javax.servlet.http.Cookie[] cookies = httpServletRequest.getCookies();
		if (cookies == null) {
			return new HttpCookie[0];
		}

		HttpCookie[] values = new HttpCookie[cookies.length];
		for (int i = 0; i < cookies.length; i++) {
			values[i] = new ServletHttpCookie(cookies[i]);
		}
		return values;
	}

	public Session getSession() {
		HttpSession session = httpServletRequest.getSession();
		return session == null ? null : new ServletHttpSession(session);
	}

	public Session getSession(boolean create) {
		HttpSession httpSession = httpServletRequest.getSession(create);
		return new ServletHttpSession(httpSession);
	}

	public Principal getPrincipal() {
		return httpServletRequest.getUserPrincipal();
	}

	public InetAddress getLocalAddress() {

		return new InetAddress.DefaultInetAddress(this.httpServletRequest.getLocalName(),
				this.httpServletRequest.getLocalPort());
	}

	public InetAddress getRemoteAddress() {
		return new InetAddress.DefaultInetAddress(this.httpServletRequest.getRemoteHost(),
				this.httpServletRequest.getRemotePort());
	}

	public void setCharacterEncoding(String enc) {
		try {
			httpServletRequest.setCharacterEncoding(enc);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public HttpHeaders getHeaders() {
		if (this.headers == null) {
			this.headers = new HttpHeaders();

			for (Enumeration<?> names = httpServletRequest.getHeaderNames(); names.hasMoreElements();) {
				String headerName = (String) names.nextElement();
				for (Enumeration<?> headerValues = httpServletRequest.getHeaders(headerName); headerValues
						.hasMoreElements();) {
					String headerValue = (String) headerValues.nextElement();
					this.headers.add(headerName, headerValue);
				}
			}

			// HttpServletRequest exposes some headers as properties:
			// we should include those if not already present
			try {
				MediaType contentType = this.headers.getContentType();
				if (contentType == null) {
					String requestContentType = httpServletRequest.getContentType();
					if (StringUtils.hasLength(requestContentType)) {
						contentType = MediaType.parseMediaType(requestContentType);
						this.headers.setContentType(contentType);
					}
				}
				if (contentType != null && contentType.getCharset() == null) {
					String requestEncoding = getCharacterEncoding();
					if (StringUtils.hasLength(requestEncoding)) {
						Charset charSet = Charset.forName(requestEncoding);
						Map<String, String> params = new LinkedCaseInsensitiveMap<String>();
						params.putAll(contentType.getParameters());
						params.put("charset", charSet.toString());
						MediaType mediaType = new MediaType(contentType.getType(), contentType.getSubtype(), params);
						this.headers.setContentType(mediaType);
					}
				}
			} catch (InvalidMediaTypeException ex) {
				// Ignore: simply not exposing an invalid content type in
				// HttpHeaders...
			}

			if (this.headers.getContentLength() < 0) {
				int requestContentLength = httpServletRequest.getContentLength();
				if (requestContentLength != -1) {
					this.headers.setContentLength(requestContentLength);
				}
			}
		}
		return this.headers;
	}

	public HttpMethod getMethod() {
		return HttpMethod.resolve(getRawMethod());
	}

	public MediaType getContentType() {
		String contentType = httpServletRequest.getContentType();
		return contentType == null ? null : MediaType.parseMediaType(contentType);
	}

	public URI getURI() {
		return InetUtils.toURI(httpServletRequest.getRequestURI());
	}

	public long getContentLength() {
		return httpServletRequest.getContentLength();
	}

	public String getCharacterEncoding() {
		return httpServletRequest.getCharacterEncoding();
	}

	public BufferedReader getReader() throws IOException {
		return httpServletRequest.getReader();
	}

	public String getRemoteAddr() {
		return httpServletRequest.getRemoteAddr();
	}

	public InputStream getBody() throws IOException {
		return httpServletRequest.getInputStream();
	}

	private void initParameterMap() {
		if (parameterMap != null) {
			return;
		}

		Map<String, String[]> map = httpServletRequest.getParameterMap();
		if (map.isEmpty()) {
			this.parameterMap = CollectionUtils.emptyMultiValueMap();
			return;
		}

		Map<String, List<String>> valueMap = new HashMap<String, List<String>>();
		for (Entry<String, String[]> entry : map.entrySet()) {
			String[] values = entry.getValue();
			if (values == null || values.length == 0) {
				continue;
			}

			valueMap.put(entry.getKey(), Arrays.asList(values));
		}

		this.parameterMap = CollectionUtils.toMultiValueMap(Collections.unmodifiableMap(valueMap));
	}

	public MultiValueMap<String, String> getParameterMap() {
		initParameterMap();
		return parameterMap;
	}

	public String getRawMethod() {
		return httpServletRequest.getMethod();
	}

	public String getRemoteHost() {
		return httpServletRequest.getRemoteHost();
	}

	public String getContextPath() {
		return httpServletRequest.getContextPath();
	}

	public String getController() {
		return httpServletRequest.getServletPath();
	}

	@Override
	public String getRawContentType() {
		return httpServletRequest.getContentType();
	}

	public void setAttribute(String name, Object o) {
		httpServletRequest.setAttribute(name, o);
	}

	public void removeAttribute(String name) {
		httpServletRequest.removeAttribute(name);
	}

	public Object getAttribute(String name) {
		return httpServletRequest.getAttribute(name);
	}

	public Enumeration<String> getAttributeNames() {
		return httpServletRequest.getAttributeNames();
	}

	public boolean isSupportAsyncControl() {
		return httpServletRequest.isAsyncSupported();
	}

	public ServerHttpAsyncControl getAsyncControl(ServerHttpResponse response) {
		if (asyncControl == null) {
			if (response instanceof ServletServerHttpResponse) {
				this.asyncControl = new HttpServletAsyncControl(httpServletRequest,
						((ServletServerHttpResponse) response).getHttpServletResponse());
			} else {
				throw new IllegalArgumentException(
						"Response must be a ServletServerHttpResponse: " + response.getClass());
			}
		}
		return this.asyncControl;
	}

	public String getIp() {
		return HttpUtils.getServerHttpRequestIpGetter().getRequestIp(this);
	}

	public MultiValueMap<String, String> getRestfulParameterMap() {
		if (restfulParameterMap == null) {
			return CollectionUtils.emptyMultiValueMap();
		}

		return restfulParameterMap;
	}

	public void setRestfulParameterMap(MultiValueMap<String, String> restfulParameterMap) {
		this.restfulParameterMap = CollectionUtils.unmodifiableMultiValueMap(restfulParameterMap);
	}

	@Override
	public String toString() {
		return getRawMethod() + " " + getPath() + " " + httpServletRequest.getProtocol() + " parameters->" + JSONUtils.getJsonSupport().toJSONString(httpServletRequest.getParameterMap());
	}
}
