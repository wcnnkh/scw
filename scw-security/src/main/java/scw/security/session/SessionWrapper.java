package scw.security.session;

import java.util.Enumeration;

import scw.core.utils.ObjectUtils;

public class SessionWrapper implements Session {
	private final Session session;

	public SessionWrapper(Session session) {
		this.session = session;
	}

	public Object getAttribute(String name) {
		return session.getAttribute(name);
	}

	public Enumeration<String> getAttributeNames() {
		return session.getAttributeNames();
	}

	public void setAttribute(String name, Object o) {
		session.setAttribute(name, o);
	}

	public void removeAttribute(String name) {
		session.removeAttribute(name);
	}

	public long getCreationTime() {
		return session.getCreationTime();
	}

	public String getId() {
		return session.getId();
	}

	public long getLastAccessedTime() {
		return session.getLastAccessedTime();
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		session.setMaxInactiveInterval(maxInactiveInterval);
	}

	public int getMaxInactiveInterval() {
		return session.getMaxInactiveInterval();
	}

	public void invalidate() {
		session.invalidate();
	}

	public boolean isNew() {
		return session.isNew();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true;
		}
		
		if(obj == null){
			return false;
		}
		
		if(obj instanceof SessionWrapper){
			return ObjectUtils.nullSafeEquals(session, ((SessionWrapper) obj).session);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return session.hashCode();
	}

	@Override
	public String toString() {
		return session.toString();
	}
}
