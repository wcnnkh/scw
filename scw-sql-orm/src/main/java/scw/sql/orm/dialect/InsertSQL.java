package scw.sql.orm.dialect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import scw.sql.orm.Column;
import scw.sql.orm.OrmUtils;

public final class InsertSQL extends DialectSql {
	private static final long serialVersionUID = 1L;
	private String sql;
	private Object[] params;

	public InsertSQL(Class<?> clazz, String tableName,
			Object obj, DialectHelper dialectHelper) {
		StringBuilder cols = new StringBuilder();
		StringBuilder values = new StringBuilder();
		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();
		Iterator<Column> iterator = OrmUtils.getObjectRelationalMapping().getColumns(clazz).iterator();
		while (iterator.hasNext()) {
			Column column = iterator.next();
			if (column.isAutoIncrement()) {
				continue;
			}

			if (cols.length() > 0) {
				cols.append(",");
				values.append(",");
			}

			dialectHelper.keywordProcessing(cols, column.getName());
			values.append("?");
			params.add(column.get(obj));
		}
		sql.append(INSERT_INTO_PREFIX);
		dialectHelper.keywordProcessing(sql, tableName);
		sql.append("(");
		sql.append(cols);
		sql.append(VALUES);
		sql.append(values);
		sql.append(")");
		this.sql = sql.toString();
		this.params = params.toArray();
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParams() {
		return params;
	}
}
