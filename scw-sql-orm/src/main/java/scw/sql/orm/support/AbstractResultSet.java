package scw.sql.orm.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import scw.sql.SqlUtils;
import scw.sql.orm.ResultMapping;
import scw.sql.orm.ResultSet;
import scw.sql.orm.TableNameMapping;
import scw.util.AbstractIterator;

public abstract class AbstractResultSet implements ResultSet {
	private static final long serialVersionUID = 1L;
	protected ResultSetResolver resultSetResolver;
	protected List<Object[]> dataList;

	public AbstractResultSet(ResultSetResolver resultSetResolver,
			LinkedList<Object[]> dataList) {
		this.resultSetResolver = resultSetResolver;
		this.dataList = dataList;
	}

	public AbstractResultSet(java.sql.ResultSet resultSet) throws SQLException {
		while (resultSet.next()) {
			if (resultSetResolver == null) {// 第一次
				resultSetResolver = new ResultSetResolver(
						resultSet.getMetaData());
				dataList = new ArrayList<Object[]>();
			}

			dataList.add(SqlUtils.getRowValues(resultSet,
					resultSetResolver.getColumnCount()));
		}
	}

	public final ResultSetResolver getValueIndexMapping() {
		return resultSetResolver;
	}

	public final List<Object[]> getDataList() {
		return dataList;
	}

	@SuppressWarnings("unchecked")
	public final List<Object[]> getList() {
		if (isEmpty()) {
			return Collections.EMPTY_LIST;
		}

		return new ArrayList<Object[]>(dataList);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<? extends T> clazz,
			TableNameMapping tableNameMapping) {
		if (isEmpty()) {
			return Collections.EMPTY_LIST;
		}

		List<T> list = new ArrayList<T>(dataList.size());
		for (Object[] values : dataList) {
			ResultMapping resultMapping = createResultMapping(values);
			list.add(resultMapping.get(clazz, tableNameMapping));
		}
		return list;
	}

	public <T> List<T> getList(Class<? extends T> clazz, String tableName) {
		return getList(clazz, new SingleTableNameMapping(clazz, tableName));
	}

	public <T> List<T> getList(Class<? extends T> clazz) {
		return getList(clazz, (String) null);
	}

	protected abstract ResultMapping createResultMapping(Object[] values);

	public final int size() {
		return dataList == null ? 0 : dataList.size();
	}

	public final ResultMapping getFirst() {
		if (isEmpty()) {
			return ResultMapping.EMPTY_RESULT;
		}

		return createResultMapping(dataList.get(0));
	}

	public final ResultMapping getLast() {
		if (isEmpty()) {
			return ResultMapping.EMPTY_RESULT;
		}

		return createResultMapping(dataList.get(dataList.size()));
	}

	public final boolean isEmpty() {
		return resultSetResolver == null || dataList == null
				|| dataList.isEmpty();
	}

	public final Iterator<ResultMapping> iterator() {
		return new ResultIterator();
	}

	public List<ResultMapping> toResultMappingList() {
		if (isEmpty()) {
			return Collections.emptyList();
		}

		List<ResultMapping> list = new ArrayList<ResultMapping>();
		for (ResultMapping resultMapping : this) {
			list.add(resultMapping);
		}
		return list;
	}

	final class ResultIterator extends AbstractIterator<ResultMapping> {
		private Iterator<Object[]> iterator;

		public ResultIterator() {
			iterator = dataList.iterator();
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public ResultMapping next() {
			return createResultMapping(iterator.next());
		}
	}
}
