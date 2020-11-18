package scw.data.generator;

import scw.data.Counter;

public final class CounterIdFactory implements IdFactory<Long> {
	private final Counter counter;

	public CounterIdFactory(Counter counter) {
		this.counter = counter;
	}

	public Long generator(String name) {
		return counter.incr(this.getClass().getName() + "#" + name, 1, 1);
	}

}
