package scw.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

public final class AnnotationUtils {
	private AnnotationUtils() {
	};

	public static Method[] getAnnoationMethods(Class<?> type, boolean useSuper, boolean useInterface,
			Class<? extends Annotation> annotationClass) {
		Map<String, Method> map = new HashMap<String, Method>();
		Class<?> clz = type;
		while (clz != null) {
			appendAnnoationMethod(map, clz, annotationClass);
			if (useInterface) {
				appendAnnoationInterfaceMethod(map, clz, annotationClass);
			}

			if (!useSuper) {
				break;
			}

			clz = clz.getSuperclass();
		}
		return map.values().toArray(new Method[map.size()]);
	}

	private static void appendAnnoationInterfaceMethod(Map<String, Method> methodMap, Class<?> type,
			Class<? extends Annotation> annotationClass) {
		Class<?>[] interfaces = type.getInterfaces();
		if (interfaces == null || interfaces.length == 0) {
			return;
		}

		for (Class<?> clz : interfaces) {
			appendAnnoationMethod(methodMap, clz, annotationClass);
			appendAnnoationInterfaceMethod(methodMap, clz, annotationClass);
		}
	}

	private static void appendAnnoationMethod(Map<String, Method> methodMap, Class<?> type,
			Class<? extends Annotation> annotationClass) {
		for (Method method : type.getDeclaredMethods()) {
			if (isDeprecated(method)) {
				continue;
			}

			Annotation annotation = method.getAnnotation(annotationClass);
			if (annotation == null) {
				continue;
			}

			StringBuilder sb = new StringBuilder();
			sb.append(method.getName());
			for (Class<?> t : method.getParameterTypes()) {
				sb.append("&");
				sb.append(t.getName());
			}

			String key = sb.toString();
			if (methodMap.containsKey(key)) {
				continue;
			}

			methodMap.put(key, method);
		}
	}

	public static boolean isDeprecated(AccessibleObject accessibleObject) {
		return accessibleObject.getAnnotation(Deprecated.class) != null;
	}

	public static LinkedList<Field> getAnnotationFieldList(Class<?> clazz, boolean isDeclared, boolean sup,
			Class<? extends Annotation> annotationClass) {
		Class<?> clz = clazz;
		LinkedList<Field> fieldList = new LinkedList<Field>();
		while (clz != null && clz != Object.class) {
			for (Field field : isDeclared ? clz.getDeclaredFields() : clz.getFields()) {
				if (isDeprecated(field)) {
					continue;
				}

				Annotation annotation = field.getAnnotation(annotationClass);
				if (annotation == null) {
					continue;
				}

				fieldList.add(field);
				if (sup) {
					clz = clz.getSuperclass();
				} else {
					break;
				}
			}
		}
		return fieldList;
	}

	public static IdentityHashMap<Class<? extends Annotation>, Annotation> getAnnoataionMap(
			AnnotatedElement annotatedElement) {
		Annotation[] annotations = annotatedElement.getAnnotations();
		if (ArrayUtils.isEmpty(annotations)) {
			return null;
		}

		IdentityHashMap<Class<? extends Annotation>, Annotation> map = new IdentityHashMap<Class<? extends Annotation>, Annotation>(
				annotations.length);
		for (Annotation annotation : annotations) {
			map.put(annotation.getClass(), annotation);
		}
		return map;
	}
}