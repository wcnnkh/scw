package scw.security.login;

import scw.core.Assert;
import scw.core.utils.ObjectUtils;
import scw.core.utils.StringUtils;
import scw.data.TemporaryCache;
import scw.util.XUtils;

public abstract class AbstractLoginService<T> implements LoginService<T> {
	private final TemporaryCache temporaryCache;
	private final int exp;

	public AbstractLoginService(TemporaryCache temporaryCache, int exp) {
		this.temporaryCache = temporaryCache;
		this.exp = exp;
	}

	public TemporaryCache getTemporaryCache() {
		return temporaryCache;
	}

	public int getExp() {
		return exp;
	}

	protected String generatorToken(T uid) {
		return uid + XUtils.getUUID();
	}

	protected String formatUid(T uid) {
		return uid.toString();
	}

	public UserToken<T> getUserToken(String token) {
		if (StringUtils.isEmpty(token)) {
			return null;
		}

		T uid = temporaryCache.getAndTouch(token, exp);
		if (uid == null) {
			return null;
		}
		temporaryCache.touch(formatUid(uid), exp);
		return new UserToken<T>(token, uid);
	}

	public UserToken<T> getUserTokenByUid(T uid) {
		if (uid == null) {
			return null;
		}

		String token = temporaryCache.getAndTouch(formatUid(uid), exp);
		if (token == null) {
			return null;
		}

		temporaryCache.touch(token, exp);
		return new UserToken<T>(token, uid);
	}

	public boolean cancelLogin(String token) {
		if (token == null) {
			return false;
		}

		T uid = temporaryCache.get(token);
		if (uid != null) {
			temporaryCache.delete(formatUid(uid));
		}
		return temporaryCache.delete(token);
	}

	public boolean cancelLoginByUid(T uid) {
		if (uid == null) {
			return false;
		}

		String token = temporaryCache.get(formatUid(uid));
		if (token != null) {
			temporaryCache.delete(token);
		}
		return temporaryCache.delete(formatUid(uid));
	}

	public UserToken<T> login(T uid) {
		Assert.notNull(uid);

		String oldToken = temporaryCache.get(formatUid(uid));
		if(oldToken != null){
			temporaryCache.delete(oldToken);
		}
		
		String token = generatorToken(uid);
		temporaryCache.set(token, exp, uid);
		temporaryCache.set(formatUid(uid), exp, token);
		return new UserToken<T>(token, uid);
	}

	public boolean verification(String token, T uid) {
		if (uid == null || token == null) {
			return false;
		}

		UserToken<T> userToken = getUserToken(token);
		if (userToken == null) {
			return false;
		}

		return ObjectUtils.nullSafeEquals(uid, userToken.getUid());
	}
}
