package scw.data.memory;

import scw.lang.NotSupportedException;

public class DefaultMemoryData extends AbstractMemoryData {
	private volatile Object value;

	public long incr(long incr, long initialValue) {
		throw new NotSupportedException("incr");
	}

	public long decr(long incr, long initialValue) {
		throw new NotSupportedException("decr");
	}

	public long incr(long delta) {
		throw new NotSupportedException("incr");
	}

	public long decr(long delta) {
		throw new NotSupportedException("decr");
	}

	@Override
	protected boolean setValue(Object value) {
		this.value = value;
		return true;
	}

	@Override
	protected Object getValue() {
		return value;
	}
}
