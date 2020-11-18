package scw.sql.orm.dialect;

import java.util.Collection;
import java.util.Iterator;

import scw.sql.orm.Column;
import scw.sql.orm.OrmUtils;

public final class SelectByIdSQL extends DialectSql {
	private static final long serialVersionUID = 1L;
	private String sql;
	private Object[] params;

	public SelectByIdSQL(Class<?> clazz, String tableName,
			Collection<Object> ids, DialectHelper dialectHelper) {
		StringBuilder sb = new StringBuilder();
		sb.append(SELECT_ALL_PREFIX);
		dialectHelper.keywordProcessing(sb, tableName);
		Collection<Column> primaryKeys = OrmUtils.getObjectRelationalMapping().getColumns(clazz).getPrimaryKeys();
		Iterator<Column> iterator = primaryKeys.iterator();
		Iterator<Object> valueIterator = ids.iterator();
		if (iterator.hasNext() && valueIterator.hasNext()) {
			sb.append(WHERE);
		}

		this.params = new Object[ids.size()];
		int i = 0;
		while (iterator.hasNext() && valueIterator.hasNext()) {
			Column column = iterator.next();
			Object value = valueIterator.next();
			params[i++] = column.toDataBaseValue(value);

			dialectHelper.keywordProcessing(sb, column.getName());
			sb.append("=?");
			if (iterator.hasNext() && valueIterator.hasNext()) {
				sb.append(AND);
			}
		}
		this.sql = sb.toString();
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParams() {
		return params;
	}
}
