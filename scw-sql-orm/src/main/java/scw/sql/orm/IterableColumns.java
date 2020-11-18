package scw.sql.orm;

import java.util.Iterator;

import scw.mapper.Field;
import scw.util.AbstractIterator;

public abstract class IterableColumns extends AbstractColumns {
	private Iterable<Field> iterable;

	public IterableColumns(Iterable<Field> iterable) {
		this.iterable = iterable;
	}

	public Iterator<Column> iterator() {
		return new ColumnIterator();
	}

	protected abstract Column create(Field field);

	private final class ColumnIterator extends AbstractIterator<Column> {
		private Iterator<Field> iterator = iterable.iterator();

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public Column next() {
			return create(iterator.next());
		}
	}
}
