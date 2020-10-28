package scw.io.serialzer;

public class SerializerException extends RuntimeException {
	private static final long serialVersionUID = 5341163945147654715L;

	public SerializerException(String message) {
		super(message);
	}

	public SerializerException(Throwable e) {
		super(e);
	}

	public SerializerException(String message, Throwable e) {
		super(message, e);
	}
}
