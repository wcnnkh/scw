package scw.sql.orm.support;

import java.sql.SQLException;
import java.util.LinkedList;

import scw.sql.orm.ResultMapping;

public class DefaultResultSet extends AbstractResultSet {
	private static final long serialVersionUID = 1L;

	public DefaultResultSet(ResultSetResolver resultSetResolver, LinkedList<Object[]> dataList) {
		super(resultSetResolver, dataList);
	}

	public DefaultResultSet(java.sql.ResultSet resultSet) throws SQLException {
		super(resultSet);
	}

	protected ResultMapping createResultMapping(Object[] values) {
		return new DefaultResultMapping(resultSetResolver, values);
	}
}
