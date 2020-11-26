package scw.security.session;

import java.util.HashSet;

import scw.core.annotation.Order;
import scw.core.instance.annotation.Configuration;
import scw.data.TemporaryCache;
import scw.data.memory.MemoryDataTemplete;
import scw.logger.Logger;
import scw.logger.LoggerFactory;

@Configuration(order = Integer.MIN_VALUE)
public final class DefaultUserSessionFactory<T> implements UserSessionFactory<T> {
	private static Logger logger = LoggerFactory.getLogger(DefaultUserSessionFactory.class);
	private final TemporaryCache temporaryCache;
	private final SessionFactory sessionFactory;
	
	public DefaultUserSessionFactory(){
		this(new MemoryDataTemplete());
		logger.info("Using memory {}", getTemporaryCache());
	}
	
	public DefaultUserSessionFactory(TemporaryCache temporaryCache) {
		this(86400 * 7, temporaryCache);
	}

	@Order
	public DefaultUserSessionFactory(int maxInactiveInterval,
			TemporaryCache temporaryCache) {
		this(temporaryCache, new DefaultSessionFactory(maxInactiveInterval, temporaryCache));
	}
	
	
	public DefaultUserSessionFactory(TemporaryCache temporaryCache, SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
		this.temporaryCache = temporaryCache;
	}
	
	public TemporaryCache getTemporaryCache() {
		return temporaryCache;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public UserSessions<T> getUserSessions(T uid) {
		DefaultUserSessions<T> userSessions = new DefaultUserSessions<T>(uid);
		SessionIds<T> sessionIds = getSessionIds(uid, false);
		if(sessionIds != null){
			for(String id : sessionIds){
				Session session = getUserSession(uid, id);
				if(session == null){
					continue;
				}
				
				userSessions.add(session);
			}
		}
		return userSessions;
	}
	
	private String getUidToSessionIdsKey(T uid){
		return "user-session-ids:" + uid;
	}
	
	public SessionIds<T> getSessionIds(T uid, boolean create){
		String key = getUidToSessionIdsKey(uid);
		SessionIds<T> ids = temporaryCache.getAndTouch(key, sessionFactory.getMaxInactiveInterval());
		if(ids == null){
			if(create){
				while(true){
					ids = temporaryCache.getAndTouch(key, sessionFactory.getMaxInactiveInterval());
					if(ids != null){
						break;
					}
					
					ids = new SessionIds<T>(sessionFactory.getMaxInactiveInterval());
					if(temporaryCache.add(key, sessionFactory.getMaxInactiveInterval(), ids)){
						break;
					}
				}
			}
		}
		return ids;
	}
	
	public void updateSessionIds(T uid, SessionIds<T> sessionIds){
		String key = getUidToSessionIdsKey(uid);
		temporaryCache.set(key, sessionIds.getMaxInactiveInterval(), sessionIds);
	}
	
	protected void createUserSession(T uid, String sessionId){
		String key = getUidToSessionIdsKey(uid);
		SessionIds<T> ids = temporaryCache.get(key);
		if(ids == null){
			ids = new SessionIds<T>(sessionFactory.getMaxInactiveInterval());
			ids.add(sessionId);
			if(!temporaryCache.add(key, sessionFactory.getMaxInactiveInterval(), ids)){
				createUserSession(uid, sessionId);
			}
		}else{
			ids.add(sessionId);
			temporaryCache.set(key, ids);
		}
	}
	
	public UserSession<T> getUserSession(T uid, String sessionId) {
		return getUserSession(uid, sessionId, false);
	}
	
	public UserSession<T> getUserSession(T uid, String sessionId, boolean create) {
		Session session = sessionFactory.getSession(uid + ":" + sessionId, create);
		if(session == null){
			return null;
		}
		
		if(session.isNew()){
			SessionIds<T> sessionIds = getSessionIds(uid, true);
			sessionIds.add(sessionId);
			updateSessionIds(uid, sessionIds);
		}
		return new InternalUserSession(uid, sessionId, session);
	}
	
	private static final class SessionIds<T> extends HashSet<String>{
		private static final long serialVersionUID = 1L;
		private int maxInactiveInterval;
		
		public SessionIds(int maxInactiveInterval){
			this.maxInactiveInterval = maxInactiveInterval;
		}
		
		public int getMaxInactiveInterval() {
			return maxInactiveInterval;
		}
		public void setMaxInactiveInterval(int maxInactiveInterval) {
			this.maxInactiveInterval = maxInactiveInterval;
		}
	}
	
	private final class InternalUserSession extends SessionWrapper implements UserSession<T>{
		private final String dispalySessionId;
		private final T uid;
		
		public InternalUserSession(T uid, String dispalySessionId, Session session){
			super(session);
			this.dispalySessionId = dispalySessionId;
			this.uid = uid;
		}
		
		@Override
		public String getId() {
			return dispalySessionId;
		}
		
		@Override
		public void setMaxInactiveInterval(int maxInactiveInterval) {
			SessionIds<T> sessionIds = getSessionIds(uid, false);
			if(sessionIds != null && maxInactiveInterval > sessionIds.getMaxInactiveInterval()){
				sessionIds.setMaxInactiveInterval(maxInactiveInterval);
				updateSessionIds(uid, sessionIds);
			}
			super.setMaxInactiveInterval(maxInactiveInterval);
		}
		
		@Override
		public void invalidate() {
			SessionIds<T> sessionIds = getSessionIds(uid, false);
			if(sessionIds != null){
				sessionIds.remove(getId());
				updateSessionIds(uid, sessionIds);
			}
			super.invalidate();
		}

		public T getUid() {
			return uid;
		}
	}
}
