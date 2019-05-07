package scw.servlet.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import scw.core.net.http.ContentType;
import scw.servlet.Request;
import scw.servlet.Response;
import scw.servlet.View;

public abstract class AbstractTextView implements View {
	private Map<String, String> responseProperties;

	public abstract String getResponseText();

	public void addResponseHeader(String key, String value) {
		if (responseProperties == null) {
			responseProperties = new HashMap<String, String>(8);
		}
		responseProperties.put(key, value);
	}

	public void render(Request request, Response response) throws IOException {
		if (responseProperties != null) {
			for (Entry<String, String> entry : responseProperties.entrySet()) {
				response.setHeader(entry.getKey(), entry.getValue());
			}
		}

		if (response.getContentType() == null) {
			response.setContentType(ContentType.TEXT_HTML);
		}
		response.write(getResponseText());
	}
}
