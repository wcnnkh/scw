package scw.json;

public class JsonParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public JsonParseException(String msg) {
		super(msg);
	}

	public JsonParseException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
