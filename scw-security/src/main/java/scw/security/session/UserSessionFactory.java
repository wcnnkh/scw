package scw.security.session;

public interface UserSessionFactory<T> {
	UserSessions<T> getUserSessions(T uid);
	
	UserSession<T> getUserSession(T uid, String sessionId);

	UserSession<T> getUserSession(T uid, String sessionId, boolean create);
}
