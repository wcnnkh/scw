package scw.database;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface ResultSet extends Serializable {
	public static final ResultSet EMPTY_RESULTSET = new EmptyResultSet();

	Iterator<Result> iterator();
	
	/**
	 * 返回全部数据
	 * @return
	 */
	List<Object[]> getList();

	<T> List<T> getList(Class<T> type, Map<Class<?>, String> tableMapping);

	/**
	 * 一般用于分表对象
	 * 
	 * @param type
	 * @param tableName
	 * @return
	 */
	<T> List<T> getList(Class<T> type, String tableName);

	/**
	 * 默认的实现
	 * 
	 * @param type
	 * @return
	 */
	<T> List<T> getList(Class<T> type);

	int size();

	/**
	 * 获取第一个
	 * 
	 * @return
	 */
	Result getFirst();

	/**
	 * 获取第最后一个
	 * 
	 * @return
	 */
	Result getLast();

	boolean isEmpty();
}
