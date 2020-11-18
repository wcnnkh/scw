package scw.sql.orm.dialect;

public interface SqlTypeFactory {
	public static final SqlType VARCHAR = new SqlType("VARCHAR", 255);
	public static final SqlType BIT = new SqlType("BIT", 1);
	public static final SqlType TINYINT = new SqlType("TINYINT", 2);
	public static final SqlType SMALLINT = new SqlType("SMALLINT", 5);
	public static final SqlType INTEGER = new SqlType("INTEGER", 10);
	public static final SqlType BIGINT = new SqlType("BIGINT", 20);
	public static final SqlType FLOAT = new SqlType("FLOAT", 10);
	public static final SqlType DOUBLE = new SqlType("DOUBLE", 20);
	public static final SqlType DATE = new SqlType("DATE", 0);
	public static final SqlType TIMESTAMP = new SqlType("TIMESTAMP", 0);
	public static final SqlType TIME = new SqlType("TIME", 0);
	public static final SqlType YEAR = new SqlType("YEAR", 0);
	public static final SqlType BLOB = new SqlType("BLOB", 0);
	public static final SqlType CLOB = new SqlType("CLOB", 0);
	public static final SqlType NUMERIC = new SqlType("NUMERIC", 0);
	public static final SqlType TEXT = new SqlType("TEXT", 0);
	
	SqlType getSqlType(Class<?> type);
	
	SqlType getSqlType(String sqlType);
}
