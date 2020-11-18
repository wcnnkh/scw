package scw.sql.orm.dialect;

public class SqlDialectException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public SqlDialectException(String message){
		super(message);
	}

	public SqlDialectException(String message, Throwable e) {
		super(message, e);
	}
}
