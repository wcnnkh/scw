package scw.mvc;

import scw.core.instance.NoArgsInstanceFactory;
import scw.core.parameter.ParameterDescriptor;
import scw.core.parameter.ParameterFactory;
import scw.http.server.ServerHttpRequest;
import scw.http.server.ServerHttpResponse;
import scw.lang.Nullable;
import scw.mvc.security.UserSessionFactoryAdapter;
import scw.mvc.security.UserSessionResolver;
import scw.security.session.UserSession;
import scw.value.Value;

public interface HttpChannel extends ParameterFactory, UserSessionFactoryAdapter {
	static final String UID_ATTRIBUTE = "mvc.http.channel.uid";
	static final String SESSIONID_ATTRIBUTE = "mvc.http.channel.sessionid";
	
	long getCreateTime();

	ServerHttpRequest getRequest();

	ServerHttpResponse getResponse();

	<E> E[] getArray(String name, Class<? extends E> type);

	boolean isCompleted();

	Value getValue(String name);

	Value getValue(String name, Value defaultValue);

	Object getParameter(ParameterDescriptor parameterDescriptor);
	
	NoArgsInstanceFactory getInstanceFactory();
	
	UserSessionResolver getUserSessionResolver();
	
	@Nullable
	<T> T getUid(Class<T> type);
	
	@Nullable
	String getSessionId();
	
	@Nullable
	<T> UserSession<T> getUserSession(Class<T> type);
	
	<T> UserSession<T> createUserSession(Class<T> type, T uid, String sessionId);
}
