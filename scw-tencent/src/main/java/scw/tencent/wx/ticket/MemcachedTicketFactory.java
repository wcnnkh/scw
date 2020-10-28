package scw.tencent.wx.ticket;

import scw.core.parameter.annotation.ParameterName;
import scw.locks.Lock;
import scw.locks.LockFactory;
import scw.memcached.Memcached;
import scw.memcached.locks.MemcachedLockFactory;
import scw.security.Token;
import scw.tencent.wx.WeiXinUtils;
import scw.tencent.wx.token.AccessTokenFactory;
import scw.tencent.wx.token.MemcachedAccessTokenFactory;

public final class MemcachedTicketFactory extends AbstractTicketFactory {
	private final Memcached memcached;
	private final String key;
	private final String lockKey;
	private final LockFactory lockFactory;

	public MemcachedTicketFactory(Memcached memcached, @ParameterName(WX_APPID_KEY) String appid,
			@ParameterName(WX_APPSECRET_KEY) String appsecret) {
		this(memcached, appid, appsecret, "jsapi");
	}

	public MemcachedTicketFactory(Memcached memcached, @ParameterName(WX_APPID_KEY) String appid,
			@ParameterName(WX_APPSECRET_KEY) String appsecret, @ParameterName(WX_TICKET_TYPE) String type) {
		this(memcached, appid, new MemcachedAccessTokenFactory(memcached, appid, appsecret), type);
	}

	public MemcachedTicketFactory(Memcached memcached, String key, AccessTokenFactory accessTokenFactory) {
		this(memcached, key, accessTokenFactory, "jsapi");
	}

	public MemcachedTicketFactory(Memcached memcached, String key, AccessTokenFactory accessTokenFactory, String type) {
		super(accessTokenFactory, type);
		this.memcached = memcached;
		this.key = "wx_ticket:#" + type + "#" + key;
		this.lockKey = "wx_ticket:#lock#" + type + "#" + key;
		this.lockFactory = new MemcachedLockFactory(memcached);
	}

	@Override
	protected Token getJsApiTicketByCache() {
		return memcached.get(key);
	}

	@Override
	protected Token refreshJsApiTicket() {
		if (!isExpired()) {
			return getJsApiTicketByCache();
		}

		Lock lock = lockFactory.getLock(lockKey);
		if (lock.tryLock()) {
			try {
				if (isExpired()) {
					Token ticket = WeiXinUtils.getTicket(getAccessToken(), getType());
					memcached.set(key, ticket.getExpiresIn(), ticket.clone());
					return ticket;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}

		// 没有拿到锁
		try {
			Thread.sleep(50L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return refreshJsApiTicket();
	}
}
