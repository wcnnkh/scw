package scw.security.session;

public interface UserSession<T> extends Session {
	T getUid();
}
