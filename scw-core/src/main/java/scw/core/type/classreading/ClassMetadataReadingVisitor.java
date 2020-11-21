package scw.core.type.classreading;

import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import scw.core.Constants;
import scw.core.type.ClassMetadata;
import scw.core.utils.ClassUtils;
import scw.core.utils.StringUtils;

/**
 * ASM class visitor which looks only for the class name and implemented types,
 * exposing them through the {@link scw.core.type.ClassMetadata}
 * interface.
 */
class ClassMetadataReadingVisitor extends ClassVisitor implements ClassMetadata {

	private String className;

	private boolean isInterface;

	private boolean isAnnotation;

	private boolean isAbstract;

	private boolean isFinal;

	private String enclosingClassName;

	private boolean independentInnerClass;

	private String superClassName;

	private String[] interfaces;

	private Set<String> memberClassNames = new LinkedHashSet<String>(4);


	public ClassMetadataReadingVisitor() {
		super(Constants.ASM_VERSION);
	}


	@Override
	public void visit(int version, int access, String name, String signature, String supername, String[] interfaces) {
		this.className = ClassUtils.convertResourcePathToClassName(name);
		this.isInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
		this.isAnnotation = ((access & Opcodes.ACC_ANNOTATION) != 0);
		this.isAbstract = ((access & Opcodes.ACC_ABSTRACT) != 0);
		this.isFinal = ((access & Opcodes.ACC_FINAL) != 0);
		if (supername != null && !this.isInterface) {
			this.superClassName = ClassUtils.convertResourcePathToClassName(supername);
		}
		this.interfaces = new String[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			this.interfaces[i] = ClassUtils.convertResourcePathToClassName(interfaces[i]);
		}
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		this.enclosingClassName = ClassUtils.convertResourcePathToClassName(owner);
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		if (outerName != null) {
			String fqName = ClassUtils.convertResourcePathToClassName(name);
			String fqOuterName = ClassUtils.convertResourcePathToClassName(outerName);
			if (this.className.equals(fqName)) {
				this.enclosingClassName = fqOuterName;
				this.independentInnerClass = ((access & Opcodes.ACC_STATIC) != 0);
			}
			else if (this.className.equals(fqOuterName)) {
				this.memberClassNames.add(fqName);
			}
		}
	}

	@Override
	public void visitSource(String source, String debug) {
		// no-op
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// no-op
		return new EmptyAnnotationVisitor();
	}

	@Override
	public void visitAttribute(Attribute attr) {
		// no-op
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		// no-op
		return new EmptyFieldVisitor();
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// no-op
		return new EmptyMethodVisitor();
	}

	@Override
	public void visitEnd() {
		// no-op
	}


	public String getClassName() {
		return this.className;
	}

	public boolean isInterface() {
		return this.isInterface;
	}

	public boolean isAnnotation() {
		return this.isAnnotation;
	}

	public boolean isAbstract() {
		return this.isAbstract;
	}

	public boolean isConcrete() {
		return !(this.isInterface || this.isAbstract);
	}

	public boolean isFinal() {
		return this.isFinal;
	}

	public boolean isIndependent() {
		return (this.enclosingClassName == null || this.independentInnerClass);
	}

	public boolean hasEnclosingClass() {
		return (this.enclosingClassName != null);
	}

	public String getEnclosingClassName() {
		return this.enclosingClassName;
	}

	public boolean hasSuperClass() {
		return (this.superClassName != null);
	}

	public String getSuperClassName() {
		return this.superClassName;
	}

	public String[] getInterfaceNames() {
		return this.interfaces;
	}

	public String[] getMemberClassNames() {
		return StringUtils.toStringArray(this.memberClassNames);
	}


	private static class EmptyAnnotationVisitor extends AnnotationVisitor {

		public EmptyAnnotationVisitor() {
			super(Constants.ASM_VERSION);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String desc) {
			return this;
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			return this;
		}
	}


	private static class EmptyMethodVisitor extends MethodVisitor {

		public EmptyMethodVisitor() {
			super(Constants.ASM_VERSION);
		}
	}


	private static class EmptyFieldVisitor extends FieldVisitor {

		public EmptyFieldVisitor() {
			super(Constants.ASM_VERSION);
		}
	}

}
