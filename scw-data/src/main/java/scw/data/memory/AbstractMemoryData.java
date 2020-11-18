package scw.data.memory;

import java.util.concurrent.atomic.AtomicLong;

import scw.data.cas.CAS;
import scw.io.SerializerUtils;

public abstract class AbstractMemoryData implements MemoryData {
	private volatile long lastTouch;
	private volatile int exp;
	protected AtomicLong cas = new AtomicLong();

	public AbstractMemoryData() {
		this.lastTouch = System.currentTimeMillis();
	}

	public void setExpire(int exp) {
		this.exp = exp;
	}

	public void touch() {
		this.lastTouch = System.currentTimeMillis();
	}

	public boolean isExpire() {
		return exp <= 0 ? false : (System.currentTimeMillis() - lastTouch) > exp * 1000;
	}

	public boolean setIfAbsent(Object value) {
		if (!isExpire()) {
			return false;
		}

		set(value);
		return true;
	}

	public boolean setIfAbsent(CAS<Object> value) {
		if (!isExpire()) {
			return false;
		}

		return set(value);
	}

	@SuppressWarnings("unchecked")
	public <T> CAS<T> get() {
		Object value = getValue();
		if (value == null) {
			return null;
		}

		return new CAS<T>(cas.get(), (T) SerializerUtils.clone(value));
	}

	public boolean incrCasAndCompare(long cas) {
		long c;
		long a;
		do {
			a = this.cas.get();
			c = isExpire() ? 0 : a;
		} while (!this.cas.compareAndSet(a, c + 1));
		if (c == 0) {
			touch();
		}
		return c == cas;
	}

	public boolean set(CAS<? extends Object> value) {
		if (incrCasAndCompare(value.getCas())) {
			return setValue(SerializerUtils.clone(value.getValue()));
		}
		return false;
	}

	public void set(Object value) {
		cas.incrementAndGet();
		setValue(SerializerUtils.clone(value));
	}

	protected abstract boolean setValue(Object value);

	protected abstract Object getValue();
}
