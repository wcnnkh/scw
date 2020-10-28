package scw.mapper;

import java.lang.reflect.Modifier;

public enum FilterFeature {
	SUPPORT_GETTER(new SupportGetterFieldFilter()), 
	SUPPORT_SETTER(new SupportSetterFieldFilter()), 
	GETTER_PUBLIC(new GetterPublicFieldFilter()),
	SETTER_PUBLIC(new SetterPublicFieldFilter()), 
	GETTER_IGNORE_STATIC(new IgnoreGetterStaticFieldFilter()), 
	SETTER_IGNORE_STATIC(new IgnoreSetterStaticFieldFilter()),
	GETTER_IGNORE_TRANSIENT(new IgnoreGetterTransientFieldFilter()),
	SETTER_IGNORE_TRANSIENT(new IgnoreSetterTransientFieldFilter()),
	/**
	 * 忽略静态字段
	 */
	IGNORE_STATIC(new IgnoreStaticFieldFilter()),
	/**
	 * 对象公有的setter字段，忽略static, final字段,必须存在实际的java.lang.Field
	 */
	SETTER(new SetterFieldFilter()),

	/**
	 * 对象公有的getter字段,忽略static字段
	 */
	GETTER(new GetterFieldFilter()),

	EXISTING_GETTER_FIELD(new ExistingFieldFilter(true)),

	EXISTING_SETTER_FIELD(new ExistingFieldFilter(false));

	private final FieldFilter filter;

	private FilterFeature(FieldFilter filter) {
		this.filter = filter;
	}

	public FieldFilter getFilter() {
		return filter;
	}

	private static final class ExistingFieldFilter implements FieldFilter {
		private final boolean getter;

		public ExistingFieldFilter(boolean getter) {
			this.getter = getter;
		}

		public boolean accept(Field field) {
			if (getter) {
				return field.isSupportGetter() && field.getGetter().getField() != null;
			} else {
				return field.isSupportSetter() && field.getSetter().getField() != null;
			}
		}
	}

	private static final class GetterFieldFilter implements FieldFilter {
		public boolean accept(Field field) {
			return field.isSupportGetter() && !Modifier.isStatic(field.getGetter().getModifiers())
					&& Modifier.isPublic(field.getGetter().getModifiers());
		}
	}

	private static final class SetterFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			return field.isSupportSetter() && field.getSetter().getField() != null
					&& !Modifier.isStatic(field.getSetter().getField().getModifiers())
					&& !Modifier.isFinal(field.getSetter().getField().getModifiers());
		}
	}

	private static final class IgnoreStaticFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			if (field.isSupportGetter() && Modifier.isStatic(field.getGetter().getModifiers())) {
				return false;
			}

			if (field.isSupportSetter() && Modifier.isStatic(field.getSetter().getModifiers())) {
				return false;
			}
			return true;
		}

	}

	private static final class IgnoreGetterTransientFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			return field.isSupportGetter() && !Modifier.isTransient(field.getGetter().getModifiers());
		}
	}

	private static final class IgnoreSetterTransientFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			return field.isSupportSetter() && !Modifier.isTransient(field.getSetter().getModifiers());
		}
	}

	private static final class IgnoreGetterStaticFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			return field.isSupportGetter() && !Modifier.isStatic(field.getGetter().getModifiers());
		}
	}

	private static final class IgnoreSetterStaticFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			return field.isSupportSetter() && !Modifier.isStatic(field.getSetter().getModifiers());
		}
	}

	private static final class SupportGetterFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			return field.isSupportGetter();
		}
	}

	private static final class SupportSetterFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			return field.isSupportSetter();
		}
	}

	private static final class SetterPublicFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			return field.isSupportSetter() && Modifier.isPublic(field.getSetter().getModifiers());
		}
	}

	private static final class GetterPublicFieldFilter implements FieldFilter {

		public boolean accept(Field field) {
			return field.isSupportGetter() && Modifier.isPublic(field.getGetter().getModifiers());
		}
	}
}
