package scw.http;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scw.core.Assert;
import scw.core.GlobalPropertyFactory;
import scw.core.utils.ArrayUtils;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.lang.Nullable;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.net.InetUtils;
import scw.net.MimeType;
import scw.net.MimeTypeUtils;
import scw.net.message.Headers;
import scw.util.DefaultStringMatcher;
import scw.value.Value;
import scw.value.property.DynamicProperties;
import scw.value.property.DynamicProperties.ValueCreator;

/**
 * A data structure representing HTTP request or response headers, mapping
 * String header names to a list of String values, also offering accessors for
 * common application-level data types.
 *
 * <p>
 * In addition to the regular methods defined by {@link Map}, this class offers
 * many common convenience methods, for example:
 * <ul>
 * <li>{@link #getFirst(String)} returns the first value associated with a given
 * header name</li>
 * <li>{@link #add(String, String)} adds a header value to the list of values
 * for a header name</li>
 * <li>{@link #set(String, String)} sets the header value to a single string
 * value</li>
 * </ul>
 *
 * <p>
 * Note that {@code HttpHeaders} generally treats header names in a
 * case-insensitive manner.
 *
 */
public class HttpHeaders extends Headers {
	private static Logger logger = LoggerUtils.getLogger(HttpHeaders.class);
	private static final long serialVersionUID = -8578554704772377436L;

	/**
	 * The HTTP {@code Accept} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">Section
	 *      5.3.2 of RFC 7231</a>
	 */
	public static final String ACCEPT = "Accept";
	/**
	 * The HTTP {@code Accept-Charset} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.3">Section
	 *      5.3.3 of RFC 7231</a>
	 */
	public static final String ACCEPT_CHARSET = "Accept-Charset";
	/**
	 * The HTTP {@code Accept-Encoding} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.4">Section
	 *      5.3.4 of RFC 7231</a>
	 */
	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	/**
	 * The HTTP {@code Accept-Language} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.5">Section
	 *      5.3.5 of RFC 7231</a>
	 */
	public static final String ACCEPT_LANGUAGE = "Accept-Language";
	/**
	 * The HTTP {@code Accept-Ranges} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-2.3">Section
	 *      5.3.5 of RFC 7233</a>
	 */
	public static final String ACCEPT_RANGES = "Accept-Ranges";
	/**
	 * The CORS {@code Access-Control-Allow-Credentials} response header field
	 * name.
	 * 
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
	/**
	 * The CORS {@code Access-Control-Allow-Headers} response header field name.
	 * 
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	/**
	 * The CORS {@code Access-Control-Allow-Methods} response header field name.
	 * 
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
	/**
	 * The CORS {@code Access-Control-Allow-Origin} response header field name.
	 * 
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	/**
	 * The CORS {@code Access-Control-Expose-Headers} response header field
	 * name.
	 * 
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
	/**
	 * The CORS {@code Access-Control-Max-Age} response header field name.
	 * 
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
	/**
	 * The CORS {@code Access-Control-Request-Headers} request header field
	 * name.
	 * 
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
	/**
	 * The CORS {@code Access-Control-Request-Method} request header field name.
	 * 
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
	/**
	 * The HTTP {@code Age} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.1">Section
	 *      5.1 of RFC 7234</a>
	 */
	public static final String AGE = "Age";
	/**
	 * The HTTP {@code Allow} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.1">Section
	 *      7.4.1 of RFC 7231</a>
	 */
	public static final String ALLOW = "Allow";
	/**
	 * The HTTP {@code Authorization} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.2">Section
	 *      4.2 of RFC 7235</a>
	 */
	public static final String AUTHORIZATION = "Authorization";
	/**
	 * The HTTP {@code Cache-Control} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.2">Section
	 *      5.2 of RFC 7234</a>
	 */
	public static final String CACHE_CONTROL = "Cache-Control";
	/**
	 * The HTTP {@code Connection} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.1">Section
	 *      6.1 of RFC 7230</a>
	 */
	public static final String CONNECTION = "Connection";
	/**
	 * The HTTP {@code Content-Encoding} header field name.
	 * 
	 * @see <a href=
	 *      "https://tools.ietf.org/html/rfc7231#section-3.1.2.2">Section
	 *      3.1.2.2 of RFC 7231</a>
	 */
	public static final String CONTENT_ENCODING = "Content-Encoding";
	/**
	 * The HTTP {@code Content-Disposition} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc6266">RFC 6266</a>
	 */
	public static final String CONTENT_DISPOSITION = "Content-Disposition";
	/**
	 * The HTTP {@code Content-Language} header field name.
	 * 
	 * @see <a href=
	 *      "https://tools.ietf.org/html/rfc7231#section-3.1.3.2">Section
	 *      3.1.3.2 of RFC 7231</a>
	 */
	public static final String CONTENT_LANGUAGE = "Content-Language";
	/**
	 * The HTTP {@code Content-Length} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.2">Section
	 *      3.3.2 of RFC 7230</a>
	 */
	public static final String CONTENT_LENGTH = "Content-Length";
	/**
	 * The HTTP {@code Content-Location} header field name.
	 * 
	 * @see <a href=
	 *      "https://tools.ietf.org/html/rfc7231#section-3.1.4.2">Section
	 *      3.1.4.2 of RFC 7231</a>
	 */
	public static final String CONTENT_LOCATION = "Content-Location";
	/**
	 * The HTTP {@code Content-Range} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.2">Section
	 *      4.2 of RFC 7233</a>
	 */
	public static final String CONTENT_RANGE = "Content-Range";
	/**
	 * The HTTP {@code Content-Type} header field name.
	 * 
	 * @see <a href=
	 *      "https://tools.ietf.org/html/rfc7231#section-3.1.1.5">Section
	 *      3.1.1.5 of RFC 7231</a>
	 */
	public static final String CONTENT_TYPE = "Content-Type";
	/**
	 * The HTTP {@code Cookie} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.3.4">Section
	 *      4.3.4 of RFC 2109</a>
	 */
	public static final String COOKIE = "Cookie";
	/**
	 * The HTTP {@code Date} header field name.
	 * 
	 * @see <a href=
	 *      "https://tools.ietf.org/html/rfc7231#section-7.1.1.2">Section
	 *      7.1.1.2 of RFC 7231</a>
	 */
	public static final String DATE = "Date";
	/**
	 * The HTTP {@code ETag} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section
	 *      2.3 of RFC 7232</a>
	 */
	public static final String ETAG = "ETag";
	/**
	 * The HTTP {@code Expect} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.1">Section
	 *      5.1.1 of RFC 7231</a>
	 */
	public static final String EXPECT = "Expect";
	/**
	 * The HTTP {@code Expires} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.3">Section
	 *      5.3 of RFC 7234</a>
	 */
	public static final String EXPIRES = "Expires";
	/**
	 * The HTTP {@code From} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.1">Section
	 *      5.5.1 of RFC 7231</a>
	 */
	public static final String FROM = "From";
	/**
	 * The HTTP {@code Host} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.4">Section
	 *      5.4 of RFC 7230</a>
	 */
	public static final String HOST = "Host";
	/**
	 * The HTTP {@code If-Match} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.1">Section
	 *      3.1 of RFC 7232</a>
	 */
	public static final String IF_MATCH = "If-Match";
	/**
	 * The HTTP {@code If-Modified-Since} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.3">Section
	 *      3.3 of RFC 7232</a>
	 */
	public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
	/**
	 * The HTTP {@code If-None-Match} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.2">Section
	 *      3.2 of RFC 7232</a>
	 */
	public static final String IF_NONE_MATCH = "If-None-Match";
	/**
	 * The HTTP {@code If-Range} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.2">Section
	 *      3.2 of RFC 7233</a>
	 */
	public static final String IF_RANGE = "If-Range";
	/**
	 * The HTTP {@code If-Unmodified-Since} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.4">Section
	 *      3.4 of RFC 7232</a>
	 */
	public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
	/**
	 * The HTTP {@code Last-Modified} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.2">Section
	 *      2.2 of RFC 7232</a>
	 */
	public static final String LAST_MODIFIED = "Last-Modified";
	/**
	 * The HTTP {@code Link} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc5988">RFC 5988</a>
	 */
	public static final String LINK = "Link";
	/**
	 * The HTTP {@code Location} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.2">Section
	 *      7.1.2 of RFC 7231</a>
	 */
	public static final String LOCATION = "Location";
	/**
	 * The HTTP {@code Max-Forwards} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.2">Section
	 *      5.1.2 of RFC 7231</a>
	 */
	public static final String MAX_FORWARDS = "Max-Forwards";
	/**
	 * The HTTP {@code Origin} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc6454">RFC 6454</a>
	 */
	public static final String ORIGIN = "Origin";
	/**
	 * The HTTP {@code Pragma} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.4">Section
	 *      5.4 of RFC 7234</a>
	 */
	public static final String PRAGMA = "Pragma";
	/**
	 * The HTTP {@code Proxy-Authenticate} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.3">Section
	 *      4.3 of RFC 7235</a>
	 */
	public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
	/**
	 * The HTTP {@code Proxy-Authorization} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.4">Section
	 *      4.4 of RFC 7235</a>
	 */
	public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
	/**
	 * The HTTP {@code Range} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.1">Section
	 *      3.1 of RFC 7233</a>
	 */
	public static final String RANGE = "Range";
	/**
	 * The HTTP {@code Referer} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.2">Section
	 *      5.5.2 of RFC 7231</a>
	 */
	public static final String REFERER = "Referer";
	/**
	 * The HTTP {@code Retry-After} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.3">Section
	 *      7.1.3 of RFC 7231</a>
	 */
	public static final String RETRY_AFTER = "Retry-After";
	/**
	 * The HTTP {@code Server} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.2">Section
	 *      7.4.2 of RFC 7231</a>
	 */
	public static final String SERVER = "Server";
	/**
	 * The HTTP {@code Set-Cookie} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.2.2">Section
	 *      4.2.2 of RFC 2109</a>
	 */
	public static final String SET_COOKIE = "Set-Cookie";
	/**
	 * The HTTP {@code Set-Cookie2} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc2965">RFC 2965</a>
	 */
	public static final String SET_COOKIE2 = "Set-Cookie2";
	/**
	 * The HTTP {@code TE} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.3">Section
	 *      4.3 of RFC 7230</a>
	 */
	public static final String TE = "TE";
	/**
	 * The HTTP {@code Trailer} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.4">Section
	 *      4.4 of RFC 7230</a>
	 */
	public static final String TRAILER = "Trailer";
	/**
	 * The HTTP {@code Transfer-Encoding} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.1">Section
	 *      3.3.1 of RFC 7230</a>
	 */
	public static final String TRANSFER_ENCODING = "Transfer-Encoding";
	/**
	 * The HTTP {@code Upgrade} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.7">Section
	 *      6.7 of RFC 7230</a>
	 */
	public static final String UPGRADE = "Upgrade";
	/**
	 * The HTTP {@code User-Agent} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.3">Section
	 *      5.5.3 of RFC 7231</a>
	 */
	public static final String USER_AGENT = "User-Agent";
	/**
	 * The HTTP {@code Vary} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.4">Section
	 *      7.1.4 of RFC 7231</a>
	 */
	public static final String VARY = "Vary";
	/**
	 * The HTTP {@code Via} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.7.1">Section
	 *      5.7.1 of RFC 7230</a>
	 */
	public static final String VIA = "Via";
	/**
	 * The HTTP {@code Warning} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.5">Section
	 *      5.5 of RFC 7234</a>
	 */
	public static final String WARNING = "Warning";
	/**
	 * The HTTP {@code WWW-Authenticate} header field name.
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.1">Section
	 *      4.1 of RFC 7235</a>
	 */
	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	/**
	 * Pattern matching ETag multiple field values in headers such as
	 * "If-Match", "If-None-Match".
	 * 
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section
	 *      2.3 of RFC 7232</a>
	 */
	private static final Pattern ETAG_HEADER_VALUE_PATTERN = Pattern.compile("\\*|\\s*((W\\/)?(\"[^\"]*\"))\\s*,?");

	private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

	/**
	 * Date formats as specified in the HTTP RFC.
	 * 
	 * @see <a href=
	 *      "https://tools.ietf.org/html/rfc7231#section-7.1.1.1">Section
	 *      7.1.1.1 of RFC 7231</a>
	 */
	private static final String[] DATE_FORMATS = new String[] { "EEE, dd MMM yyyy HH:mm:ss zzz",
			"EEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM dd HH:mm:ss yyyy" };

	public static final String X_REQUESTED_WITH = "X-Requested-With";

	public static final String X_REAL_IP = "X-Real-Ip";

	public static final String X_FORWARDED_FOR = "X-Forwarded-For";

	private static final DynamicProperties AJAX_HEADERS = new DynamicProperties(false);

	static {
		AJAX_HEADERS.loadProperties("/scw/net/headers/ajax.headers.properties", ValueCreator.CREATOR);
		AJAX_HEADERS.loadProperties(GlobalPropertyFactory.getInstance().getValue("scw.net.ajax.headers", String.class,
				"/ajax-headers.properties"), ValueCreator.CREATOR).register();
	}

	public HttpHeaders() {
		super(false);
	}

	public HttpHeaders(Map<String, List<String>> wrapperHeaders) {
		super(wrapperHeaders, false);
	}

	/**
	 * Set the list of acceptable {@linkplain MediaType media types}, as
	 * specified by the {@code Accept} header.
	 */
	public void setAccept(List<MediaType> acceptableMediaTypes) {
		set(ACCEPT, MediaType.toString(acceptableMediaTypes));
	}

	/**
	 * Return the list of acceptable {@linkplain MediaType media types}, as
	 * specified by the {@code Accept} header.
	 * <p>
	 * Returns an empty list when the acceptable media types are unspecified.
	 */
	public List<MediaType> getAccept() {
		return MediaType.parseMediaTypes(get(ACCEPT));
	}

	/**
	 * Set the (new) value of the {@code Access-Control-Allow-Credentials}
	 * response header.
	 */
	public void setAccessControlAllowCredentials(boolean allowCredentials) {
		set(ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.toString(allowCredentials));
	}

	/**
	 * Return the value of the {@code Access-Control-Allow-Credentials} response
	 * header.
	 */
	public boolean getAccessControlAllowCredentials() {
		return Boolean.parseBoolean(getFirst(ACCESS_CONTROL_ALLOW_CREDENTIALS));
	}

	/**
	 * Set the (new) value of the {@code Access-Control-Allow-Headers} response
	 * header.
	 */
	public void setAccessControlAllowHeaders(List<String> allowedHeaders) {
		set(ACCESS_CONTROL_ALLOW_HEADERS, toCommaDelimitedString(allowedHeaders));
	}

	/**
	 * Return the value of the {@code Access-Control-Allow-Headers} response
	 * header.
	 */
	public List<String> getAccessControlAllowHeaders() {
		return getValuesAsList(ACCESS_CONTROL_ALLOW_HEADERS);
	}

	/**
	 * Set the (new) value of the {@code Access-Control-Allow-Methods} response
	 * header.
	 */
	public void setAccessControlAllowMethods(List<HttpMethod> allowedMethods) {
		set(ACCESS_CONTROL_ALLOW_METHODS, StringUtils.collectionToCommaDelimitedString(allowedMethods));
	}

	/**
	 * Return the value of the {@code Access-Control-Allow-Methods} response
	 * header.
	 */
	public List<HttpMethod> getAccessControlAllowMethods() {
		List<HttpMethod> result = new ArrayList<HttpMethod>();
		String value = getFirst(ACCESS_CONTROL_ALLOW_METHODS);
		if (value != null) {
			String[] tokens = StringUtils.tokenizeToStringArray(value, ",");
			for (String token : tokens) {
				HttpMethod resolved = HttpMethod.resolve(token);
				if (resolved != null) {
					result.add(resolved);
				}
			}
		}
		return result;
	}

	/**
	 * Set the (new) value of the {@code Access-Control-Allow-Origin} response
	 * header.
	 */
	public void setAccessControlAllowOrigin(String allowedOrigin) {
		set(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
	}

	/**
	 * Return the value of the {@code Access-Control-Allow-Origin} response
	 * header.
	 */
	public String getAccessControlAllowOrigin() {
		return getFieldValues(ACCESS_CONTROL_ALLOW_ORIGIN);
	}

	/**
	 * Set the (new) value of the {@code Access-Control-Expose-Headers} response
	 * header.
	 */
	public void setAccessControlExposeHeaders(List<String> exposedHeaders) {
		set(ACCESS_CONTROL_EXPOSE_HEADERS, toCommaDelimitedString(exposedHeaders));
	}

	/**
	 * Return the value of the {@code Access-Control-Expose-Headers} response
	 * header.
	 */
	public List<String> getAccessControlExposeHeaders() {
		return getValuesAsList(ACCESS_CONTROL_EXPOSE_HEADERS);
	}

	/**
	 * Set the (new) value of the {@code Access-Control-Max-Age} response
	 * header.
	 */
	public void setAccessControlMaxAge(long maxAge) {
		set(ACCESS_CONTROL_MAX_AGE, Long.toString(maxAge));
	}

	/**
	 * Return the value of the {@code Access-Control-Max-Age} response header.
	 * <p>
	 * Returns -1 when the max age is unknown.
	 */
	public long getAccessControlMaxAge() {
		String value = getFirst(ACCESS_CONTROL_MAX_AGE);
		return (value != null ? Long.parseLong(value) : -1);
	}

	/**
	 * Set the (new) value of the {@code Access-Control-Request-Headers} request
	 * header.
	 */
	public void setAccessControlRequestHeaders(List<String> requestHeaders) {
		set(ACCESS_CONTROL_REQUEST_HEADERS, toCommaDelimitedString(requestHeaders));
	}

	/**
	 * Return the value of the {@code Access-Control-Request-Headers} request
	 * header.
	 */
	public List<String> getAccessControlRequestHeaders() {
		return getValuesAsList(ACCESS_CONTROL_REQUEST_HEADERS);
	}

	/**
	 * Set the (new) value of the {@code Access-Control-Request-Method} request
	 * header.
	 */
	public void setAccessControlRequestMethod(HttpMethod requestMethod) {
		set(ACCESS_CONTROL_REQUEST_METHOD, requestMethod.name());
	}

	/**
	 * Return the value of the {@code Access-Control-Request-Method} request
	 * header.
	 */
	public HttpMethod getAccessControlRequestMethod() {
		return HttpMethod.resolve(getFirst(ACCESS_CONTROL_REQUEST_METHOD));
	}

	/**
	 * Set the list of acceptable {@linkplain Charset charsets}, as specified by
	 * the {@code Accept-Charset} header.
	 */
	public void setAcceptCharset(List<Charset> acceptableCharsets) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<Charset> iterator = acceptableCharsets.iterator(); iterator.hasNext();) {
			Charset charset = iterator.next();
			builder.append(charset.name().toLowerCase(Locale.ENGLISH));
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		set(ACCEPT_CHARSET, builder.toString());
	}

	/**
	 * Return the list of acceptable {@linkplain Charset charsets}, as specified
	 * by the {@code Accept-Charset} header.
	 */
	public List<Charset> getAcceptCharset() {
		String value = getFirst(ACCEPT_CHARSET);
		if (value != null) {
			String[] tokens = StringUtils.tokenizeToStringArray(value, ",");
			List<Charset> result = new ArrayList<Charset>(tokens.length);
			for (String token : tokens) {
				int paramIdx = token.indexOf(';');
				String charsetName;
				if (paramIdx == -1) {
					charsetName = token;
				} else {
					charsetName = token.substring(0, paramIdx);
				}
				if (!charsetName.equals("*")) {
					result.add(Charset.forName(charsetName));
				}
			}
			return result;
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Set the set of allowed {@link HttpMethod HTTP methods}, as specified by
	 * the {@code Allow} header.
	 */
	public void setAllow(Set<HttpMethod> allowedMethods) {
		set(ALLOW, StringUtils.collectionToCommaDelimitedString(allowedMethods));
	}

	/**
	 * Return the set of allowed {@link HttpMethod HTTP methods}, as specified
	 * by the {@code Allow} header.
	 * <p>
	 * Returns an empty set when the allowed methods are unspecified.
	 */
	public Set<HttpMethod> getAllow() {
		String value = getFirst(ALLOW);
		if (!StringUtils.isEmpty(value)) {
			String[] tokens = StringUtils.tokenizeToStringArray(value, ",");
			List<HttpMethod> result = new ArrayList<HttpMethod>(tokens.length);
			for (String token : tokens) {
				HttpMethod resolved = HttpMethod.resolve(token);
				if (resolved != null) {
					result.add(resolved);
				}
			}
			return EnumSet.copyOf(result);
		} else {
			return EnumSet.noneOf(HttpMethod.class);
		}
	}

	/**
	 * Set the (new) value of the {@code Cache-Control} header.
	 */
	public void setCacheControl(String cacheControl) {
		set(CACHE_CONTROL, cacheControl);
	}

	/**
	 * Return the value of the {@code Cache-Control} header.
	 */
	public String getCacheControl() {
		return getFieldValues(CACHE_CONTROL);
	}

	/**
	 * Set the (new) value of the {@code Connection} header.
	 */
	public void setConnection(String connection) {
		set(CONNECTION, connection);
	}

	/**
	 * Set the (new) value of the {@code Connection} header.
	 */
	public void setConnection(List<String> connection) {
		set(CONNECTION, toCommaDelimitedString(connection));
	}

	/**
	 * Return the value of the {@code Connection} header.
	 */
	public List<String> getConnection() {
		return getValuesAsList(CONNECTION);
	}

	/**
	 * Set the {@code Content-Disposition} header when creating a
	 * {@code "multipart/form-data"} request.
	 * <p>
	 * Applications typically would not set this header directly but rather
	 * prepare a {@code MultiValueMap<String, Object>}, containing an Object or
	 * a {@link org.springframework.core.io.Resource} for each part, and then
	 * pass that to the {@code RestTemplate} or {@code WebClient}.
	 * 
	 * @param name
	 *            the control name
	 * @param filename
	 *            the filename (may be {@code null})
	 */
	public void setContentDispositionFormData(String name, String filename) {
		Assert.notNull(name, "'name' must not be null");
		StringBuilder builder = new StringBuilder("form-data; name=\"");
		builder.append(name).append('\"');
		if (filename != null) {
			builder.append("; filename=\"");
			builder.append(filename).append('\"');
		}
		set(CONTENT_DISPOSITION, builder.toString());
	}

	/**
	 * Set the {@literal Content-Disposition} header.
	 * <p>
	 * This could be used on a response to indicate if the content is expected
	 * to be displayed inline in the browser or as an attachment to be saved
	 * locally.
	 * <p>
	 * It can also be used for a {@code "multipart/form-data"} request. For more
	 * details see notes on {@link #setContentDispositionFormData}.
	 * 
	 * @see #getContentDisposition()
	 */
	public void setContentDisposition(ContentDisposition contentDisposition) {
		set(CONTENT_DISPOSITION, contentDisposition.toString());
	}

	/**
	 * Return a parsed representation of the {@literal Content-Disposition}
	 * header.
	 * 
	 * @see #setContentDisposition(ContentDisposition)
	 */
	public ContentDisposition getContentDisposition() {
		String contentDisposition = getFirst(CONTENT_DISPOSITION);
		if (contentDisposition != null) {
			return ContentDisposition.parse(contentDisposition);
		}
		return ContentDisposition.empty();
	}

	/**
	 * Set the length of the body in bytes, as specified by the
	 * {@code Content-Length} header.
	 */
	public void setContentLength(long contentLength) {
		set(CONTENT_LENGTH, Long.toString(contentLength));
	}

	/**
	 * Return the length of the body in bytes, as specified by the
	 * {@code Content-Length} header.
	 * <p>
	 * Returns -1 when the content-length is unknown.
	 */
	public long getContentLength() {
		String value = getFirst(CONTENT_LENGTH);
		return (value != null ? Long.parseLong(value) : -1);
	}

	/**
	 * Set the {@linkplain MediaType media type} of the body, as specified by
	 * the {@code Content-Type} header.
	 */
	public void setContentType(MediaType mediaType) {
		Assert.isTrue(!mediaType.isWildcardType(), "Content-Type cannot contain wildcard type '*'");
		Assert.isTrue(!mediaType.isWildcardSubtype(), "Content-Type cannot contain wildcard subtype '*'");
		set(CONTENT_TYPE, mediaType.toString());
	}

	/**
	 * Return the {@linkplain MediaType media type} of the body, as specified by
	 * the {@code Content-Type} header.
	 * <p>
	 * Returns {@code null} when the content-type is unknown.
	 */
	public MediaType getContentType() {
		String value = getFirst(CONTENT_TYPE);
		return (StringUtils.hasLength(value) ? MediaType.parseMediaType(value) : null);
	}

	/**
	 * Set the date and time at which the message was created, as specified by
	 * the {@code Date} header.
	 * <p>
	 * The date should be specified as the number of milliseconds since January
	 * 1, 1970 GMT.
	 */
	public void setDate(long date) {
		setDate(DATE, date);
	}

	/**
	 * Return the date and time at which the message was created, as specified
	 * by the {@code Date} header.
	 * <p>
	 * The date is returned as the number of milliseconds since January 1, 1970
	 * GMT. Returns -1 when the date is unknown.
	 * 
	 * @throws IllegalArgumentException
	 *             if the value cannot be converted to a date
	 */
	public long getDate() {
		return getFirstDate(DATE);
	}

	/**
	 * Set the (new) entity tag of the body, as specified by the {@code ETag}
	 * header.
	 */
	public void setETag(String etag) {
		if (etag != null) {
			Assert.isTrue(etag.startsWith("\"") || etag.startsWith("W/"), "Invalid ETag: does not start with W/ or \"");
			Assert.isTrue(etag.endsWith("\""), "Invalid ETag: does not end with \"");
		}
		set(ETAG, etag);
	}

	/**
	 * Return the entity tag of the body, as specified by the {@code ETag}
	 * header.
	 */
	public String getETag() {
		return getFirst(ETAG);
	}

	/**
	 * Set the date and time at which the message is no longer valid, as
	 * specified by the {@code Expires} header.
	 * <p>
	 * The date should be specified as the number of milliseconds since January
	 * 1, 1970 GMT.
	 */
	public void setExpires(long expires) {
		setDate(EXPIRES, expires);
	}

	/**
	 * Return the date and time at which the message is no longer valid, as
	 * specified by the {@code Expires} header.
	 * <p>
	 * The date is returned as the number of milliseconds since January 1, 1970
	 * GMT. Returns -1 when the date is unknown.
	 */
	public long getExpires() {
		return getFirstDate(EXPIRES, false);
	}

	/**
	 * Set the (new) value of the {@code If-Match} header.
	 * 
	 */
	public void setIfMatch(String ifMatch) {
		set(IF_MATCH, ifMatch);
	}

	/**
	 * Set the (new) value of the {@code If-Match} header.
	 * 
	 */
	public void setIfMatch(List<String> ifMatchList) {
		set(IF_MATCH, toCommaDelimitedString(ifMatchList));
	}

	/**
	 * Return the value of the {@code If-Match} header.
	 * 
	 */
	public List<String> getIfMatch() {
		return getETagValuesAsList(IF_MATCH);
	}

	/**
	 * Set the (new) value of the {@code If-Modified-Since} header.
	 * <p>
	 * The date should be specified as the number of milliseconds since January
	 * 1, 1970 GMT.
	 */
	public void setIfModifiedSince(long ifModifiedSince) {
		setDate(IF_MODIFIED_SINCE, ifModifiedSince);
	}

	/**
	 * Return the value of the {@code If-Modified-Since} header.
	 * <p>
	 * The date is returned as the number of milliseconds since January 1, 1970
	 * GMT. Returns -1 when the date is unknown.
	 */
	public long getIfModifiedSince() {
		return getFirstDate(IF_MODIFIED_SINCE, false);
	}

	/**
	 * Set the (new) value of the {@code If-None-Match} header.
	 */
	public void setIfNoneMatch(String ifNoneMatch) {
		set(IF_NONE_MATCH, ifNoneMatch);
	}

	/**
	 * Set the (new) values of the {@code If-None-Match} header.
	 */
	public void setIfNoneMatch(List<String> ifNoneMatchList) {
		set(IF_NONE_MATCH, toCommaDelimitedString(ifNoneMatchList));
	}

	/**
	 * Return the value of the {@code If-None-Match} header.
	 */
	public List<String> getIfNoneMatch() {
		return getETagValuesAsList(IF_NONE_MATCH);
	}

	/**
	 * Set the (new) value of the {@code If-Unmodified-Since} header.
	 * <p>
	 * The date should be specified as the number of milliseconds since January
	 * 1, 1970 GMT.
	 * 
	 */
	public void setIfUnmodifiedSince(long ifUnmodifiedSince) {
		setDate(IF_UNMODIFIED_SINCE, ifUnmodifiedSince);
	}

	/**
	 * Return the value of the {@code If-Unmodified-Since} header.
	 * <p>
	 * The date is returned as the number of milliseconds since January 1, 1970
	 * GMT. Returns -1 when the date is unknown.
	 * 
	 */
	public long getIfUnmodifiedSince() {
		return getFirstDate(IF_UNMODIFIED_SINCE, false);
	}

	/**
	 * Set the time the resource was last changed, as specified by the
	 * {@code Last-Modified} header.
	 * <p>
	 * The date should be specified as the number of milliseconds since January
	 * 1, 1970 GMT.
	 */
	public void setLastModified(long lastModified) {
		setDate(LAST_MODIFIED, lastModified);
	}

	/**
	 * Return the time the resource was last changed, as specified by the
	 * {@code Last-Modified} header.
	 * <p>
	 * The date is returned as the number of milliseconds since January 1, 1970
	 * GMT. Returns -1 when the date is unknown.
	 */
	public long getLastModified() {
		return getFirstDate(LAST_MODIFIED, false);
	}

	/**
	 * Set the (new) location of a resource, as specified by the
	 * {@code Location} header.
	 */
	public void setLocation(URI location) {
		set(LOCATION, location.toASCIIString());
	}

	/**
	 * Return the (new) location of a resource as specified by the
	 * {@code Location} header.
	 * <p>
	 * Returns {@code null} when the location is unknown.
	 */
	public URI getLocation() {
		String value = getFirst(LOCATION);
		return (value != null ? URI.create(value) : null);
	}

	/**
	 * Set the (new) value of the {@code Origin} header.
	 */
	public void setOrigin(String origin) {
		set(ORIGIN, origin);
	}

	/**
	 * Return the value of the {@code Origin} header.
	 */
	public String getOrigin() {
		return getFirst(ORIGIN);
	}

	/**
	 * Set the (new) value of the {@code Pragma} header.
	 */
	public void setPragma(String pragma) {
		set(PRAGMA, pragma);
	}

	/**
	 * Return the value of the {@code Pragma} header.
	 */
	public String getPragma() {
		return getFirst(PRAGMA);
	}

	/**
	 * Sets the (new) value of the {@code Range} header.
	 */
	public void setRange(List<HttpRange> ranges) {
		String value = HttpRange.toString(ranges);
		set(RANGE, value);
	}

	/**
	 * Return the value of the {@code Range} header.
	 * <p>
	 * Returns an empty list when the range is unknown.
	 */
	public List<HttpRange> getRange() {
		String value = getFirst(RANGE);
		return HttpRange.parseRanges(value);
	}

	/**
	 * Set the (new) value of the {@code Upgrade} header.
	 */
	public void setUpgrade(String upgrade) {
		set(UPGRADE, upgrade);
	}

	/**
	 * Return the value of the {@code Upgrade} header.
	 */
	public String getUpgrade() {
		return getFirst(UPGRADE);
	}

	/**
	 * Set the request header names (e.g. "Accept-Language") for which the
	 * response is subject to content negotiation and variances based on the
	 * value of those request headers.
	 * 
	 * @param requestHeaders
	 *            the request header names
	 */
	public void setVary(List<String> requestHeaders) {
		set(VARY, toCommaDelimitedString(requestHeaders));
	}

	/**
	 * Return the request header names subject to content negotiation.
	 * 
	 */
	public List<String> getVary() {
		return getValuesAsList(VARY);
	}

	/**
	 * Set the given date under the given header name after formatting it as a
	 * string using the pattern {@code "EEE, dd MMM yyyy HH:mm:ss zzz"}. The
	 * equivalent of {@link #set(String, String)} but for date headers.
	 * 
	 */
	public void setDate(String headerName, long date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMATS[0], Locale.US);
		dateFormat.setTimeZone(GMT);
		set(headerName, dateFormat.format(new Date(date)));
	}

	/**
	 * Parse the first header value for the given header name as a date, return
	 * -1 if there is no value, or raise {@link IllegalArgumentException} if the
	 * value cannot be parsed as a date.
	 * 
	 * @param headerName
	 *            the header name
	 * @return the parsed date header, or -1 if none
	 */
	public long getFirstDate(String headerName) {
		return getFirstDate(headerName, true);
	}

	/**
	 * Parse the first header value for the given header name as a date, return
	 * -1 if there is no value or also in case of an invalid value (if
	 * {@code rejectInvalid=false}), or raise {@link IllegalArgumentException}
	 * if the value cannot be parsed as a date.
	 * 
	 * @param headerName
	 *            the header name
	 * @param rejectInvalid
	 *            whether to reject invalid values with an
	 *            {@link IllegalArgumentException} ({@code true}) or rather
	 *            return -1 in that case ({@code false})
	 * @return the parsed date header, or -1 if none (or invalid)
	 */
	private long getFirstDate(String headerName, boolean rejectInvalid) {
		String headerValue = getFirst(headerName);
		if (headerValue == null) {
			// No header value sent at all
			return -1;
		}
		if (headerValue.length() >= 3) {
			// Short "0" or "-1" like values are never valid HTTP date
			// headers...
			// Let's only bother with SimpleDateFormat parsing for long enough
			// values.
			for (String dateFormat : DATE_FORMATS) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
				simpleDateFormat.setTimeZone(GMT);
				try {
					return simpleDateFormat.parse(headerValue).getTime();
				} catch (ParseException ex) {
					// ignore
				}
			}
		}
		if (rejectInvalid) {
			throw new IllegalArgumentException(
					"Cannot parse date value \"" + headerValue + "\" for \"" + headerName + "\" header");
		}
		return -1;
	}

	/**
	 * Return all values of a given header name, even if this header is set
	 * multiple times.
	 */
	public List<String> getValuesAsList(String headerName) {
		return getValuesAsList(headerName, ",");
	}

	/**
	 * Retrieve a combined result from the field values of the ETag header.
	 * 
	 * @param headerName
	 *            the header name
	 * @return the combined result
	 */
	protected List<String> getETagValuesAsList(String headerName) {
		List<String> values = get(headerName);
		if (values != null) {
			List<String> result = new ArrayList<String>();
			for (String value : values) {
				if (value != null) {
					Matcher matcher = ETAG_HEADER_VALUE_PATTERN.matcher(value);
					while (matcher.find()) {
						if ("*".equals(matcher.group())) {
							result.add(matcher.group());
						} else {
							result.add(matcher.group(1));
						}
					}
					if (result.isEmpty()) {
						throw new IllegalArgumentException(
								"Could not parse header '" + headerName + "' with value '" + value + "'");
					}
				}
			}
			return result;
		}
		return Collections.emptyList();
	}

	/**
	 * Retrieve a combined result from the field values of multi-valued headers.
	 * 
	 * @param headerName
	 *            the header name
	 * @return the combined result
	 */
	protected String getFieldValues(String headerName) {
		List<String> headerValues = get(headerName);
		return (headerValues != null ? toCommaDelimitedString(headerValues) : null);
	}

	/**
	 * Turn the given list of header values into a comma-delimited result.
	 * 
	 * @param headerValues
	 *            the list of header values
	 * @return a combined result with comma delimitation
	 */
	protected String toCommaDelimitedString(List<String> headerValues) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<String> it = headerValues.iterator(); it.hasNext();) {
			String val = it.next();
			builder.append(val);
			if (it.hasNext()) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	// MultiValueMap implementation

	/**
	 * Return the first header value for the given header name, if any.
	 * 
	 * @param headerName
	 *            the header name
	 * @return the first header value, or {@code null} if none
	 */
	public String getFirst(String headerName) {
		List<String> headerValues = get(headerName);
		if (CollectionUtils.isEmpty(headerValues)) {
			return null;
		}
		return headerValues.get(0);
	}

	/**
	 * Add the given, single header value under the given name.
	 * 
	 * @param headerName
	 *            the header name
	 * @param headerValue
	 *            the header value
	 * @throws UnsupportedOperationException
	 *             if adding headers is not supported
	 * @see #put(String, List)
	 * @see #set(String, String)
	 */
	public void add(String headerName, String headerValue) {
		List<String> headerValues = get(headerName);
		if (headerValues == null) {
			headerValues = new LinkedList<String>();
			put(headerName, headerValues);
		}
		headerValues.add(headerValue);
	}

	/**
	 * Set the given, single header value under the given name.
	 * 
	 * @param headerName
	 *            the header name
	 * @param headerValue
	 *            the header value
	 * @throws UnsupportedOperationException
	 *             if adding headers is not supported
	 * @see #put(String, List)
	 * @see #add(String, String)
	 */
	public void set(String headerName, String headerValue) {
		List<String> headerValues = new LinkedList<String>();
		headerValues.add(headerValue);
		put(headerName, headerValues);
	}

	public void setAll(Map<String, String> values) {
		for (Entry<String, String> entry : values.entrySet()) {
			set(entry.getKey(), entry.getValue());
		}
	}

	public Map<String, String> toSingleValueMap() {
		LinkedHashMap<String, String> singleValueMap = new LinkedHashMap<String, String>(size());
		for (Entry<String, List<String>> entry : entrySet()) {
			singleValueMap.put(entry.getKey(), entry.getValue().get(0));
		}
		return singleValueMap;
	}

	public boolean isAjax() {
		for (Entry<String, Value> entry : AJAX_HEADERS.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue().getAsObject(String[].class);
			if (ArrayUtils.isEmpty(values)) {
				continue;
			}

			if (matchHeaders(key, get(key), values)) {
				return true;
			}
		}
		return false;
	}

	private boolean matchHeaders(String name, List<String> values, String[] matchs) {
		for (String match : matchs) {
			if (match == null) {
				continue;
			}

			if (values == null) {
				if (matchHeader(name, null, match)) {
					return true;
				}
			} else {
				for (String value : values) {
					if (matchHeader(name, value, match)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean matchHeader(String name, String value, String match) {
		boolean b = DefaultStringMatcher.getInstance().match(match, value);
		if (logger.isDebugEnabled()) {
			logger.debug("check ajax header name={}, value={}, match={}, result={}", name, value, match, b);
		}
		return b;
	}

	public boolean isFormContentType() {
		return isCompatibleWithContentType(MediaType.APPLICATION_FORM_URLENCODED);
	}

	public boolean isJsonContentType() {
		return isCompatibleWithContentType(MediaType.APPLICATION_JSON, MimeTypeUtils.TEXT_JSON);
	}

	public boolean isXmlContentType() {
		return isCompatibleWithContentType(MediaType.APPLICATION_XML, MediaType.TEXT_XML);
	}

	public boolean isMultipartFormContentType() {
		return isCompatibleWithContentType(MediaType.MULTIPART_FORM_DATA);
	}

	public boolean isCompatibleWithContentType(MimeType... contentTypes) {
		if (contentTypes == null) {
			return false;
		}

		MediaType contentType = getContentType();
		if (contentType == null) {
			return false;
		}

		for (MimeType type : contentTypes) {
			if (contentType.isCompatibleWith(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Encode the given header field param as describe in RFC 5987.
	 * 
	 * @param input
	 *            the header field param
	 * @param charset
	 *            the charset of the header field param string
	 * @return the encoded header field param
	 * @see <a href="https://tools.ietf.org/html/rfc5987">RFC 5987</a>
	 */
	static String encodeHeaderFieldParam(String input, Charset charset) {
		Assert.notNull(input, "Input String should not be null");
		Assert.notNull(charset, "Charset should not be null");
		if (charset.name().equals("US-ASCII")) {
			return input;
		}
		Assert.isTrue(charset.name().equals("UTF-8") || charset.name().equals("ISO-8859-1"),
				"Charset should be UTF-8 or ISO-8859-1");
		byte[] source = input.getBytes(charset);
		int len = source.length;
		StringBuilder sb = new StringBuilder(len << 1);
		sb.append(charset.name());
		sb.append("''");
		for (byte b : source) {
			if (isRFC5987AttrChar(b)) {
				sb.append((char) b);
			} else {
				sb.append('%');
				char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
				char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
				sb.append(hex1);
				sb.append(hex2);
			}
		}
		return sb.toString();
	}

	private static boolean isRFC5987AttrChar(byte c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '!' || c == '#'
				|| c == '$' || c == '&' || c == '+' || c == '-' || c == '.' || c == '^' || c == '_' || c == '`'
				|| c == '|' || c == '~';
	}

	private static final String[] GET_IP_HEADERES = new String[] { X_FORWARDED_FOR, X_REAL_IP };

	/**
	 * 通过headers获取客户端ip(一般来说获取的都是代理工具的ip)
	 * 
	 * @see ServletServerHttpRequest#getIp()
	 * @return
	 */
	@Nullable
	public String getIp() {
		for (String name : GET_IP_HEADERES) {
			String value = getFirst(name);
			if (value == null) {
				continue;
			}

			String[] ips = StringUtils.split(value, ",");
			for (String ip : ips) {
				if (StringUtils.isEmpty(ip) || "unKnown".equals(ip) || InetUtils.isInnerIP(ip)) {
					continue;
				}

				InetAddress inetAddress;
				try {
					inetAddress = InetAddress.getByName(ip);
				} catch (UnknownHostException e) {
					continue;
				}

				if (inetAddress.isMCGlobal()) {
					continue;
				}

				return inetAddress.getHostAddress();
			}
		}
		return null;
	}

}