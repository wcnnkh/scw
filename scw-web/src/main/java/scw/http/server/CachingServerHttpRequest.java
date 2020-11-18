package scw.http.server;

import java.io.IOException;
import java.io.InputStream;

import scw.http.HttpMethod;
import scw.io.IOUtils;
import scw.io.UnsafeByteArrayInputStream;
import scw.util.XUtils;

/**
 * 缓存请求体，未处理内存溢出的情况
 * @author shuchaowen
 *
 */
public class CachingServerHttpRequest extends ServerHttpRequestWrapper {

	public CachingServerHttpRequest(ServerHttpRequest targetRequest) {
		super(targetRequest, true);
	}

	private byte[] bytes;

	public byte[] getBytes() throws IOException {
		CachingServerHttpRequest cachingServerHttpRequest = XUtils.getTarget(getTargetRequest(),
				CachingServerHttpRequest.class);
		if (cachingServerHttpRequest != null) {
			return cachingServerHttpRequest.getBytes();
		}

		if (getMethod() == HttpMethod.GET) {
			return null;
		}

		InputStream inputStream = super.getBody();
		if (inputStream == null) {
			return null;
		}

		if (bytes == null) {
			bytes = IOUtils.toByteArray(inputStream);
		}
		return bytes;
	}

	@Override
	public InputStream getBody() throws IOException {
		byte[] bytes = getBytes();
		if (bytes == null) {
			return null;
		}
		return new UnsafeByteArrayInputStream(bytes);
	}
}
