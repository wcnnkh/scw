package scw.mvc.security;

import scw.core.instance.annotation.Configuration;
import scw.http.HttpCookie;
import scw.mvc.HttpChannel;
import scw.value.EmptyValue;
import scw.value.StringValue;
import scw.value.Value;
import scw.web.WebUtils;

@Configuration(order = Integer.MIN_VALUE)
public class DefaultUserSessionResolver implements UserSessionResolver {

	public <T> T getUid(HttpChannel httpChannel, Class<T> type) {
		Value value = getParameter(httpChannel, UID_NAME);
		return value == null ? EmptyValue.INSTANCE.getAsObject(type) : value
				.getAsObject(type);
	}

	public String getSessionId(HttpChannel httpChannel) {
		Value value = getParameter(httpChannel, TOKEN_NAME);
		return value == null ? null : value.getAsString();
	}

	protected Value getParameter(HttpChannel httpChannel, String name) {
		Value value = httpChannel.getValue(name);
		if (value == null || value.isEmpty()) {
			String token = httpChannel.getRequest().getHeaders().getFirst(name);
			if (token == null) {
				HttpCookie httpCookie = WebUtils.getCookie(
						httpChannel.getRequest(), name);
				if (httpCookie != null) {
					token = httpCookie.getValue();
				}
			}

			if (token != null) {
				value = new StringValue(token);
			}
		}
		return value;
	}
}
