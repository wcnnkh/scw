package scw.data.generator;

import scw.data.Counter;

public final class CounterIdGenerator implements IdGenerator<Long> {
	private final Counter counter;
	private final String key;
	private final long initId;

	public CounterIdGenerator(Counter counter, String key, long initId) {
		this.key = key;
		this.counter = counter;
		this.initId = initId;
	}

	public Long next() {
		return counter.incr(key, 1, initId);
	}
}
