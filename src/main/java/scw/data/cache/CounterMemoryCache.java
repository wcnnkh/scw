package scw.data.cache;

import java.util.concurrent.atomic.AtomicLong;

public class CounterMemoryCache extends AbstractMemoryCache {
	private AtomicLong value;

	public CounterMemoryCache(long initialValue) {
		this.value = new AtomicLong(initialValue);
	}

	public long incr(long delta, long initialValue) {
		long prev, v;
		do {
			prev = value.get();
			v = isExpire(System.currentTimeMillis()) ? initialValue : prev + delta;
		} while (!value.compareAndSet(prev, v));
		return v;
	}

	public long decr(long delta, long initialValue) {
		return incr(-delta, initialValue);
	}

	public Object get() {
		return value;
	}

	public void set(Object value) {
		this.value.set((Long) value);
	}

	public long incr(long delta) {
		if (isExpire(System.currentTimeMillis())) {
			return 0;
		}

		long prev, v;
		do {
			prev = value.get();
			if (isExpire(System.currentTimeMillis())) {
				return 0;
			}

			v = prev + delta;
		} while (!value.compareAndSet(prev, v));
		return v;
	}

	public long decr(long delta) {
		return incr(-delta);
	}
}
