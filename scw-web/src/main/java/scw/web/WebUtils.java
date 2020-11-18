package scw.web;

import java.io.IOException;
import java.util.List;

import scw.core.utils.CollectionUtils;
import scw.http.HttpCookie;
import scw.http.HttpMethod;
import scw.http.HttpStatus;
import scw.http.server.HttpServiceConfigAccessor;
import scw.http.server.JsonServerHttpRequest;
import scw.http.server.MultiPartServerHttpRequest;
import scw.http.server.ServerHttpRequest;
import scw.http.server.ServerHttpResponse;
import scw.io.IOUtils;
import scw.io.Resource;
import scw.json.JsonArray;
import scw.json.JsonElement;
import scw.json.JsonObject;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.net.InetUtils;
import scw.net.MimeType;
import scw.net.Restful;
import scw.net.Restful.RestfulMatchingResult;
import scw.net.message.multipart.FileItemParser;
import scw.util.XUtils;
import scw.value.EmptyValue;
import scw.value.StringValue;
import scw.value.Value;
import scw.value.ValueUtils;

public final class WebUtils {
	private static Logger logger = LoggerFactory.getLogger(WebUtils.class);
	
	/**
	 * 缓存是否过期,如果未过期那么返回304，如果已过期则setLastModified
	 * 
	 * @param request
	 * @param response
	 * @param lastModified
	 * @return
	 */
	public static boolean isExpired(ServerHttpRequest request, ServerHttpResponse response, long lastModified) {
		response.getHeaders().setLastModified(lastModified);
		long ifModifiedSince = request.getHeaders().getIfModifiedSince();
		if (ifModifiedSince < 0 || lastModified < 0) {
			// 缓存已过期,请求中没有此值
			return true;
		}

		// 不比较毫秒
		if (ifModifiedSince / 1000 != lastModified / 1000) {
			// 缓存已过期
			return true;
		}

		// 客户端缓存未过期
		response.setStatusCode(HttpStatus.NOT_MODIFIED);
		return false;
	}

	/**
	 * 写入一个静态资源
	 * 
	 * @param request
	 * @param response
	 * @param resource
	 * @param mimeType
	 * @throws IOException
	 */
	public static void writeStaticResource(ServerHttpRequest request, ServerHttpResponse response, Resource resource,
			MimeType mimeType) throws IOException {
		if (!resource.exists()) {
			response.sendError(HttpStatus.NOT_FOUND.value(), "The resource does not exist!");
			return;
		}

		if (mimeType != null) {
			response.setContentType(mimeType);
		}

		if (!isExpired(request, response, resource.lastModified())) {
			return;
		}
		IOUtils.copy(resource.getInputStream(), response.getBody());
	}

	/**
	 * 根据参数名获取
	 * 
	 * @param request
	 * @param name
	 * @return 如果不存在返回{@see EmptyValue}
	 */
	public static Value getParameter(ServerHttpRequest request, String name) {
		String value = request.getParameterMap().getFirst(name);
		if (value != null) {
			return new StringValue(value);
		}

		JsonServerHttpRequest jsonServerHttpRequest = XUtils.getTarget(request, JsonServerHttpRequest.class);
		if (jsonServerHttpRequest != null) {
			JsonObject jsonObject = jsonServerHttpRequest.getJsonObject();
			if (jsonObject != null) {
				JsonElement element = jsonObject.get(name);
				if (element != null) {
					return element;
				}
			}
		}
		return EmptyValue.INSTANCE;
	}

	/**
	 * 此方法不会返回空，如果不存在返回的数组长度为0
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public static Value[] getParameterValues(ServerHttpRequest request, String name) {
		List<String> valueList = request.getParameterMap().get(name);
		if (!CollectionUtils.isEmpty(valueList)) {
			Value[] values = new Value[valueList.size()];
			int index = 0;
			for (String value : valueList) {
				values[index++] = new StringValue(value);
			}
			return values;
		}

		JsonServerHttpRequest jsonServerHttpRequest = XUtils.getTarget(request, JsonServerHttpRequest.class);
		if (jsonServerHttpRequest != null) {
			JsonObject jsonObject = jsonServerHttpRequest.getJsonObject();
			if (jsonObject != null) {
				JsonElement jsonElement = jsonObject.get(name);
				if (jsonElement.isJsonArray()) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					Value[] values = new Value[jsonArray.size()];
					int index = 0;
					for (JsonElement element : jsonElement.getAsJsonArray()) {
						values[index++] = element;
					}
					return values;
				}
			}
		}
		return ValueUtils.EMPTY_ARRAY;
	}

	public static ServerHttpRequest wrapperServerJsonRequest(ServerHttpRequest request,
			HttpServiceConfigAccessor configAccessor) {
		if (request.getMethod() == HttpMethod.GET) {
			return request;
		}

		// 如果是一个json请求，那么包装一下
		if (request.getHeaders().isJsonContentType()
				&& (configAccessor == null || configAccessor.isSupportJsonWrapper(request))) {
			JsonServerHttpRequest jsonServerHttpRequest = XUtils.getTarget(request, JsonServerHttpRequest.class);
			if (jsonServerHttpRequest != null) {
				// 返回原始对象
				return request;
			}

			return new JsonServerHttpRequest(request);
		}
		return request;
	}

	private static FileItemParser getFileItemParser(HttpServiceConfigAccessor configAccessor) {
		return configAccessor == null ? InetUtils.getFileItemParser() : configAccessor.getFileItemParser();
	}

	public static ServerHttpRequest wrapperServerMultipartFormRequest(ServerHttpRequest request,
			HttpServiceConfigAccessor configAccessor) {
		if (request.getMethod() == HttpMethod.GET) {
			return request;
		}

		// 如果是 一个MultiParty请求，那么包装一下
		if (request.getHeaders().isMultipartFormContentType()) {
			MultiPartServerHttpRequest multiPartServerHttpRequest = XUtils.getTarget(request,
					MultiPartServerHttpRequest.class);
			if (multiPartServerHttpRequest != null) {
				// 返回原始对象
				return request;
			}

			FileItemParser fileItemParser = getFileItemParser(configAccessor);
			if (fileItemParser == null) {
				logger.warn("Multipart is not supported: {}", request);
			} else {
				return new MultiPartServerHttpRequest(request, fileItemParser);
			}
		}
		return request;
	}
	
	/**
	 * 从cookie中获取数据
	 * 
	 * @param request
	 * 
	 * @param name
	 *            cookie中的名字
	 * @return
	 */
	public static HttpCookie getCookie(ServerHttpRequest request, String name) {
		if (name == null) {
			return null;
		}

		HttpCookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length == 0) {
			return null;
		}

		for (HttpCookie cookie : cookies) {
			if (cookie == null) {
				continue;
			}

			if (name.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}
	
	public static RestfulMatchingResult matching(Restful restful, ServerHttpRequest request) {
		RestfulMatchingResult result = restful.matching(request.getPath());
		if (result.isSuccess()) {
			ServerHttpRequest targetRequest = request;
			Restful.restfulParameterMapAware(targetRequest, result.getParameterMap());
		}
		return result;
	}
}
