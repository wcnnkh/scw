package scw.db.sql;

import java.util.Collection;
import java.util.Map;

import scw.beans.BeanFieldListen;
import scw.common.Pagination;
import scw.common.exception.ShuChaoWenRuntimeException;
import scw.database.TableInfo;
import scw.db.sql.mysql.CreateTableSQL;
import scw.db.sql.mysql.DecrSQL;
import scw.db.sql.mysql.DeleteSQL;
import scw.db.sql.mysql.IncrSQL;
import scw.db.sql.mysql.InsertSQL;
import scw.db.sql.mysql.SaveOrUpdateSQL;
import scw.db.sql.mysql.SaveOrUpdateSQLByBeanListen;
import scw.db.sql.mysql.SelectByIdSQL;
import scw.db.sql.mysql.SelectInIdSQL;
import scw.db.sql.mysql.UpdateSQL;
import scw.db.sql.mysql.UpdateSQLByBeanListen;
import scw.jdbc.Sql;

public class MysqlFormat implements SqlFormat {
	public Sql toCreateTableSql(TableInfo tableInfo, String tableName) {
		return new CreateTableSQL(tableInfo, tableName);
	}

	public Sql toSelectByIdSql(TableInfo info, String tableName, Object[] ids) {
		return new SelectByIdSQL(info, tableName, ids);
	}

	public Sql toInsertSql(Object obj, TableInfo tableInfo, String tableName) {
		return new InsertSQL(tableInfo, tableName, obj);
	}

	public Sql toDeleteSql(Object obj, TableInfo tableInfo, String tableName) {
		return new DeleteSQL(obj, tableInfo, tableName);
	}

	public Sql toUpdateSql(Object obj, TableInfo tableInfo, String tableName) {
		try {
			if (obj instanceof BeanFieldListen) {
				return new UpdateSQLByBeanListen((BeanFieldListen) obj,
						tableInfo, tableName);
			} else {
				return new UpdateSQL(obj, tableInfo, tableName);
			}
		} catch (Exception e) {
			throw new ShuChaoWenRuntimeException(e);
		}
	}

	public Sql toSaveOrUpdateSql(Object obj, TableInfo tableInfo,
			String tableName) {
		try {
			if (obj instanceof BeanFieldListen) {
				return new SaveOrUpdateSQLByBeanListen((BeanFieldListen) obj,
						tableInfo, tableName);
			} else {
				return new SaveOrUpdateSQL(obj, tableInfo, tableName);
			}
		} catch (Exception e) {
			throw new ShuChaoWenRuntimeException(e);
		}

	}

	public Sql toIncrSql(Object obj, TableInfo tableInfo, String tableName,
			String fieldName, double limit, Double maxValue) {
		return new IncrSQL(obj, tableInfo, tableName, fieldName, limit,
				maxValue);
	}

	public Sql toDecrSql(Object obj, TableInfo tableInfo, String tableName,
			String fieldName, double limit, Double minValue) {
		try {
			return new DecrSQL(obj, tableInfo, tableName, fieldName, limit,
					minValue);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		throw new ShuChaoWenRuntimeException();
	}

	public Sql toDeleteSql(TableInfo tableInfo, String tableName,
			Object[] params) {
		return new DeleteSQL(tableInfo, tableName, params);
	}

	public Sql toUpdateSql(TableInfo tableInfo, String tableName,
			Map<String, Object> valueMap, Object[] params) {
		return new UpdateSQL(tableInfo, tableName, valueMap, params);
	}

	public PaginationSql toPaginationSql(Sql sql, long page, int limit) {
		String str = sql.getSql();
		int fromIndex = str.indexOf(" from ");// ignore select
		if (fromIndex == -1) {
			fromIndex = str.indexOf(" FROM ");
		}

		if (fromIndex == -1) {
			throw new IndexOutOfBoundsException(str);
		}

		String whereSql;
		int orderIndex = str.lastIndexOf(" order by ");
		if (orderIndex == -1) {
			orderIndex = str.lastIndexOf(" ORDER BY ");
		}

		if (orderIndex == -1) {// 不存在 order by 子语句
			whereSql = str.substring(fromIndex);
		} else {
			whereSql = str.substring(fromIndex, orderIndex);
		}

		Sql countSql = new SimpleSql("select count(*)" + whereSql,
				sql.getParams());
		StringBuilder sb = new StringBuilder(str);
		sb.append(" limit ").append(Pagination.getBegin(page, limit))
				.append(",").append(limit);
		return new PaginationSql(countSql, new SimpleSql(sb.toString(),
				sql.getParams()));
	}

	public Sql toSelectInIdSql(TableInfo tableInfo, String tableName,
			Object[] params, Collection<?> inIdList) {
		return new SelectInIdSQL(tableInfo, tableName, params, inIdList);
	}

	public Sql toCopyTableStructure(String newTableName, String oldTableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS `").append(newTableName)
				.append("`");
		sb.append(" like `").append(oldTableName).append("`");
		return new SimpleSql(sb.toString());
	}
}