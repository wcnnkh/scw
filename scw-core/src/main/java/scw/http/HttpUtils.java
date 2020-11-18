package scw.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import scw.core.Assert;
import scw.core.Constants;
import scw.core.instance.InstanceUtils;
import scw.core.utils.CollectionUtils;
import scw.core.utils.ObjectUtils;
import scw.core.utils.StringUtils;
import scw.http.client.HttpClient;
import scw.net.FileMimeTypeUitls;
import scw.net.MimeType;
import scw.net.uri.UriComponentsBuilder;

public final class HttpUtils {
	private HttpUtils() {
	};

	private static final HttpClient HTTP_CLIENT = InstanceUtils.loadService(HttpClient.class,
			"scw.http.client.SimpleHttpClient");

	public static HttpClient getHttpClient() {
		return HTTP_CLIENT;
	}

	public static boolean isValidOrigin(HttpRequest request, Collection<String> allowedOrigins) {
		Assert.notNull(request, "Request must not be null");
		Assert.notNull(allowedOrigins, "Allowed origins must not be null");

		String origin = request.getHeaders().getOrigin();
		if (origin == null || allowedOrigins.contains("*")) {
			return true;
		} else if (CollectionUtils.isEmpty(allowedOrigins)) {
			return isSameOrigin(request);
		} else {
			return allowedOrigins.contains(origin);
		}
	}

	/**
	 * 是否是同一个origin
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isSameOrigin(HttpRequest request) {
		HttpHeaders headers = request.getHeaders();
		String origin = headers.getOrigin();
		if (origin == null) {
			return true;
		}

		return isSameOrigin(request.getURI(), UriComponentsBuilder.fromOriginHeader(origin).build().toUri());
	}

	/**
	 * 判断两个url是否同源
	 * 
	 * @param url1
	 * @param url2
	 * @return
	 */
	public static boolean isSameOrigin(String url1, String url2) {
		if (url1 == null || url2 == null) {
			return false;
		}

		if (StringUtils.equals(url1, url2)) {
			return true;
		}

		try {
			return isSameOrigin(new URI(url1), new URI(url2));
		} catch (URISyntaxException e) {
			return false;
		}
	}

	/**
	 * 判断两个uri是否同源
	 * 
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static boolean isSameOrigin(URI uri1, URI uri2) {
		if (uri1 == null || uri2 == null) {
			return false;
		}

		if (uri1.equals(uri2)) {
			return true;
		}

		return (ObjectUtils.nullSafeEquals(uri1.getScheme(), uri2.getScheme())
				&& ObjectUtils.nullSafeEquals(uri1.getHost(), uri2.getHost())
				&& getPort(uri1.getScheme(), uri1.getPort()) == getPort(uri2.getScheme(), uri2.getPort()));
	}

	private static int getPort(String scheme, int port) {
		if (port == -1) {
			if ("http".equals(scheme) || "ws".equals(scheme)) {
				port = 80;
			} else if ("https".equals(scheme) || "wss".equals(scheme)) {
				port = 443;
			}
		}
		return port;
	}

	/**
	 * 将文件信息写入ContentDisposition
	 * 
	 * @param outputMessage
	 * @param fileName
	 */
	public static void writeFileMessageHeaders(HttpOutputMessage outputMessage, String fileName) {
		MimeType mimeType = FileMimeTypeUitls.getMimeType(fileName);
		if (mimeType != null) {
			outputMessage.setContentType(mimeType);
		}
		ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
				.filename(fileName, Constants.UTF_8).build();
		outputMessage.getHeaders().setContentDisposition(contentDisposition);
	}
}
