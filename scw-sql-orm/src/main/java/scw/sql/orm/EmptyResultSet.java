package scw.sql.orm;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class EmptyResultSet implements ResultSet {
	private static final long serialVersionUID = 1L;

	public Iterator<ResultMapping> iterator() {
		return Collections.emptyIterator();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getList() {
		return Collections.EMPTY_LIST;
	}

	public <T> List<T> getList(Class<? extends T> clazz, TableNameMapping tableNameMapping) {
		return null;
	}

	public <T> List<T> getList(Class<? extends T> clazz, String tableName) {
		return null;
	}

	public <T> List<T> getList(Class<? extends T> clazz) {
		return null;
	}

	public int size() {
		return 0;
	}

	public ResultMapping getFirst() {
		return null;
	}

	public ResultMapping getLast() {
		return null;
	}

	public boolean isEmpty() {
		return true;
	}

	public List<ResultMapping> toResultMappingList() {
		return Collections.emptyList();
	}
}
