package scw.lang;

public class KeyNonexistentException extends RuntimeException{
	private static final long serialVersionUID = 5341163945147654715L;

	public KeyNonexistentException(String message) {
		super(message);
	}
	
	public KeyNonexistentException(Throwable e) {
		super(e);
	}
	
	public KeyNonexistentException(String message, Throwable e) {
		super(message, e);
	}
}
