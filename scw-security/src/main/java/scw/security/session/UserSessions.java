package scw.security.session;

public interface UserSessions<T> extends Iterable<Session>{
	T getUid();
	
	Session getSession(String sessionId);
	
	int size();
}