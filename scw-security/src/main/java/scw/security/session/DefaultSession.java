package scw.security.session;

import scw.util.attribute.AttributesWrapper;

public final class DefaultSession extends AttributesWrapper<String, Object> implements Session {
	private AbstractSessionFactory sessionFactory;
	private SessionData sessionData;
	private boolean create;
	private long lastAccessedTime;

	public DefaultSession(AbstractSessionFactory sessionFactory, SessionData sessionData, boolean create) {
		super(sessionData);
		this.sessionFactory = sessionFactory;
		this.sessionData = sessionData;
		this.create = create;
		this.lastAccessedTime = System.currentTimeMillis();
	}

	public long getCreationTime() {
		return sessionData.getCreateTime();
	}

	public String getId() {
		return sessionData.getSessionId();
	}

	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		sessionData.setMaxInactiveInterval(maxInactiveInterval);
		sessionFactory.setSessionData(sessionData);
	}

	public int getMaxInactiveInterval() {
		return sessionData.getMaxInactiveInterval();
	}

	public void setAttribute(String name, Object value) {
		super.setAttribute(name, value);
		sessionFactory.setSessionData(sessionData);
	}

	public void removeAttribute(String name) {
		super.removeAttribute(name);
		sessionFactory.setSessionData(sessionData);
	}

	public void invalidate() {
		sessionFactory.invalidate(sessionData.getSessionId());
	}

	public boolean isNew() {
		return create;
	}

}
