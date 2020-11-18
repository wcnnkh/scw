package scw.sql.orm.dialect.mysql;

import scw.sql.orm.Column;
import scw.sql.orm.OrmUtils;
import scw.sql.orm.dialect.DialectHelper;
import scw.sql.orm.dialect.DialectSql;

public class MaxIdSql extends DialectSql {
	private static final long serialVersionUID = 1L;
	private String sql;

	public MaxIdSql(Class<?> clazz, String tableName, String idField, DialectHelper dialectHelper) {
		Column column = OrmUtils.getObjectRelationalMapping().getColumns(clazz).find(idField);
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		dialectHelper.keywordProcessing(sb, column.getName());
		sb.append(" from ");
		dialectHelper.keywordProcessing(sb, tableName);
		sb.append(" order by ");
		dialectHelper.keywordProcessing(sb, column.getName());
		sb.append(" desc");
		this.sql = sb.toString();
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParams() {
		return null;
	}
}
