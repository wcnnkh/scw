package scw.lang;

public class NestedRuntimeException extends RuntimeException {

	/** Use serialVersionUID from Spring 1.2 for interoperability. */
	private static final long serialVersionUID = 5439915454935047936L;

	static {
		// Eagerly load the NestedExceptionUtils class to avoid classloader
		// deadlock
		// issues on OSGi when calling getMessage(). Reported by Don Brown;
		// SPR-5607.
		NestedExceptionUtils.class.getName();
	}

	/**
	 * Construct a {@code NestedRuntimeException} with the specified detail
	 * message.
	 * 
	 * @param msg
	 *            the detail message
	 */
	public NestedRuntimeException(String msg) {
		super(msg);
	}

	public NestedRuntimeException(Throwable cause) {
		super(cause);
	}

	public NestedRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Return the detail message, including the message from the nested
	 * exception if there is one.
	 */
	@Override
	public String getMessage() {
		return NestedExceptionUtils.buildMessage(super.getMessage(), getCause());
	}

	/**
	 * Retrieve the innermost cause of this exception, if any.
	 * 
	 * @return the innermost exception, or {@code null} if none
	 */
	public Throwable getRootCause() {
		return NestedExceptionUtils.getRootCause(this);
	}

	/**
	 * Retrieve the most specific cause of this exception, that is, either the
	 * innermost cause (root cause) or this exception itself.
	 * <p>
	 * Differs from {@link #getRootCause()} in that it falls back to the present
	 * exception if there is no root cause.
	 * 
	 * @return the most specific cause (never {@code null})
	 */
	public Throwable getMostSpecificCause() {
		Throwable rootCause = getRootCause();
		return (rootCause != null ? rootCause : this);
	}

	/**
	 * Check whether this exception contains an exception of the given type:
	 * either it is of the given class itself or it contains a nested cause of
	 * the given type.
	 * 
	 * @param exType
	 *            the exception type to look for
	 * @return whether there is a nested exception of the specified type
	 */
	public boolean contains(Class<?> exType) {
		if (exType == null) {
			return false;
		}
		if (exType.isInstance(this)) {
			return true;
		}
		Throwable cause = getCause();
		if (cause == this) {
			return false;
		}
		if (cause instanceof NestedRuntimeException) {
			return ((NestedRuntimeException) cause).contains(exType);
		} else {
			while (cause != null) {
				if (exType.isInstance(cause)) {
					return true;
				}
				if (cause.getCause() == cause) {
					break;
				}
				cause = cause.getCause();
			}
			return false;
		}
	}

}
