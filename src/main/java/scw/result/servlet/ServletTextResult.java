package scw.result.servlet;

import java.util.HashMap;
import java.util.Map;

import scw.json.JSONUtils;
import scw.result.DefaultResult;
import scw.servlet.Text;

public class ServletTextResult<T> extends DefaultResult<T> implements Text {
	private static final long serialVersionUID = 1L;
	private String contentType;

	protected ServletTextResult() {
	}

	public ServletTextResult(boolean success, int code, T data, String msg,
			String contentType, boolean rollbackOnly) {
		super(success, code, data, msg, rollbackOnly);
		this.contentType = contentType;
	}

	public String getTextContent() {
		Map<String, Object> map = new HashMap<String, Object>(4, 1);
		map.put("success", isSuccess());
		map.put("code", getCode());
		map.put("data", getData());
		map.put("msg", getMsg());
		return JSONUtils.toJSONString(map);
	}

	public String getTextContentType() {
		return contentType;
	}
}
