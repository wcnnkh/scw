package scw.sql.orm;

/**
 * 表名映射
 * 
 * @author shuchaowen
 *
 */
public interface TableNameMapping {
	String getTableName(Class<?> clazz);
}
