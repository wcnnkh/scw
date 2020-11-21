package scw.mapper;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;

import scw.core.annotation.AnnotatedElementUtils;
import scw.core.annotation.MultiAnnotatedElement;
import scw.core.utils.ObjectUtils;

public class FieldMetadata implements Serializable {
	public static final FieldMetadata[] EMPTY_ARRAY = new FieldMetadata[0];
	private static final long serialVersionUID = 1L;
	private final Getter getter;
	private final Setter setter;

	public FieldMetadata(Getter getter, Setter setter) {
		this.getter = getter;
		this.setter = setter;
	}

	public Getter getGetter() {
		return getter;
	}

	public Setter getSetter() {
		return setter;
	}

	public boolean isSupportGetter() {
		return getter != null;
	}

	public boolean isSupportSetter() {
		return setter != null;
	}

	/**
	 * 将gettr setter的AnnotatedElement结果合并
	 * 
	 * @return
	 */
	public AnnotatedElement getAnnotatedElement() {
		if (isSupportGetter() && isSupportSetter()) {
			return new MultiAnnotatedElement(getGetter().getAnnotatedElement(), getSetter().getAnnotatedElement());
		}

		if (isSupportGetter()) {
			return getGetter().getAnnotatedElement();
		}

		if (isSupportSetter()) {
			return getSetter().getAnnotatedElement();
		}
		return AnnotatedElementUtils.EMPTY_ANNOTATED_ELEMENT;
	}

	@Override
	public int hashCode() {
		if (getter == null && setter == null) {
			return 0;
		}

		if (getter == null) {
			return setter.hashCode();
		}

		if (setter == null) {
			return getter.hashCode();
		}

		return getter.hashCode() + setter.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof FieldMetadata) {
			return ObjectUtils.nullSafeEquals(getter, ((FieldMetadata) obj).getter)
					&& ObjectUtils.nullSafeEquals(setter, ((FieldMetadata) obj).setter);
		}

		return false;
	}
}
