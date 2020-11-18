package scw.sql.orm.support;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import scw.aop.support.FieldSetterListen;
import scw.mapper.Field;
import scw.sql.SqlUtils;
import scw.sql.orm.ObjectRelationalMapping;
import scw.sql.orm.OrmUtils;
import scw.sql.orm.ResultMapping;
import scw.sql.orm.TableNameMapping;
import scw.value.ValueUtils;

public abstract class AbstractResultMapping implements ResultMapping {
	private static final long serialVersionUID = 1L;
	protected final ResultSetResolver resultSetResolver;
	protected final Object[] values;

	public AbstractResultMapping(ResultSetResolver resultSetResolver, Object[] values) {
		this.resultSetResolver = resultSetResolver;
		this.values = values;
	}

	public AbstractResultMapping(ResultSet resultSet) throws SQLException {
		this.resultSetResolver = new ResultSetResolver(resultSet.getMetaData());
		this.values = SqlUtils.getRowValues(resultSet, resultSetResolver.getColumnCount());
	}

	@SuppressWarnings("unchecked")
	public final <T> T get(Class<? extends T> clazz, TableNameMapping tableNameMapping) {
		if (isEmpty()) {
			return null;
		}

		if (ValueUtils.isBaseType(clazz)) {
			return get(clazz, 0);
		}

		if (clazz.isArray()) {
			Object array = Array.newInstance(clazz.getComponentType(), size());
			for (int i = 0; i < size(); i++) {
				Array.set(array, i, get(clazz.getComponentType(), i));
			}
			return (T) array;
		}

		T v = mapping(clazz, tableNameMapping, OrmUtils.getObjectRelationalMapping(), null);
		if (v instanceof FieldSetterListen) {
			((FieldSetterListen) v).clear_field_setter_listen();
		}
		return v;
	}

	protected abstract <T> T mapping(Class<T> clazz, TableNameMapping tableNameMapping, ObjectRelationalMapping objectRelationalMapping, Field parentField);

	public final <T> T get(Class<? extends T> clazz, String tableName) {
		return get(clazz, new SingleTableNameMapping(clazz, tableName));
	}

	public final <T> T get(Class<? extends T> clazz) {
		return get(clazz, (String) null);
	}

	public final Map<String, Object> getTableValueMap(String tableName) {
		Map<String, Integer> indexMap = resultSetResolver.getIndexMap(tableName);
		if (indexMap == null) {
			return null;
		}

		Map<String, Object> valueMap = new LinkedHashMap<String, Object>(indexMap.size());
		for (Entry<String, Integer> entry : indexMap.entrySet()) {
			valueMap.put(entry.getKey(), values[entry.getValue()]);
		}
		return valueMap;
	}

	public final Object[] getValues() {
		if (values == null) {
			return null;
		}

		Object[] dest = new Object[values.length];
		System.arraycopy(values, 0, dest, 0, dest.length);
		return dest;
	}

	@SuppressWarnings("unchecked")
	public final <T> T get(int index) {
		if (values == null) {
			return null;
		}

		return (T) values[index];
	}

	public final int size() {
		return values == null ? 0 : values.length;
	}

	public final boolean isEmpty() {
		return resultSetResolver == null || values == null || values.length == 0;
	}
}
