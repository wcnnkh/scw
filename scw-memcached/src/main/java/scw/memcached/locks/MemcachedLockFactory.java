package scw.memcached.locks;

import java.util.concurrent.TimeUnit;

import scw.core.instance.annotation.Configuration;
import scw.locks.AbstractLockFactory;
import scw.locks.Lock;
import scw.locks.LockFactory;
import scw.memcached.Memcached;
import scw.util.XUtils;

@Configuration(order = Integer.MIN_VALUE + 1, value = LockFactory.class)
public final class MemcachedLockFactory extends AbstractLockFactory {
	private final Memcached memcached;

	public MemcachedLockFactory(Memcached memcached) {
		this.memcached = memcached;
	}

	public Lock getLock(String name, long timeout, TimeUnit timeUnit) {
		return new MemcachedLock(memcached, name, XUtils.getUUID(), (int) TimeUnit.SECONDS.convert(timeout, timeUnit));
	}
}
