package scw.sql.orm;

import java.io.Serializable;
import java.util.List;

public interface ResultSet extends Serializable, Iterable<ResultMapping> {
	public static final EmptyResultSet EMPTY_RESULT_SET = new EmptyResultSet();

	/**
	 * 返回全部数据
	 * 
	 * @return
	 */
	List<Object[]> getList();

	<T> List<T> getList(Class<? extends T> clazz, TableNameMapping tableNameMapping);

	<T> List<T> getList(Class<? extends T> clazz, String tableName);

	<T> List<T> getList(Class<? extends T> clazz);

	int size();

	/**
	 * 获取第一个
	 * 
	 * @return
	 */
	ResultMapping getFirst();

	/**
	 * 获取第最后一个
	 * 
	 * @return
	 */
	ResultMapping getLast();

	boolean isEmpty();
	
	List<ResultMapping> toResultMappingList();
}