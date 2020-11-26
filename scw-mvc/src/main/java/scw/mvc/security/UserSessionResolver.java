package scw.mvc.security;

import scw.core.GlobalPropertyFactory;
import scw.mvc.HttpChannel;

public interface UserSessionResolver{
	static final String TOKEN_NAME = GlobalPropertyFactory.getInstance().getValue(HttpChannel.SESSIONID_ATTRIBUTE,
			String.class, "token");
	static final String UID_NAME = GlobalPropertyFactory.getInstance().getValue(HttpChannel.UID_ATTRIBUTE, String.class,
			"uid");
	
	<T> T getUid(HttpChannel httpChannel, Class<T> type);
	
	String getSessionId(HttpChannel httpChannel);
}
