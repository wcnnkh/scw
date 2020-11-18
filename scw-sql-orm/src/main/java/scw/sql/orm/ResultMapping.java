package scw.sql.orm;

import java.io.Serializable;
import java.util.Map;

public interface ResultMapping extends Serializable {
	public static final EmptyResultMapping EMPTY_RESULT = new EmptyResultMapping();
	
	<T> T get(Class<? extends T> clazz, TableNameMapping tableNameMapping);

	<T> T get(Class<? extends T> clazz, String tableName);

	<T> T get(Class<? extends T> clazz);

	Map<String, Object> getTableValueMap(String tableName);

	Object[] getValues();

	<T> T get(Class<? extends T> type, int index);

	<T> T get(int index);

	int size();

	boolean isEmpty();
}
