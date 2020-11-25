package scw.mvc.security;

import scw.security.session.UserSessionFactory;

public interface UserSessionFactoryAdapter {
	<T> UserSessionFactory<T> getUserSessionFactory(Class<T> type);
}
