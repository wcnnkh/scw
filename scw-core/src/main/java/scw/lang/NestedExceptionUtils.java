package scw.lang;

import java.lang.reflect.InvocationTargetException;

/**
 * Helper class for implementing exception classes which are capable of holding
 * nested exceptions. Necessary because we can't share a base class among
 * different exception types.
 *
 * <p>
 * Mainly for use within the framework.
 *
 * @see NestedRuntimeException
 * @see NestedCheckedException
 * @see NestedIOException
 */
public abstract class NestedExceptionUtils {

	/**
	 * Build a message for the given base message and root cause.
	 * 
	 * @param message
	 *            the base message
	 * @param cause
	 *            the root cause
	 * @return the full exception message
	 */
	public static String buildMessage(String message, Throwable cause) {
		if (cause == null) {
			return message;
		}
		StringBuilder sb = new StringBuilder(64);
		if (message != null) {
			sb.append(message).append("; ");
		}
		sb.append("nested exception is ").append(cause);
		return sb.toString();
	}

	/**
	 * Retrieve the innermost cause of the given exception, if any.
	 * 
	 * @param original
	 *            the original exception to introspect
	 * @return the innermost exception, or {@code null} if none
	 */
	public static Throwable getRootCause(Throwable original) {
		if (original == null) {
			return null;
		}
		Throwable rootCause = null;
		Throwable cause = original.getCause();
		while (cause != null && cause != rootCause) {
			rootCause = cause;
			cause = cause.getCause();
		}
		return rootCause;
	}

	/**
	 * Retrieve the most specific cause of the given exception, that is, either
	 * the innermost cause (root cause) or the exception itself.
	 * <p>
	 * Differs from {@link #getRootCause} in that it falls back to the original
	 * exception if there is no root cause.
	 * 
	 * @param original
	 *            the original exception to introspect
	 * @return the most specific cause (never {@code null})
	 */
	public static Throwable getMostSpecificCause(Throwable original) {
		Throwable rootCause = getRootCause(original);
		return (rootCause != null ? rootCause : original);
	}

	/**
	 * 排除无效的嵌套异常
	 * 
	 * @param original
	 * @return
	 */
	public static Throwable excludeInvalidNestedExcpetion(Throwable original) {
		Throwable cause = original;
		if (original instanceof InvocationTargetException) {// 排除反射异常
			cause = ((InvocationTargetException) cause).getTargetException();
		}
		return cause == null ? original : cause;
	}

	public static String getNonEmptyMessage(Throwable error, boolean localized){
		String message = localized? error.getLocalizedMessage():error.getMessage();
		while(message == null){
			Throwable cause = error.getCause();
			if(cause == null){
				break;
			}
			message = localized? cause.getLocalizedMessage():cause.getMessage();
		}
		return message;
	}
}
