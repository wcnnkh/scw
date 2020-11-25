package scw.mvc.security;

import scw.core.GlobalPropertyFactory;
import scw.mvc.HttpChannel;

public interface UserSessionResolver{
	static final String TOKEN_NAME = GlobalPropertyFactory.getInstance().getValue("mvc.security.token.name",
			String.class, "token");
	static final String UID_NAME = GlobalPropertyFactory.getInstance().getValue("mvc.security.uid.name", String.class,
			"uid");
	
	<T> T getUid(HttpChannel httpChannel, Class<T> type);
	
	String getSessionId(HttpChannel httpChannel);
}
