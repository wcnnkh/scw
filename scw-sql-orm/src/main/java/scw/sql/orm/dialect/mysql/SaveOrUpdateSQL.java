package scw.sql.orm.dialect.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.sql.orm.Column;
import scw.sql.orm.CounterInfo;
import scw.sql.orm.OrmUtils;
import scw.sql.orm.dialect.DialectHelper;
import scw.sql.orm.dialect.DialectSql;

public class SaveOrUpdateSQL extends DialectSql {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerUtils.getLogger(SaveOrUpdateSQL.class);
	private static final String DUPLICATE_KEY = " ON DUPLICATE KEY UPDATE ";
	private static final String IF = "IF(";
	private String sql;
	private Object[] params;

	public SaveOrUpdateSQL(Class<?> clazz, Object obj, String tableName, DialectHelper dialectHelper) {
		Collection<Column> primaryKeys = OrmUtils.getObjectRelationalMapping().getColumns(clazz).getColumns();
		if (primaryKeys.size() == 0) {
			throw new NullPointerException("not found primary key");
		}

		StringBuilder sb = new StringBuilder(512);
		StringBuilder cols = new StringBuilder();
		StringBuilder values = new StringBuilder();
		List<Object> params = new ArrayList<Object>();
		Iterator<Column> iterator = OrmUtils.getObjectRelationalMapping().getColumns(clazz).iterator();
		while (iterator.hasNext()) {
			Column column = iterator.next();
			dialectHelper.keywordProcessing(cols, column.getName());
			values.append("?");
			params.add(column.get(obj));

			if (iterator.hasNext()) {
				cols.append(",");
				values.append(",");
			}
		}

		sb.append(INSERT_INTO_PREFIX);
		dialectHelper.keywordProcessing(sb, tableName);
		sb.append("(");
		sb.append(cols);
		sb.append(VALUES);
		sb.append(values);
		sb.append(")");
		sb.append(DUPLICATE_KEY);
		
		iterator = OrmUtils.getObjectRelationalMapping().getColumns(clazz).iterator();
		while (iterator.hasNext()) {
			Column column = iterator.next();
			Object v = column.get(obj);
			CounterInfo counterInfo = column.getCounterInfo();
			if (counterInfo == null) {
				dialectHelper.keywordProcessing(sb, column.getName());
				sb.append("=?");
				params.add(v);
			} else {
				if (v == null) {
					logger.warn("{}中计数器字段{}的值为空", clazz.getName(), column.getName());
					dialectHelper.keywordProcessing(sb, column.getName());
					sb.append("=?");
					params.add(v);
				} else {
					dialectHelper.keywordProcessing(sb, column.getName());
					sb.append("=");
					sb.append(IF);
					dialectHelper.keywordProcessing(sb, column.getName());
					sb.append("+").append(v);
					sb.append(">=").append(counterInfo.getMin());
					sb.append(AND);
					dialectHelper.keywordProcessing(sb, column.getName());
					sb.append("+").append(v);
					sb.append("<=").append(counterInfo.getMax());
					sb.append(",");
					dialectHelper.keywordProcessing(sb, column.getName());
					sb.append("+?");
					params.add(v);
					sb.append(",");
					dialectHelper.keywordProcessing(sb, column.getName());
					sb.append(")");
				}
			}

			if (iterator.hasNext()) {
				sb.append(",");
			}
		}

		this.sql = sb.toString();
		this.params = params.toArray();
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParams() {
		return params;
	}
}
