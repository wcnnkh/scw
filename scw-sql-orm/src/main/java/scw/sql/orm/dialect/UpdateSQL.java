package scw.sql.orm.dialect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import scw.sql.orm.Column;
import scw.sql.orm.Columns;
import scw.sql.orm.OrmUtils;
import scw.sql.orm.enums.CasType;

public class UpdateSQL extends DialectSql {
	private static final long serialVersionUID = 1L;
	private String sql;
	private Object[] params;

	public UpdateSQL(Class<?> clazz, Object obj, String tableName, DialectHelper dialectHelper) {
		Columns columns = OrmUtils.getObjectRelationalMapping().getColumns(clazz);
		Collection<Column> primaryKeys = columns.getPrimaryKeys();
		if (primaryKeys.size() == 0) {
			throw new NullPointerException(tableName + " not found primary key");
		}

		Collection<Column> notPrimaryKeys = columns.getNotPrimaryKeys();
		StringBuilder sb = new StringBuilder(512);
		sb.append(UPDATE_PREFIX);
		dialectHelper.keywordProcessing(sb, tableName);
		sb.append(SET);
		List<Object> params = new ArrayList<Object>(notPrimaryKeys.size());
		Iterator<Column> iterator = notPrimaryKeys.iterator();
		while (iterator.hasNext()) {
			Column column = iterator.next();
			dialectHelper.keywordProcessing(sb, column.getName());
			if (column.getCasType() == CasType.AUTO_INCREMENT) {
				sb.append("=");
				dialectHelper.keywordProcessing(sb, column.getName());
				sb.append("+1");
			} else {
				sb.append("=?");
				params.add(column.get(obj));
			}

			if (iterator.hasNext()) {
				sb.append(",");
			}
		}

		sb.append(WHERE);
		iterator = primaryKeys.iterator();
		while (iterator.hasNext()) {
			Column column = iterator.next();
			dialectHelper.keywordProcessing(sb, column.getName());
			sb.append("=?");
			params.add(column.get(obj));
			if (iterator.hasNext()) {
				sb.append(AND);
			}
		}

		iterator = notPrimaryKeys.iterator();
		while (iterator.hasNext()) {
			Column column = iterator.next();
			if (column.getCasType() == CasType.NOTHING) {
				continue;
			}

			sb.append(AND);
			dialectHelper.keywordProcessing(sb, column.getName());
			sb.append("=?");
			params.add(column.get(obj));
		}
		this.sql = sb.toString();
		this.params = params.toArray(new Object[params.size()]);
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParams() {
		return params;
	}
}
