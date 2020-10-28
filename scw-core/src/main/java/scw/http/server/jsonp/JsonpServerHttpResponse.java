package scw.http.server.jsonp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import scw.core.Assert;
import scw.core.utils.StringUtils;
import scw.http.MediaType;
import scw.http.server.ServerHttpResponse;
import scw.http.server.ServerHttpResponseWrapper;

/**
 * 使用结束后必须要调用close方法
 * @author shuchaowen
 *
 */
public class JsonpServerHttpResponse extends ServerHttpResponseWrapper {
	private final String jsonp;
	private boolean writeJsonp = false;
	private boolean close = false;

	public JsonpServerHttpResponse(String jsonp,
			ServerHttpResponse targetResponse) {
		super(targetResponse);
		Assert.requiredArgument(StringUtils.isNotEmpty(jsonp), "jsonp");
		this.jsonp = jsonp;
	}

	private void writeJsonp() throws IOException {
		if (writeJsonp) {
			return;
		}
		getHeaders().setContentType(MediaType.TEXT_JAVASCRIPT);
		super.getWriter().write(jsonp);
		super.getWriter().write(JsonpUtils.JSONP_RESP_PREFIX);
		writeJsonp = true;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		writeJsonp();
		return super.getWriter();
	}

	@Override
	public OutputStream getBody() throws IOException {
		writeJsonp();
		return super.getBody();
	}

	@Override
	public void close() throws IOException {
		if(close){
			return ;
		}
		
		close = true;
		if (writeJsonp) {
			super.getWriter().write(JsonpUtils.JSONP_RESP_SUFFIX);
		}
		super.close();
	}
}
