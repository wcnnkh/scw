package scw.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import scw.core.reflect.ReflectionUtils;
import scw.lang.NestedExceptionUtils;

abstract class AbstractGetterSetter extends AbstractFieldDescriptor {
	private static final long serialVersionUID = 1L;
	private final String name;

	public AbstractGetterSetter(Class<?> declaringClass, String name, Field field, Method method) {
		super(declaringClass, field, method);
		this.name = name;
	}

	public Object get(Object instance) {
		Method method = getMethod();
		if (method != null) {
			ReflectionUtils.makeAccessible(method);
			try {
				return method.invoke(Modifier.isStatic(method.getModifiers()) ? null : instance);
			} catch (Exception e) {
				throw new RuntimeException(toString(), NestedExceptionUtils.excludeInvalidNestedExcpetion(e));
			}
		}

		java.lang.reflect.Field field = getField();
		if (field != null) {
			ReflectionUtils.makeAccessible(field);
			try {
				return field.get(Modifier.isStatic(field.getModifiers()) ? null : instance);
			} catch (Exception e) {
				throw new RuntimeException(toString(), NestedExceptionUtils.excludeInvalidNestedExcpetion(e));
			}
		}
		throw createNotSupportException();
	}

	public void set(Object instance, Object value) {
		Method method = getMethod();
		if (method != null) {
			ReflectionUtils.makeAccessible(method);
			try {
				method.invoke(Modifier.isStatic(method.getModifiers()) ? null : instance, value);
			} catch (Exception e) {
				throw new RuntimeException(toString() + " value [" + value + "]", NestedExceptionUtils.excludeInvalidNestedExcpetion(e));
			}
			return;
		}

		java.lang.reflect.Field field = getField();
		if (field != null) {
			ReflectionUtils.makeAccessible(field);
			try {
				field.set(Modifier.isStatic(field.getModifiers()) ? null : instance, value);
			} catch (Exception e) {
				throw new RuntimeException(toString() + " value [" + value + "]", NestedExceptionUtils.excludeInvalidNestedExcpetion(e));
			}
			return;
		}
		throw createNotSupportException();
	}

	public String getName() {
		return name;
	}
}
