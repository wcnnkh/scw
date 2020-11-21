package scw.core.instance;

import java.util.Collection;
import java.util.Map;

import scw.core.annotation.AnnotationUtils;
import scw.core.instance.annotation.PropertyParameter;
import scw.core.instance.annotation.ResourceParameter;
import scw.core.parameter.AbstractParameterFactory;
import scw.core.parameter.ParameterDescriptor;
import scw.core.parameter.ParameterDescriptors;
import scw.core.parameter.ParameterUtils;
import scw.core.reflect.ReflectionUtils;
import scw.io.ResourceUtils;
import scw.value.Value;
import scw.value.ValueUtils;
import scw.value.property.PropertyFactory;

public abstract class DefaultParameterFactory extends AbstractParameterFactory {
	public abstract NoArgsInstanceFactory getInstanceFactory();

	public abstract PropertyFactory getPropertyFactory();

	protected boolean isProerptyType(ParameterDescriptor parameterConfig) {
		PropertyParameter propertyParameter = parameterConfig.getAnnotatedElement()
				.getAnnotation(PropertyParameter.class);
		if (propertyParameter == null) {
			Class<?> type = parameterConfig.getType();
			if (ValueUtils.isBaseType(type) || type.isArray() || Collection.class.isAssignableFrom(type)
					|| Map.class.isAssignableFrom(type)) {
				return true;
			}

			if (!ReflectionUtils.isInstance(type, true)) {
				return false;
			}

			return type.getName().startsWith("java.") || type.getName().startsWith("javax.");
		} else {
			return propertyParameter.value();
		}
	}

	protected String getParameterName(ParameterDescriptors parameterDescriptors,
			ParameterDescriptor parameterDescriptor) {
		String displayName = ParameterUtils.getDisplayName(parameterDescriptor);
		if (parameterDescriptor.getName().equals(displayName)) {
			return parameterDescriptors.getDeclaringClass().getName() + "." + displayName;
		}
		return displayName;
	}

	protected Value getProperty(ParameterDescriptors parameterDescriptors, ParameterDescriptor parameterDescriptor) {
		String name = getParameterName(parameterDescriptors, parameterDescriptor);
		Value value = getPropertyFactory().getValue(name);
		if (value == null) {
			value = ParameterUtils.getDefaultValue(parameterDescriptor);
		}

		if (value != null) {
			ResourceParameter resourceParameter = parameterDescriptor.getAnnotatedElement()
					.getAnnotation(ResourceParameter.class);
			if (resourceParameter != null) {
				if (!ResourceUtils.getResourceOperations().isExist(value.getAsString())) {
					return null;
				}
			}
		}
		return value;
	}

	protected String getInstanceName(ParameterDescriptors parameterDescriptors,
			ParameterDescriptor parameterDescriptor) {
		if (getInstanceFactory().isInstance(parameterDescriptor.getType())) {
			return parameterDescriptor.getType().getName();
		}

		String name = getParameterName(parameterDescriptors, parameterDescriptor);
		if (getInstanceFactory().isInstance(name)) {
			return name;
		}

		return null;
	}

	@Override
	protected boolean isAccept(ParameterDescriptors parameterDescriptors, ParameterDescriptor parameterDescriptor,
			int index) {
		boolean require = !AnnotationUtils.isNullable(parameterDescriptor.getAnnotatedElement(), false);
		if (!require) {
			return true;
		}

		if (parameterDescriptor.getType() == parameterDescriptors.getDeclaringClass()) {
			return false;
		}

		boolean isProperty = isProerptyType(parameterDescriptor);
		// 是否是属性而不是bean
		if (isProperty) {
			Value value = getProperty(parameterDescriptors, parameterDescriptor);
			if (value == null) {
				return false;
			}
		} else {
			String name = getInstanceName(parameterDescriptors, parameterDescriptor);
			if (name == null) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected Object getParameter(ParameterDescriptors parameterDescriptors, ParameterDescriptor parameterDescriptor,
			int index) {
		boolean require = !AnnotationUtils.isNullable(parameterDescriptor.getAnnotatedElement(), false);
		if (isProerptyType(parameterDescriptor)) {
			Value value = getProperty(parameterDescriptors, parameterDescriptor);
			if (value == null) {
				if (require) {
					throw new RuntimeException(
							parameterDescriptors.getSource() + " require parameter:" + parameterDescriptor.getName());
				}
				return null;
			}

			return value.getAsObject(parameterDescriptor.getGenericType());
		} else {
			String name = getInstanceName(parameterDescriptors, parameterDescriptor);
			return name == null ? null : getInstanceFactory().getInstance(name);
		}
	}
}
