package scw.db;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import scw.lang.ParameterException;
import scw.sql.Sql;
import scw.sql.orm.Column;
import scw.sql.orm.OrmUtils;
import scw.sql.orm.ResultMapping;
import scw.sql.orm.ResultSet;
import scw.sql.orm.dialect.DialectHelper;
import scw.sql.orm.dialect.DialectSql;
import scw.util.Pagination;

/**
 * 暂不支持分表
 * 
 * @author shuchaowen
 *
 */
@Deprecated
public abstract class Select extends DialectSql {
	private static final long serialVersionUID = 1L;
	private Map<String, String> associationWhereMap;
	private HashSet<String> selectTableSet;
	protected AbstractDB abstractDB;
	private DialectHelper dialectHelper;

	public Select(AbstractDB abstractDB, DialectHelper dialectHelper) {
		this.abstractDB = abstractDB;
		this.dialectHelper = dialectHelper;
	}

	public DialectHelper getDialectHelper() {
		return dialectHelper;
	}

	public Select from(Class<?> tableClass) {
		if (selectTableSet == null) {
			selectTableSet = new HashSet<String>();
		}

		selectTableSet.add(getTableName(tableClass));
		return this;
	}

	protected Map<String, String> getAssociationWhereMap() {
		return associationWhereMap;
	}

	public String getAssociationWhere() {
		if (associationWhereMap == null || associationWhereMap.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		Iterator<Entry<String, String>> iterator = associationWhereMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());

			if (iterator.hasNext()) {
				sb.append(" and ");
			}
		}
		return sb.toString();
	}

	public String getTableName(Class<?> tableClass) {
		return abstractDB.getTableName(tableClass, null);
	}

	protected void addSelectTable(String tableName) {
		if (selectTableSet == null) {
			selectTableSet = new HashSet<String>();
		}
		selectTableSet.add(tableName);
	}

	public String getSelectTables() {
		if (selectTableSet == null || selectTableSet.isEmpty()) {
			throw new NullPointerException("select tables");
		}

		StringBuilder sb = new StringBuilder();
		Iterator<String> iterator = selectTableSet.iterator();
		while (iterator.hasNext()) {
			sb.append("`");
			sb.append(iterator.next());
			sb.append("`");

			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public HashSet<String> getSelectTableSet() {
		return selectTableSet;
	}

	/**
	 * 检查两个字段在map是否存在相互引用的情况
	 * 
	 * @param whereMap
	 * @param name1
	 * @param name2
	 */
	private static boolean checkWhere(Map<String, String> whereMap, String name1, String name2) {
		if (whereMap.containsKey(name1)) {
			String v = whereMap.get(name1);
			if (name2.equals(v)) {
				return true;
			} else {
				return checkWhere(whereMap, v, name2);
			}
		} else if (whereMap.containsKey(name2)) {
			String v = whereMap.get(name2);
			if (name1.equals(v)) {
				return true;
			} else {
				return checkWhere(whereMap, v, name1);
			}
		}
		return false;
	}

	public AbstractDB getAbstractDB() {
		return abstractDB;
	}

	public abstract Sql toSQL(String select, boolean order);

	/**
	 * 把table2的指定字段和table1的主键关联
	 * 
	 * @param tableClass1
	 * @param tableClass2
	 * @param table2Columns
	 *            如果不填写就是两个表的主键关联
	 * @return
	 */
	public Select associationQuery(Class<?> tableClass1, Class<?> tableClass2, String... table2Columns) {
		if (associationWhereMap == null) {
			associationWhereMap = new HashMap<String, String>();
		}

		Collection<Column> t1 = OrmUtils.getObjectRelationalMapping().getColumns(tableClass1).getPrimaryKeys();
		Collection<Column> t2 = OrmUtils.getObjectRelationalMapping().getColumns(tableClass2).getPrimaryKeys();
		String tName1 = getTableName(tableClass1);
		String tName2 = getTableName(tableClass2);
		if (table2Columns == null || table2Columns.length == 0) {
			if (t1.size() != t2.size()) {
				// 两张表的主键数量不一致
				throw new ParameterException("primary key count atypism");
			}

			Iterator<Column> iterator1 = t1.iterator();
			Iterator<Column> iterator2 = t2.iterator();
			while (iterator1.hasNext() && iterator2.hasNext()) {
				Column c1 = iterator1.next();
				Column c2 = iterator2.next();
				String n1 = dialectHelper.getSqlName(tName1, c1.getName());
				String n2 = dialectHelper.getSqlName(tName2, c2.getName());
				if (checkWhere(associationWhereMap, n1, n2)) {
					continue;
				}

				associationWhereMap.put(n1, n2);
			}
		} else {
			if (table2Columns.length != t1.size()) {
				// 指明的外键和主键数量不一致
				throw new ParameterException("primary key count atypism");
			}

			Iterator<Column> iterator1 = t1.iterator();
			Iterator<Column> iterator2 = t2.iterator();
			while (iterator1.hasNext() && iterator2.hasNext())
				for (int i = 0; i < table2Columns.length; i++) {
					Column c1 = iterator1.next();
					Column c2 = iterator2.next();
					String n1 = dialectHelper.getSqlName(tName2, c2.getName());
					String n2 = dialectHelper.getSqlName(tName1, c1.getName());
					if (checkWhere(associationWhereMap, n1, n2)) {
						continue;
					}
					associationWhereMap.put(n1, n2);
				}
		}

		if (selectTableSet == null) {
			selectTableSet = new HashSet<String>();
		}

		selectTableSet.add(tName1);
		selectTableSet.add(tName2);
		return this;
	}

	public abstract Select whereAnd(String where, Collection<?> values);

	public Select whereAnd(String where, Object... value) {
		return whereAnd(where, Arrays.asList(value));
	}

	public abstract Select whereOr(String where, Collection<?> values);

	public Select whereOr(String where, Object... value) {
		return whereOr(where, Arrays.asList(value));
	}

	public abstract Select whereAndValue(Class<?> tableClass, String name, Object value);

	public abstract Select whereOrValue(Class<?> tableClass, String name, Object value);

	public Select whereAndIn(Class<?> tableClass, String name, Object... value) {
		return whereAndIn(tableClass, name, Arrays.asList(value));
	}

	public abstract Select whereAndIn(Class<?> tableClass, String name, Collection<?> values);

	public abstract Select whereOrIn(Class<?> tableClass, String name, Collection<?> values);

	public Select whereOrIn(Class<?> tableClass, String name, Object... value) {
		return whereOrIn(tableClass, name, Arrays.asList(value));
	}

	/**
	 * 降序
	 * 
	 * @param tableClass
	 * @param nameList
	 * @return
	 */
	public abstract Select desc(Class<?> tableClass, Collection<String> nameList);

	public Select desc(Class<?> tableClass, String... names) {
		return desc(tableClass, Arrays.asList(names));
	}

	/**
	 * 升序
	 * 
	 * @param tableClass
	 * @param nameList
	 * @return
	 */
	public abstract Select asc(Class<?> tableClass, Collection<String> nameList);

	public Select asc(Class<?> tableClass, String... names) {
		return asc(tableClass, Arrays.asList(names));
	}

	public abstract long count();

	public <T> T getFirst(Class<T> type) {
		return getResultSet().getFirst().get(type);
	}

	public abstract ResultSet getResultSet();

	public <T> List<T> getList(Class<T> type) {
		return getResultSet().getList(type);
	}

	public abstract ResultSet getResultSet(long begin, int limit);

	public <T> List<T> getList(Class<T> type, long begin, int limit) {
		return getResultSet(begin, limit).getList(type);
	}

	public Pagination<ResultMapping> getResultSetPagination(long page, int limit) {
		Pagination<ResultMapping> pagination = new Pagination<ResultMapping>(limit);
		if (page <= 0 || limit <= 0) {
			return pagination;
		}

		long count = count();
		if (count == 0) {
			return pagination;
		}

		pagination.setTotalCount(count);
		pagination.setData(getResultSet((page - 1) * limit, limit).toResultMappingList());
		return pagination;
	}

	public <T> Pagination<T> getPagination(Class<T> type, long page, int limit) {
		Pagination<T> pagination = new Pagination<T>(limit);
		if (page <= 0 || limit <= 0) {
			return pagination;
		}

		long count = count();
		if (count == 0) {
			return pagination;
		}

		pagination.setTotalCount(count);
		pagination.setData(getList(type, (page - 1) * limit, limit));
		return pagination;
	}
}
