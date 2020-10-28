package scw.core.reflect;

import java.lang.reflect.Field;

public final class SimpleFieldHolder implements FieldHolder {
	private final Field field;

	public SimpleFieldHolder(Field field) {
		this.field = field;
	}

	public java.lang.reflect.Field getField() {
		return field;
	};
	
	@Override
	public String toString() {
		return field.toString();
	}
}
