package scw.mapper.support;

import java.util.LinkedList;
import java.util.ListIterator;

import scw.core.instance.InstanceUtils;
import scw.core.parameter.ParameterUtils;
import scw.core.utils.ClassUtils;
import scw.mapper.Field;
import scw.mapper.FieldDescriptor;
import scw.mapper.FilterFeature;
import scw.mapper.Mapper;
import scw.mapper.Mapping;

public abstract class AbstractMapping implements Mapping {

	public <T> T newInstance(Class<? extends T> type) {
		return InstanceUtils.INSTANCE_FACTORY.getInstance(type);
	}

	public Object mapping(Class<?> entityClass, Field field,
			Mapper fieldFactory) throws Exception{
		if (isNesting(field)) {
			return fieldFactory.mapping(field.getSetter()
					.getType(), field, this);
		} else {
			return getValue(field);
		}
	}
	
	public boolean accept(Field field) {
		return FilterFeature.GETTER_IGNORE_STATIC.getFilter().accept(field);
	}

	protected boolean isNesting(Field field) {
		Class<?> type = field.getSetter().getType();
		return !(type == String.class || ClassUtils.isPrimitiveOrWrapper(type));
	}

	protected abstract Object getValue(Field field);

	protected String getDisplayName(FieldDescriptor fieldMetadata) {
		return ParameterUtils.getDisplayName(fieldMetadata);
	}

	protected final String getNestingDisplayName(Field field) {
		if (field.getParentField() == null) {
			return getDisplayName(field.getSetter());
		}

		LinkedList<FieldDescriptor> fieldMetadatas = new LinkedList<FieldDescriptor>();
		Field parent = field;
		while (parent != null) {
			fieldMetadatas.add(parent.getSetter());
			parent = parent.getParentField();
		}

		StringBuilder sb = new StringBuilder();
		ListIterator<FieldDescriptor> iterator = fieldMetadatas
				.listIterator(fieldMetadatas.size());
		while (iterator.hasPrevious()) {
			FieldDescriptor fieldMetadata = iterator.next();
			sb.append(getDisplayName(fieldMetadata));
			if (iterator.hasPrevious()) {
				sb.append(".");
			}
		}
		return sb.toString();
	}
}
