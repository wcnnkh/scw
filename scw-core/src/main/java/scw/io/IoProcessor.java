package scw.io;

import java.io.IOException;

import scw.util.Processor;

@FunctionalInterface
public interface IoProcessor<S, T> extends Processor<S, T, IOException> {
	T process(S source) throws IOException;
}
