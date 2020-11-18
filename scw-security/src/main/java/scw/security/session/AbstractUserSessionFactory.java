package scw.security.session;

public abstract class AbstractUserSessionFactory<T> implements UserSessionFactory<T> {
	public abstract SessionFactory getSessionFactory();

	public UserSession<T> getSession(String sessionId) {
		Session session = getSessionFactory().getSession(sessionId);
		if (session == null) {
			return null;
		}

		T uid = getUid(session.getId());
		if (uid == null) {
			return null;
		}

		touch(uid, session);
		return new DefaultUserSession<T>(session, uid, this);
	}

	public UserSession<T> getSession(T uid) {
		String sessionId = getSessionId(uid);
		if (sessionId == null) {
			return null;
		}

		Session session = getSessionFactory().getSession(sessionId);
		if (session == null) {
			return null;
		}

		touch(uid, session);
		return new DefaultUserSession<T>(session, uid, this);
	}

	public UserSession<T> getSession(T uid, String sessionId, boolean create) {
		Session session = getSessionFactory().getSession(sessionId, create);
		if (session == null) {
			return null;
		}

		if (session.isNew()) {
			addUidMapping(uid, session);
		} else {
			touch(uid, session);
		}
		return new DefaultUserSession<T>(session, uid, this);
	}

	protected abstract void invalidate(T uid);

	protected abstract void touch(T uid, Session session);

	protected abstract void addUidMapping(T uid, Session session);

	protected abstract T getUid(String sessionId);

	protected abstract String getSessionId(T uid);
}
