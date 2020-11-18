package scw.sql.orm.dialect;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Year;

import scw.core.utils.TypeUtils;

public class DefaultSqlTypeFactory implements SqlTypeFactory {
	
	public SqlType getSqlType(Class<?> type) {
		if (TypeUtils.isString(type) || type.isEnum()) {
			return VARCHAR;
		} else if (TypeUtils.isBoolean(type)) {
			return BIT;
		} else if (TypeUtils.isByte(type)) {
			return TINYINT;
		} else if (TypeUtils.isShort(type)) {
			return SMALLINT;
		} else if (TypeUtils.isInt(type)) {
			return INTEGER;
		} else if (TypeUtils.isLong(type)) {
			return BIGINT;
		} else if (TypeUtils.isFloat(type)) {
			return FLOAT;
		} else if (TypeUtils.isDouble(type)) {
			return DOUBLE;
		} else if (Date.class.isAssignableFrom(type)) {
			return DATE;
		} else if (Timestamp.class.isAssignableFrom(type)) {
			return TIMESTAMP;
		} else if (Time.class.isAssignableFrom(type)) {
			return TIME;
		} else if (Year.class.isAssignableFrom(type)) {
			return YEAR;
		} else if (Blob.class.isAssignableFrom(type)) {
			return BLOB;
		} else if (Clob.class.isAssignableFrom(type)) {
			return CLOB;
		} else if (BigDecimal.class.isAssignableFrom(type)) {
			return NUMERIC;
		} else {
			return TEXT;
		}
	}

	public SqlType getSqlType(String sqlType) {
		if ("VARCHAR".equalsIgnoreCase(sqlType) || "string".equalsIgnoreCase(sqlType)) {
			return VARCHAR;
		} else if ("BIT".equalsIgnoreCase(sqlType)) {
			return BIT;
		} else if ("TINYINT".equalsIgnoreCase(sqlType)) {
			return TINYINT;
		} else if ("SMALLINT".equalsIgnoreCase(sqlType) || "short".equalsIgnoreCase(sqlType)) {
			return SMALLINT;
		} else if ("INTEGER".equalsIgnoreCase(sqlType) || "int".equalsIgnoreCase(sqlType)) {
			return INTEGER;
		} else if ("BIGINT".equalsIgnoreCase(sqlType)) {
			return BIGINT;
		} else if ("FLOAT".equalsIgnoreCase(sqlType)) {
			return FLOAT;
		} else if ("DOUBLE".equalsIgnoreCase(sqlType)) {
			return DOUBLE;
		} else if ("DATE".equalsIgnoreCase(sqlType)) {
			return DATE;
		} else if ("TIMESTAMP".equalsIgnoreCase(sqlType)) {
			return TIMESTAMP;
		} else if ("TIME".equalsIgnoreCase(sqlType)) {
			return TIME;
		} else if ("YEAR".equalsIgnoreCase(sqlType)) {
			return YEAR;
		} else if ("BLOB".equalsIgnoreCase(sqlType)) {
			return BLOB;
		} else if ("CLOB".equals(sqlType)) {
			return CLOB;
		} else if ("NUMERIC".equals(sqlType)) {
			return NUMERIC;
		} else {
			return new SqlType(sqlType, 0);
		}
	}
}
