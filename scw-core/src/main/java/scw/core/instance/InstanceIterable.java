package scw.core.instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import scw.util.AbstractIterator;

public final class InstanceIterable<E> implements Iterable<E> {
	private NoArgsInstanceFactory instanceFactory;
	private Iterable<String> names;
	private Object[] instanceArray;
	private String[] nameArray;
	private boolean optimization;

	public InstanceIterable(NoArgsInstanceFactory instanceFactory, Iterable<String> names) {
		this(instanceFactory, names, false);
	}

	/**
	 * @param instanceFactory
	 * @param names
	 * @param optimization
	 *            此参数并不表示一定可以优化，但表示推荐优化,优化会在实例化时消耗一定的性能，但会比不优化的迭代性能要好
	 */
	public InstanceIterable(NoArgsInstanceFactory instanceFactory, Iterable<String> names, boolean optimization) {
		this.instanceFactory = instanceFactory;
		this.names = names;
		this.optimization = optimization;
		if (optimization) {
			if (names != null) {
				List<E> list = new ArrayList<E>();
				List<String> nameList = new ArrayList<String>();
				for (String name : names) {
					if (instanceFactory.isSingleton(name) && instanceFactory.isInstance(name)) {
						E instance = instanceFactory.getInstance(name);
						list.add(instance);
						nameList.add(null);
					} else {
						this.optimization = false;
						list.add(null);
						nameList.add(name);
					}
				}
				instanceArray = list.toArray();
				nameArray = nameList.toArray(new String[0]);
			}
		}

		if (!optimization) {
			instanceArray = null;
			nameArray = null;
		}
	}

	/**
	 * 是否进行了优化
	 * 
	 * @return
	 */
	public boolean isOptimization() {
		return optimization;
	}

	public Iterator<E> iterator() {
		return optimization ? new CachingSingletonInstanceIterator() : new InstanceIterator();
	}

	private final class CachingSingletonInstanceIterator extends AbstractIterator<E> {
		private int index = 0;

		public boolean hasNext() {
			if (index >= nameArray.length) {
				return false;
			}

			Object instance = instanceArray[index];
			if (instance != null) {
				return true;
			}

			String name = nameArray[index];
			if (instanceFactory.isInstance(name)) {
				return true;
			}

			index++;
			return hasNext();
		}

		@SuppressWarnings("unchecked")
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			Object instance = instanceArray[index];
			if (instance == null) {
				instance = instanceFactory.getInstance(nameArray[index]);
			}

			index++;
			return (E) instance;
		}
	}

	private final class InstanceIterator extends AbstractIterator<E> {
		private Iterator<String> iterator = names == null ? null : names.iterator();
		private String name;

		public boolean hasNext() {
			if (iterator == null) {
				return false;
			}

			if (name != null) {
				return true;
			}

			while (iterator.hasNext()) {
				name = iterator.next();
				if (instanceFactory.isInstance(name)) {
					return true;
				}
				name = null;
			}
			return false;
		}

		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			E instance = instanceFactory.getInstance(name);
			name = null;
			return instance;
		}
	}
}
