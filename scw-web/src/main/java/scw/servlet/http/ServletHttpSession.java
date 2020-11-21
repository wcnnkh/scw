package scw.servlet.http;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import scw.core.utils.ObjectUtils;
import scw.security.session.Session;

public class ServletHttpSession implements Session {
	private HttpSession httpSession;

	public ServletHttpSession(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	public Object getAttribute(String name) {
		return httpSession.getAttribute(name);
	}

	public Enumeration<String> getAttributeNames() {
		return httpSession.getAttributeNames();
	}

	public void setAttribute(String name, Object o) {
		httpSession.setAttribute(name, o);
	}

	public void removeAttribute(String name) {
		httpSession.removeAttribute(name);
	}

	public long getCreationTime() {
		return httpSession.getCreationTime();
	}

	public String getId() {
		return httpSession.getId();
	}

	public long getLastAccessedTime() {
		return httpSession.getLastAccessedTime();
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		httpSession.setMaxInactiveInterval(maxInactiveInterval);
	}

	public int getMaxInactiveInterval() {
		return httpSession.getMaxInactiveInterval();
	}

	public void invalidate() {
		httpSession.invalidate();
	}

	public boolean isNew() {
		return httpSession.isNew();
	}
	
	@Override
	public int hashCode() {
		return httpSession.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		if(obj == this){
			return true;
		}
		
		if(obj instanceof ServletHttpSession){
			return ObjectUtils.nullSafeEquals(((ServletHttpSession) obj).httpSession, httpSession);
		}
		return false;
	}

	@Override
	public String toString() {
		return httpSession.toString();
	}
}
