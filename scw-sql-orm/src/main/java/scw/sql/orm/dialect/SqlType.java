package scw.sql.orm.dialect;

import java.io.Serializable;

public class SqlType implements Serializable{
	private static final long serialVersionUID = 1L;
	private final String name;
	private final int length;

	public SqlType(String name, int length) {
		this.name = name;
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public int getLength() {
		return length;
	}
}
