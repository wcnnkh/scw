package scw.security.session;

public final class DefaultUserSession<T> extends SessionWrapper implements UserSession<T> {
	private T uid;
	private AbstractUserSessionFactory<T> userSessionFactory;

	public DefaultUserSession(Session session, T uid, AbstractUserSessionFactory<T> userSessionFactory) {
		super(session);
		this.uid = uid;
		this.userSessionFactory = userSessionFactory;
	}

	public T getUid() {
		return uid;
	}

	@Override
	public void setMaxInactiveInterval(int maxInactiveInterval) {
		super.setMaxInactiveInterval(maxInactiveInterval);
		userSessionFactory.touch(uid, this);
	}

	@Override
	public void invalidate() {
		userSessionFactory.invalidate(uid);
		super.invalidate();
	}
}
