package scw.sql.orm.dialect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import scw.aop.support.FieldSetterListen;
import scw.sql.SimpleSql;
import scw.sql.Sql;
import scw.util.Pagination;

public abstract class AbstractSqlDialect implements SqlDialect {
	private DialectHelper dialectHelper;

	public AbstractSqlDialect(DialectHelper dialectHelper) {
		this.dialectHelper = dialectHelper;
	}

	public DialectHelper getDialectHelper() {
		return dialectHelper;
	}

	public PaginationSql toPaginationSql(Sql sql, long page, int limit) throws SqlDialectException {
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

		Sql countSql = new SimpleSql("select count(*)" + whereSql, sql.getParams());
		StringBuilder sb = new StringBuilder(str);
		sb.append(" limit ").append(Pagination.getBegin(page, limit)).append(",").append(limit);
		return new PaginationSql(countSql, new SimpleSql(sb.toString(), sql.getParams()));
	}

	public Sql toInsertSql(Object obj, Class<?> clazz, String tableName) throws SqlDialectException {
		return new InsertSQL(clazz, tableName, obj, getDialectHelper());
	}

	public Sql toUpdateSql(Object obj, Class<?> clazz, String tableName) throws SqlDialectException {
		return (obj instanceof FieldSetterListen)
				? new UpdateSQLByBeanListen(clazz, (FieldSetterListen) obj, tableName, getDialectHelper())
				: new UpdateSQL(clazz, obj, tableName, getDialectHelper());
	}

	public Sql toDeleteSql(Object obj, Class<?> clazz, String tableName) throws SqlDialectException {
		return new DeleteSQL(clazz, obj, tableName, getDialectHelper());
	}

	public Sql toDeleteByIdSql(Class<?> clazz, String tableName, Object[] parimayKeys) throws SqlDialectException {
		return new DeleteByIdSql(clazz, tableName, parimayKeys, getDialectHelper());
	}

	@SuppressWarnings("unchecked")
	public Sql toSelectByIdSql(Class<?> clazz, String tableName, Object[] params) throws SqlDialectException {
		return new SelectByIdSQL(clazz, tableName, params == null ? Collections.EMPTY_LIST : Arrays.asList(params),
				getDialectHelper());
	}

	public Sql toSelectInIdSql(Class<?> clazz, String tableName, Object[] ids, Collection<?> inIdList)
			throws SqlDialectException {
		return new SelectInIdSQL(clazz, tableName, ids, inIdList, getDialectHelper());
	}
}
