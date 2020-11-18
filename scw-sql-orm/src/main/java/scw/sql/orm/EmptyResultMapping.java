package scw.sql.orm;

import java.util.Collections;
import java.util.Map;

public final class EmptyResultMapping implements ResultMapping {
	private static final long serialVersionUID = 1L;

	public Object[] getValues() {
		return new Object[0];
	}

	public <T> T get(int index) {
		return null;
	}

	public int size() {
		return 0;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getValueMap(String tableName) {
		return Collections.EMPTY_MAP;
	}

	public boolean isEmpty() {
		return true;
	}

	@Override
	public Object clone() {
		return this;
	}

	public <T> T get(Class<? extends T> clazz, String tableName) {
		return null;
	}

	public <T> T get(Class<? extends T> clazz) {
		return null;
	}

	public Map<String, Object> getTableValueMap(String tableName) {
		return null;
	}

	public <T> T get(Class<? extends T> clazz, TableNameMapping tableNameMapping) {
		return null;
	}

	public <T> T get(Class<? extends T> type, int index) {
		return null;
	}

	public long getRowNum() {
		return 0;
	}
}
