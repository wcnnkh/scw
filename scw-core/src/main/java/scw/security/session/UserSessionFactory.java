package scw.security.session;

public interface UserSessionFactory<T> {
	UserSession<T> getSession(T uid);

	UserSession<T> getSession(String sessionId);

	UserSession<T> getSession(T uid, String sessionId, boolean create);
}
