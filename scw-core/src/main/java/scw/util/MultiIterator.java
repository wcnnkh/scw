package scw.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MultiIterator<E> extends AbstractIterator<E>{
	private final Iterator<Iterator<E>> iterators;
	private Iterator<E> valueIterator;
	
	public MultiIterator(Iterator<E> ... iterables){
		this(Arrays.asList(iterables));
	}
	
	public MultiIterator(Iterable<Iterator<E>> iterables) {
		this.iterators = iterables.iterator();
	}
	
	public boolean hasNext() {
		if (valueIterator != null && valueIterator.hasNext()) {
			return true;
		}

		while (iterators != null && iterators.hasNext()) {
			valueIterator = iterators.next();
			if(valueIterator != null && valueIterator.hasNext()){
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
