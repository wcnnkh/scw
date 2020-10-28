package scw.lang;

public class NotSupportedException extends RuntimeException{
	private static final long serialVersionUID = 5341163945147654715L;

	public NotSupportedException(String message) {
		super(message);
	}
	
	public NotSupportedException(Throwable e) {
		super(e);
	}
	
	public NotSupportedException(String message, Throwable e) {
		super(message, e);
	}
}
