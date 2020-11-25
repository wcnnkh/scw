package scw.security.session;

import java.util.ArrayList;

public class DefaultUserSessions<T> extends ArrayList<Session> implements UserSessions<T>{
	private static final long serialVersionUID = 1L;
	private final T uid;
	
	public DefaultUserSessions(T uid){
		this.uid = uid;
	}
	
	public T getUid() {
		return uid;
	}

	public Session getSession(String sessionId) {
		for(Session session : this){
			if(sessionId.equals(session.getId())){
				return session;
			}
		}
		return null;
	}

}
