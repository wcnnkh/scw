package scw.sql.orm.dialect.mysql;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import scw.core.utils.StringUtils;
import scw.sql.orm.IndexInfo;
import scw.sql.orm.OrmUtils;
import scw.sql.orm.annotation.Table;
import scw.sql.orm.dialect.DialectHelper;
import scw.sql.orm.dialect.DialectSql;
import scw.sql.orm.dialect.SqlType;
import scw.sql.orm.enums.IndexMethod;
import scw.sql.orm.enums.IndexOrder;

public class CreateTableSql extends DialectSql {
	private static final long serialVersionUID = 1L;
	private String sql;

	protected void appendColumnType(StringBuilder sb, SqlType sqlType) {
		sb.append(sqlType.getName());
		if (sqlType.getLength() > 0) {
			sb.append("(").append(sqlType.getLength()).append(")");
		}
	}

	public CreateTableSql(Class<?> clazz, String tableName, DialectHelper dialectHelper) {
		StringBuilder sb = new StringBuilder();
		sb.append(dialectHelper.getCreateTablePrefix());
		sb.append(" ");
		dialectHelper.keywordProcessing(sb, tableName);
		sb.append(" (");

		Iterator<scw.sql.orm.Column> iterator = OrmUtils.getObjectRelationalMapping().getColumns(clazz).iterator();
		while (iterator.hasNext()) {
			scw.sql.orm.Column col = iterator.next();
			SqlType sqlType = col.getSqlType(OrmUtils.getSqlTypeFactory());
			dialectHelper.keywordProcessing(sb, col.getName());
			sb.append(" ");
			appendColumnType(sb, sqlType);
			sb.append(" ");

			if (!StringUtils.isEmpty(col.getCharsetName())) {
				sb.append("character set ").append(col.getCharsetName()).append(" ");
			}

			if (!col.isNullable()) {
				sb.append("not null ");
			}

			if (StringUtils.isNotEmpty(col.getDescription())) {
				sb.append(" comment \'").append(col.getDescription()).append("\'");
			}

			if (col.isAutoIncrement()) {
				sb.append(" AUTO_INCREMENT");
			}

			if (iterator.hasNext()) {
				sb.append(",");
			}
		}

		iterator = OrmUtils.getObjectRelationalMapping().getColumns(clazz).iterator();
		while (iterator.hasNext()) {
			scw.sql.orm.Column column = iterator.next();
			if (!column.isUnique()) {
				continue;
			}

			sb.append(",");
			sb.append("UNIQUE (");
			dialectHelper.keywordProcessing(sb, column.getName());
			sb.append(")");
		}

		for (Entry<IndexInfo, List<IndexInfo>> entry : OrmUtils.getObjectRelationalMapping().getIndexInfoMap(clazz)
				.entrySet()) {
			sb.append(",");
			if (entry.getKey().getMethod() != IndexMethod.DEFAULT) {
				sb.append(" ");
				sb.append(entry.getKey().getMethod().name());
			}

			sb.append(" INDEX");

			if (!StringUtils.isEmpty(entry.getKey().getName())) {
				sb.append(" ");
				dialectHelper.keywordProcessing(sb, entry.getKey().getName());
			}

			sb.append(" (");
			Iterator<IndexInfo> indexIterator = entry.getValue().iterator();
			while (indexIterator.hasNext()) {
				IndexInfo indexInfo = indexIterator.next();
				dialectHelper.keywordProcessing(sb, indexInfo.getColumn().getName());
				if (indexInfo.getLength() != -1) {
					sb.append("(");
					sb.append(indexInfo.getLength());
					sb.append(")");
				}

				if (indexInfo.getOrder() != IndexOrder.DEFAULT) {
					sb.append(" ");
					dialectHelper.keywordProcessing(sb, indexInfo.getOrder().name());
				}

				if (indexIterator.hasNext()) {
					sb.append(",");
				}
			}
			sb.append(")");
		}

		StringBuilder primaryKeySql = new StringBuilder();
		iterator = OrmUtils.getObjectRelationalMapping().getColumns(clazz).iterator();
		while (iterator.hasNext()) {
			scw.sql.orm.Column column = iterator.next();
			if (!column.isPrimaryKey()) {
				continue;
			}
			if (primaryKeySql.length() > 0) {
				primaryKeySql.append(",");
			}

			dialectHelper.keywordProcessing(primaryKeySql, column.getName());
		}

		if (primaryKeySql.length() > 0) {
			sb.append(",");
			sb.append("primary key (");
			sb.append(primaryKeySql);
			sb.append(")");
		}

		sb.append(")");
		Table table = clazz.getAnnotation(Table.class);
		if (table != null) {
			if (StringUtils.isNotEmpty(table.engine())) {
				sb.append(" ENGINE=").append(table.engine());
			}

			if (StringUtils.isNotEmpty(table.charset())) {
				sb.append(" CHARSET=").append(table.charset());
			}

			if (StringUtils.isNotEmpty(table.row_format())) {
				sb.append(" ROW_FORMAT=").append(table.row_format());
			}
		}

		if (table != null && !StringUtils.isEmpty(table.comment())) {
			sb.append(" comment=\'").append(table.comment()).append("\'");
		}
		this.sql = sb.toString();
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParams() {
		return null;
	}
}