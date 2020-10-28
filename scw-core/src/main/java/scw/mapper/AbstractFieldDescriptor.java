package scw.mapper;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import scw.core.annotation.MultiAnnotatedElement;
import scw.core.reflect.FieldHolder;
import scw.core.reflect.MethodHolder;
import scw.core.reflect.SerializableField;
import scw.core.reflect.SerializableMethod;
import scw.lang.NotSupportedException;

public abstract class AbstractFieldDescriptor implements FieldDescriptor {
	private static final long serialVersionUID = 1L;
	private final Class<?> declaringClass;
	private final FieldHolder field;
	private final MethodHolder method;
	private final AnnotatedElement annotatedElement;

	public AbstractFieldDescriptor(Class<?> declaringClass, Field field, Method method) {
		this.declaringClass = declaringClass;
		this.annotatedElement = MultiAnnotatedElement.forAnnotatedElements(true, method, field);
		this.field = field == null ? null : new SerializableField(field);
		this.method = method == null ? null : new SerializableMethod(method);
	}

	public AnnotatedElement getAnnotatedElement() {
		return annotatedElement;
	}

	public java.lang.reflect.Field getField() {
		return field == null ? null : field.getField();
	}

	public Method getMethod() {
		return method == null ? null : method.getMethod();
	}

	public int getModifiers() {
		Method method = getMethod();
		Field field = getField();
		if (method != null && field != null) {
			return method.getModifiers() | field.getModifiers();
		}

		if (method != null) {
			return method.getModifiers();
		}

		if (field != null) {
			return field.getModifiers();
		}
		throw createNotSupportException();
	}

	protected NotSupportedException createNotSupportException() {
		return new NotSupportedException("class=[" + getDeclaringClass() + "] column [" + getName() + "]");
	}

	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof FieldDescriptor) {
			// 这里并不需要判断泛型类型，因为如果set/get方法的"参数类型/返回类型"一样时就是方法的重载
			return getType().equals(((FieldDescriptor) obj).getType())
					&& ((FieldDescriptor) obj).getName().equals(getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode() + getType().hashCode();
	}

	@Override
	public String toString() {
		if(field == null && method == null){
			return "declaringClass [" + getDeclaringClass() + "] name [" + getName() + "]";
		}
		
		StringBuilder sb = new StringBuilder();
		if(field != null){
			sb.append("field[").append(field).append("]");
		}
		
		if(method != null){
			if(sb.length() != 0){
				sb.append(" ");
			}
			sb.append("method[").append(method).append("]");
		}
		return sb.toString();
	}
}
