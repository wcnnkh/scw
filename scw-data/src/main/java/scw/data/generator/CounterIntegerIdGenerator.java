package scw.data.generator;

import scw.data.Counter;

public final class CounterIntegerIdGenerator implements IdGenerator<Integer> {
	private final Counter counter;
	private final String key;
	private final int initId;

	public CounterIntegerIdGenerator(Counter counter, String key, int initId) {
		this.key = key;
		this.counter = counter;
		this.initId = initId;
	}

	public Integer next() {
		return (int) counter.incr(key, 1, initId);
	}
}
