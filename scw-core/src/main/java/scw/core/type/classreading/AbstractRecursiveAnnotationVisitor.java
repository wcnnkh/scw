package scw.core.type.classreading;

import java.lang.reflect.Field;
import java.security.AccessControlException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import scw.core.Constants;
import scw.core.annotation.AnnotationAttributes;
import scw.core.reflect.ReflectionUtils;
import scw.logger.Logger;
import scw.logger.LoggerUtils;

abstract class AbstractRecursiveAnnotationVisitor extends AnnotationVisitor {
	protected final Logger logger = LoggerUtils.getLogger(getClass());

	protected final AnnotationAttributes attributes;

	protected final ClassLoader classLoader;


	public AbstractRecursiveAnnotationVisitor(ClassLoader classLoader, AnnotationAttributes attributes) {
		super(Constants.ASM_VERSION);
		this.classLoader = classLoader;
		this.attributes = attributes;
	}


	@Override
	public void visit(String attributeName, Object attributeValue) {
		this.attributes.put(attributeName, attributeValue);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String attributeName, String asmTypeDescriptor) {
		String annotationType = Type.getType(asmTypeDescriptor).getClassName();
		AnnotationAttributes nestedAttributes = new AnnotationAttributes(annotationType, this.classLoader);
		this.attributes.put(attributeName, nestedAttributes);
		return new RecursiveAnnotationAttributesVisitor(annotationType, nestedAttributes, this.classLoader);
	}

	@Override
	public AnnotationVisitor visitArray(String attributeName) {
		return new RecursiveAnnotationArrayVisitor(attributeName, this.attributes, this.classLoader);
	}

	@Override
	public void visitEnum(String attributeName, String asmTypeDescriptor, String attributeValue) {
		Object newValue = getEnumValue(asmTypeDescriptor, attributeValue);
		visit(attributeName, newValue);
	}

	protected Object getEnumValue(String asmTypeDescriptor, String attributeValue) {
		Object valueToUse = attributeValue;
		try {
			Class<?> enumType = this.classLoader.loadClass(Type.getType(asmTypeDescriptor).getClassName());
			Field enumConstant = ReflectionUtils.findField(enumType, attributeValue);
			if (enumConstant != null) {
				ReflectionUtils.makeAccessible(enumConstant);
				valueToUse = enumConstant.get(null);
			}
		}
		catch (ClassNotFoundException ex) {
			logger.debug("Failed to classload enum type while reading annotation metadata", ex);
		}
		catch (NoClassDefFoundError ex) {
			logger.debug("Failed to classload enum type while reading annotation metadata", ex);
		}
		catch (IllegalAccessException ex) {
			logger.debug("Could not access enum value while reading annotation metadata", ex);
		}
		catch (AccessControlException ex) {
			logger.debug("Could not access enum value while reading annotation metadata", ex);
		}
		return valueToUse;
	}

}
