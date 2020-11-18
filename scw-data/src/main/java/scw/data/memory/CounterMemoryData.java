package scw.data.memory;

import java.util.concurrent.atomic.AtomicLong;

public class CounterMemoryData extends AbstractMemoryData {
	private AtomicLong value = new AtomicLong();

	public long incr(long delta, long initialValue) {
		long prev, v;
		do {
			prev = value.get();
			v = (isExpire() || prev == 0) ? initialValue : (prev + delta);
		} while (!value.compareAndSet(prev, v));
		if (v == initialValue) {
			touch();
		}

		cas.incrementAndGet();
		return v;
	}

	public long incr(long delta) {
		return incr(delta, 0);
	}

	public long decr(long delta, long initialValue) {
		return incr(-delta, initialValue);
	}

	@Override
	protected Object getValue() {
		return value.get();
	}

	@Override
	protected boolean setValue(Object value) {
		this.value.set((Long) value);
		return false;
	}

	public void set(Object value) {
		this.value.set((Long) value);
	}

	public long decr(long delta) {
		return incr(-delta);
	}
}
