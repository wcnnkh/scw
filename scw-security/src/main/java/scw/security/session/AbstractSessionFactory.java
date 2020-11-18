package scw.security.session;

public abstract class AbstractSessionFactory implements SessionFactory {
	private int defaultMaxInactiveInterval;

	public AbstractSessionFactory(int defaultMaxInactiveInterval) {
		this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
	}

	public Session getSession(String sessionId) {
		return getSession(sessionId, false);
	}

	public Session getSession(String sessionId, boolean create) {
		SessionData sessionData = getSessionData(sessionId);
		if (sessionData == null && create) {
			sessionData = new SessionData();
			sessionData.setCreateTime(System.currentTimeMillis());
			sessionData.setMaxInactiveInterval(defaultMaxInactiveInterval);
			sessionData.setSessionId(sessionId);
			setSessionData(sessionData);
			return new DefaultSession(this, sessionData, true);
		}

		if (sessionData == null) {
			return null;
		}

		setSessionData(sessionData);
		return new DefaultSession(this, sessionData, false);
	}

	public abstract SessionData getSessionData(String sessionId);

	public abstract void setSessionData(SessionData sessionData);

	public abstract void invalidate(String sessionId);
}
