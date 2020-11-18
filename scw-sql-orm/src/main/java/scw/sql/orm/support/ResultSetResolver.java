package scw.sql.orm.support;

import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import scw.lang.AlreadyExistsException;
import scw.lang.NotSupportedException;

public class ResultSetResolver implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 如果查询结果中未出现重名
	 */
	private HashMap<String, Integer> singleIndexMap;
	private boolean multiTable;// 是否是多表数据
	private HashMap<String, Map<String, Integer>> indexMap;
	private int columnCount;

	public ResultSetResolver(ResultSetMetaData resultSetMetaData) throws SQLException {
		indexMap = new HashMap<String, Map<String, Integer>>(4);
		singleIndexMap = new HashMap<String, Integer>();
		this.columnCount = resultSetMetaData.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			String labelName = resultSetMetaData.getColumnLabel(i);
			if (!multiTable) {
				if (singleIndexMap.containsKey(labelName)) {
					singleIndexMap = null;
					multiTable = true;
				} else {
					singleIndexMap.put(labelName, i - 1);
				}
			}

			String tableName = resultSetMetaData.getTableName(i);
			Map<String, Integer> map = indexMap.get(tableName);
			if (map == null) {
				map = new LinkedHashMap<String, Integer>();
				indexMap.put(tableName, map);
			}

			if (map.containsKey(labelName)) {
				throw new AlreadyExistsException(tableName + " field name [" + labelName + "]");
			}
			map.put(labelName, i - 1);
		}
	}

	/**
	 * 是否是多表数据
	 * @return
	 */
	public boolean isMultiTable() {
		return multiTable;
	}

	public Map<String, Integer> getSingleIndexMap() {
		return singleIndexMap;
	}

	public Map<String, Integer> getIndexMap(String tableName) {
		return indexMap.get(tableName);
	}

	public int getTableCount() {
		return indexMap.size();
	}

	public int getColumnCount() {
		return columnCount;
	}

	public int getIndex(String tableName, String name) {
		Map<String, Integer> map = indexMap.get(tableName);
		if (map == null) {
			return -1;
		}

		Integer index = map.get(name);
		return index == null ? -1 : index;
	}

	public int getSingleIndex(String name) {
		if (isMultiTable()) {
			throw new NotSupportedException("multi table");
		}

		Integer index = singleIndexMap.get(name);
		return index == null ? -1 : index;
	}

	public HashMap<String, Map<String, Integer>> getIndexMap() {
		return indexMap;
	}
}
