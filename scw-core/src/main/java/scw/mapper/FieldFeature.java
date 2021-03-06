package scw.mapper;

import java.lang.reflect.Modifier;

import scw.lang.Ignore;
import scw.lang.NotSupportedException;
import scw.util.Accept;

public enum FieldFeature implements Accept<Field> {
	/**
	 * 存在getter行为
	 */
	SUPPORT_GETTER, 
	/**
	 * 存在setter行为
	 */
	SUPPORT_SETTER,
	
	/**
	 * 存在getter行为且是public
	 */
	GETTER_PUBLIC, 
	/**
	 * 存在setter行为且是public
	 */
	SETTER_PUBLIC, 
	/**
	 * 忽略getter的静态field
	 */
	GETTER_IGNORE_STATIC, 
	/**
	 * 忽略setter的静态field
	 */
	SETTER_IGNORE_STATIC,
	/**
	 * 忽略getter的transient field
	 */
	GETTER_IGNORE_TRANSIENT, 
	/**
	 * 忽略setter的transient field
	 */
	SETTER_IGNORE_TRANSIENT,
	/**
	 * 忽略静态字段
	 */
	IGNORE_STATIC,
	/**
	 * 对象公有的setter字段，忽略static, final字段,必须存在实际的java.lang.Field
	 */
	SETTER,

	/**
	 * 对象公有的getter字段,忽略static字段
	 */
	GETTER,

	/**
	 * getter要存在java field
	 */
	EXISTING_GETTER_FIELD,

	/**
	 * setter要存在java field
	 */
	EXISTING_SETTER_FIELD,

	/**
	 * 忽略getter的final
	 */
	IGNORE_GETTER_FINAL,
	
	/**
	 * 忽略setter的final
	 */
	IGNORE_SETTER_FINAL,

	/**
	 * @see Ignore
	 */
	IGNORE_ANNOTATION;

	@Override
	public boolean accept(Field field) {
		switch (this) {
		case SUPPORT_GETTER:
			return field.isSupportGetter();
		case SUPPORT_SETTER:
			return field.isSupportSetter();
		case GETTER_PUBLIC:
			return field.isSupportGetter() && Modifier.isPublic(field.getGetter().getModifiers());
		case SETTER_PUBLIC:
			return field.isSupportSetter() && Modifier.isPublic(field.getSetter().getModifiers());
		case GETTER_IGNORE_STATIC:
			return field.isSupportGetter() && !Modifier.isStatic(field.getGetter().getModifiers());
		case SETTER_IGNORE_STATIC:
			return field.isSupportSetter() && !Modifier.isStatic(field.getSetter().getModifiers());
		case GETTER_IGNORE_TRANSIENT:
			return field.isSupportSetter() && !Modifier.isTransient(field.getSetter().getModifiers());
		case SETTER_IGNORE_TRANSIENT:
			return field.isSupportSetter() && !Modifier.isTransient(field.getSetter().getModifiers());
		case IGNORE_STATIC:
			if (field.isSupportGetter() && Modifier.isStatic(field.getGetter().getModifiers())) {
				return false;
			}

			if (field.isSupportSetter() && Modifier.isStatic(field.getSetter().getModifiers())) {
				return false;
			}
			return true;
		case SETTER:
			return field.isSupportSetter() && field.getSetter().getField() != null
					&& !Modifier.isStatic(field.getSetter().getField().getModifiers())
					&& !Modifier.isFinal(field.getSetter().getField().getModifiers());
		case GETTER:
			return field.isSupportGetter() && !Modifier.isStatic(field.getGetter().getModifiers())
					&& Modifier.isPublic(field.getGetter().getModifiers());
		case EXISTING_GETTER_FIELD:
			return field.isSupportGetter() && field.getGetter().getField() != null;
		case EXISTING_SETTER_FIELD:
			return field.isSupportSetter() && field.getSetter().getField() != null;
		case IGNORE_GETTER_FINAL:
			if (field.isSupportGetter() && field.getGetter().getField() != null
					&& Modifier.isFinal(field.getGetter().getField().getModifiers())) {
				return false;
			}
			return true;
		case IGNORE_SETTER_FINAL:
			if (field.isSupportSetter() && field.getSetter().getField() != null
					&& Modifier.isFinal(field.getSetter().getField().getModifiers())) {
				return false;
			}
			return true;
		case IGNORE_ANNOTATION:
			return !field.isAnnotationPresent(Ignore.class);
		default:
			throw new NotSupportedException(this.toString());
		}
	}
}
