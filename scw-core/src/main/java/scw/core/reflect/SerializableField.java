package scw.core.reflect;

import java.io.Serializable;
import java.lang.reflect.Field;

public final class SerializableField implements FieldHolder, Serializable {
	private static final long serialVersionUID = 1L;
	private volatile transient Field field;
	private final String fieldName;
	private final Class<?> declaringClass;

	public SerializableField(Field field) {
		this.field = field;
		this.declaringClass = field == null ? null : field.getDeclaringClass();
		this.fieldName = field == null ? null : field.getName();
	}

	public Field getField() {
		if (field == null) {
			synchronized (this) {
				if (field == null) {
					field = ReflectionUtils.findField(declaringClass, fieldName);
				}
			}
		}
		return field;
	}

	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	public String getFieldName() {
		return fieldName;
	}
	
	@Override
	public String toString() {
		return getField().toString();
	}
}
