package scw.sql.orm;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.util.Date;

import scw.core.utils.StringUtils;
import scw.core.utils.TypeUtils;
import scw.json.JSONUtils;
import scw.mapper.Field;
import scw.sql.SqlUtils;
import scw.sql.orm.annotation.AutoIncrement;
import scw.sql.orm.annotation.Counter;
import scw.sql.orm.annotation.ForceUpdate;
import scw.sql.orm.annotation.Index;
import scw.sql.orm.annotation.PrimaryKey;
import scw.sql.orm.annotation.Table;
import scw.sql.orm.dialect.SqlType;
import scw.sql.orm.dialect.SqlTypeFactory;
import scw.sql.orm.enums.CasType;
import scw.value.AnyValue;
import scw.value.Value;

/**
 * 名称相同视为同一字段
 * 
 * @author shuchaowen
 *
 */
public class Column implements Serializable {
	private static final long serialVersionUID = 1L;
	private Field field;

	public Column(Field field) {
		this.field = field;
	}

	public final Field getField() {
		return field;
	}

	public boolean isEntity() {
		return field.getGetter().getType().getAnnotation(Table.class) != null;
	}

	public boolean isPrimaryKey() {
		return field.getAnnotatedElement().getAnnotation(PrimaryKey.class) != null;
	}

	public String getName() {
		scw.sql.orm.annotation.Column column = getColumn();
		if (column != null && !StringUtils.isEmpty(column.name())) {
			return column.name();
		}
		return getField().getGetter().getName();
	}

	/**
	 * 将值转换为对象的类型
	 * 
	 * @param value
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object toColumnValue(Object value) {
		if (value == null) {
			return value;
		}

		Class<?> type = field.getSetter().getType();
		if (type.isInstance(value)) {
			return value;
		} else if (type == Value.class) {
			return new AnyValue(value);
		} else if (TypeUtils.isBoolean(type)) {
			if (value instanceof Number) {
				return ((Number) value).intValue() == 1;
			} else {
				return StringUtils.parseBoolean(value.toString());
			}
		} else if (TypeUtils.isInt(type)) {
			if (value instanceof Number) {
				return ((Number) value).intValue();
			} else {
				return StringUtils.parseInt(value.toString());
			}
		} else if (TypeUtils.isLong(type)) {
			if (value instanceof Number) {
				return ((Number) value).longValue();
			} else {
				return StringUtils.parseLong(value.toString());
			}
		} else if (TypeUtils.isByte(type)) {
			if (value instanceof Number) {
				return ((Number) value).byteValue();
			} else {
				return StringUtils.parseByte(value.toString());
			}
		} else if (TypeUtils.isFloat(type)) {
			if (value instanceof Number) {
				return ((Number) value).floatValue();
			} else {
				return StringUtils.parseFloat(value.toString());
			}
		} else if (TypeUtils.isDouble(type)) {
			if (value instanceof Number) {
				return ((Number) value).doubleValue();
			} else {
				return StringUtils.parseDouble(value.toString());
			}
		} else if (TypeUtils.isShort(type)) {
			if (value instanceof Number) {
				return ((Number) value).shortValue();
			} else {
				return StringUtils.parseShort(value.toString());
			}
		} else if (type.isEnum()) {
			return Enum.valueOf((Class<? extends Enum>) type, value.toString());
		} else {
			return JSONUtils.parseObject(value.toString(), field.getSetter().getGenericType());
		}
	}

	/**
	 * 将数据库的值插入到对象
	 * 
	 * @param entity
	 * @param value
	 */
	public final void set(Object entity, Object value) {
		if (value == null) {
			return;
		}

		Object v = toColumnValue(value);
		if (v == null) {
			return;
		}

		field.getSetter().set(entity, v);
	}

	/**
	 * 获取插入到数据库的值
	 * 
	 * @param entity
	 * @return
	 */
	public final Object get(Object entity) {
		Object value = field.getGetter().get(entity);
		return value == null ? null : toDataBaseValue(value);
	}

	/**
	 * 将值转换成数据库类型
	 * 
	 * @param value
	 * @return
	 */
	public Object toDataBaseValue(Object value) {
		if (value != null && value instanceof Enum) {
			return ((Enum<?>) value).name();
		}

		Class<?> type = field.getGetter().getType();
		if (boolean.class == type) {
			boolean b = value == null ? false : (Boolean) value;
			return b ? 1 : 0;
		}

		if (Boolean.class == type) {
			if (value == null) {
				return null;
			}
			return (Boolean) value ? 1 : 0;
		}

		if (scw.value.Value.class.isAssignableFrom(type)) {
			if (value instanceof scw.value.Value) {
				return ((scw.value.Value) value).getAsString();
			} else {
				return value.toString();
			}
		}

		if (SqlUtils.isDataBaseType(type)) {
			return value;
		} else {
			if (value == null) {
				return null;
			}

			if (value instanceof Date) {
				return new java.sql.Date(((Date) value).getTime());
			}

			return JSONUtils.toJSONString(value);
		}
	}

	public boolean isIndexColumn() {
		return field.getAnnotatedElement().getAnnotation(Index.class) != null;
	}

	public boolean isNullable() {
		if (field.getGetter().getType().isPrimitive()) {
			return false;
		}

		if (isPrimaryKey()) {
			return false;
		}

		if (isIndexColumn()) {
			return false;
		}

		scw.sql.orm.annotation.Column column = getColumn();
		return column == null ? true : column.nullAble();
	}

	public boolean isAutoIncrement() {
		return field.getAnnotatedElement().getAnnotation(AutoIncrement.class) != null;
	}

	public String getCharsetName() {
		scw.sql.orm.annotation.Column column = getColumn();
		return column == null ? null : column.charsetName().trim();
	}

	public boolean isUnique() {
		scw.sql.orm.annotation.Column column = getColumn();
		return column == null ? false : column.unique();
	}

	protected scw.sql.orm.annotation.Column getColumn() {
		return field.getAnnotatedElement().getAnnotation(scw.sql.orm.annotation.Column.class);
	}

	public CasType getCasType() {
		if (isPrimaryKey()) {
			return CasType.NOTHING;
		}

		scw.sql.orm.annotation.Column column = getColumn();
		if (column == null) {
			return CasType.NOTHING;
		}
		return column.casType();
	}

	@Override
	public final int hashCode() {
		return getName().toLowerCase().hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof Column) {
			return ((Column) obj).getName().equalsIgnoreCase(getName());
		}
		return false;
	}

	public String getDescription() {
		scw.sql.orm.annotation.Column column = getColumn();
		return column == null ? null : column.comment();
	}

	public SqlType getSqlType(SqlTypeFactory sqlTypeFactory) {
		String type = null;
		scw.sql.orm.annotation.Column column = getColumn();
		if (column != null) {
			type = column.type();
		}

		SqlType tempSqlType = StringUtils.isEmpty(type) ? sqlTypeFactory.getSqlType(field.getGetter().getType())
				: sqlTypeFactory.getSqlType(type);
		type = tempSqlType.getName();

		int len = -1;
		if (column != null) {
			len = column.length();
		}
		if (len <= 0) {
			len = tempSqlType.getLength();
		}
		return new SqlType(type, len);
	}

	public CounterInfo getCounterInfo() {
		Counter counter = field.getAnnotatedElement().getAnnotation(Counter.class);
		return counter == null ? null : new CounterInfo(counter.min(), counter.max());
	}

	public AnnotatedElement getAnnotatedElement() {
		return field.getAnnotatedElement();
	}
	
	public boolean isForceUpdate(){
		return getAnnotatedElement().getAnnotation(ForceUpdate.class) != null;
	}
	
	@Override
	public String toString() {
		return field.toString();
	}
}
