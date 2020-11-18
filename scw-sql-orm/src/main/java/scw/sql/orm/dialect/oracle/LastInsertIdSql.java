package scw.sql.orm.dialect.oracle;

import scw.sql.Sql;

public final class LastInsertIdSql implements Sql {
	private static final long serialVersionUID = 1L;
	private String sql;

	public LastInsertIdSql(String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("select seq.currval from `").append(tableName).append("`");
		this.sql = sb.toString();
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParams() {
		return null;
	}

	public boolean isStoredProcedure() {
		return false;
	}

}
