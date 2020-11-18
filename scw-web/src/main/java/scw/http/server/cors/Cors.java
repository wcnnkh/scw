package scw.http.server.cors;

import java.util.Arrays;

import scw.core.utils.ArrayUtils;
import scw.core.utils.StringUtils;
import scw.http.HttpHeaders;
import scw.http.server.ServerHttpRequest;
import scw.lang.NotSupportedException;
import scw.mapper.MapperUtils;

public class Cors {
	private static final String[] DEFAULT_HEADERS = new String[] { HttpHeaders.X_REQUESTED_WITH,
			HttpHeaders.CONTENT_TYPE, HttpHeaders.X_FORWARDED_FOR, HttpHeaders.COOKIE,
			HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCEPT,
			HttpHeaders.ORIGIN };
	public static final Cors DEFAULT = new Cors().readyOnly();
	public static final Cors EMPTY = new Cors().readyOnly();
	
	private static final String[] EMPTY_ARRAY = new String[0];

	/**
	 * 允许跨域的请求头
	 */
	private String[] headers;
	/**
	 * 允许跨域的方法
	 */
	private String[] methods;
	/**
	 * 允许跨域的主机地址
	 */
	private String[] origins;
	/**
	 * 是否携带cookie
	 */
	private Boolean credentials;
	/**
	 * 重新预检验跨域的缓存时间 (s)
	 */
	private Integer maxAge;

	private boolean readyOnly = false;

	public Cors() {
		this(DEFAULT_HEADERS, null, null, null, null);
	};

	public Cors(String[] headers, String[] methods, String[] origins, Boolean credentials, Integer maxAge) {
		this.headers = headers;
		this.methods = methods;
		this.origins = origins;
		this.credentials = credentials;
		this.maxAge = maxAge;
	}

	public String[] getHeaders() {
		return headers == null ? EMPTY_ARRAY : headers.clone();
	}

	public Cors setHeaders(String... headers) {
		if (isReadyOnly()) {
			throw new NotSupportedException("setHeaders");
		}

		this.headers = headers;
		return this;
	}

	public String[] getMethods() {
		return methods == null ? EMPTY_ARRAY : headers.clone();
	}

	public Cors setMethods(String... methods) {
		if (isReadyOnly()) {
			throw new NotSupportedException("setMethods");
		}

		this.methods = methods;
		return this;
	}

	public String[] getOrigins() {
		return origins == null ? EMPTY_ARRAY : origins.clone();
	}

	public void setOrigins(String... origins) {
		if (isReadyOnly()) {
			throw new NotSupportedException("setOrigins");
		}

		this.origins = origins;
	}

	public Boolean getCredentials() {
		return credentials;
	}

	public Cors setCredentials(Boolean credentials) {
		if (isReadyOnly()) {
			throw new NotSupportedException("setCredentials");
		}

		this.credentials = credentials;
		return this;
	}

	public Integer getMaxAge() {
		return maxAge;
	}

	public Cors setMaxAge(Integer maxAge) {
		if (isReadyOnly()) {
			throw new NotSupportedException("setMaxAge");
		}

		this.maxAge = maxAge;
		return this;
	}

	public boolean isReadyOnly() {
		return readyOnly;
	}

	public Cors readyOnly() {
		this.readyOnly = true;
		return this;
	}

	@Override
	public Cors clone() {
		return new Cors(ArrayUtils.isEmpty(headers) ? null : headers.clone(),
				ArrayUtils.isEmpty(methods) ? null : methods.clone(),
				ArrayUtils.isEmpty(origins) ? null : origins.clone(), credentials, maxAge);
	}

	protected void writeValues(HttpHeaders headers, String headerName, String[] values, String defaultValue) {
		if (ArrayUtils.isEmpty(values)) {
			headers.set(headerName, defaultValue);
		} else {
			headers.add(headerName, StringUtils.collectionToCommaDelimitedString(Arrays.asList(values)));
		}
	}

	public void write(ServerHttpRequest request, HttpHeaders headers) {
		String origin = request.getHeaders().getOrigin();
		if(StringUtils.isEmpty(origin)){
			return ;
		}
		
		writeValues(headers, HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, this.origins, origin);
		writeValues(headers, HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, this.methods, "*");
		writeValues(headers, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, this.headers, "*");
		if (maxAge != null) {
			headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, maxAge + "");
		}

		if (credentials != null) {
			headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, credentials + "");
		}
	}

	@Override
	public String toString() {
		return MapperUtils.getMapper().toString(this);
	}
}
