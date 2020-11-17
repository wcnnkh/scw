package scw.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class MultiIterable<E> implements Iterable<E> {
	private final Iterable<Iterable<? extends E>> iterables;
	
	public MultiIterable(Iterable<? extends E> ... iterables){
		this(Arrays.asList(iterables));
	}
	
	public MultiIterable(Iterable<Iterable<? extends E>> iterables) {
		this.iterables = iterables;
	}

	public Iterator<E> iterator() {
		return new InternalIterator();
	}

	private final class InternalIterator extends AbstractIterator<E> {
		private Iterator<Iterable<? extends E>> iterator = iterables == null ? null : iterables.iterator();
		private Iterator<? extends E> valueIterator;

		public boolean hasNext() {
			if (valueIterator != null && valueIterator.hasNext()) {
				return true;
			}

			while (iterator != null && iterator.hasNext()) {
				Iterable<? extends E> iterable = iterator.next();
				if(iterable == null){
					continue;
				}
				
				valueIterator = iterable.iterator();
				if (valueIterator.hasNext()) {
					return true;
				}
				valueIterator = null;
			}
			return false;
		}

		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			return valueIterator.next();
		}
	}
}
