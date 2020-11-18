package scw.sql.orm.dialect;

public class DialectHelper {
	private static final char POINT = '.';

	public String getEscapeCharacter() {
		return "`";
	}

	public void keywordProcessing(StringBuilder sb, String column) {
		sb.append(getEscapeCharacter()).append(column).append(getEscapeCharacter());
	}

	public void keywordProcessing(StringBuilder sb, String tableName, String column) {
		sb.append(getEscapeCharacter()).append(tableName).append(getEscapeCharacter());
		sb.append(POINT);
		sb.append(getEscapeCharacter()).append(column).append(getEscapeCharacter());
	}

	public String getSqlName(String tableName, String column) {
		StringBuilder sb = new StringBuilder();
		keywordProcessing(sb, tableName, column);
		return sb.toString();
	}

	public String getCreateTablePrefix() {
		return "CREATE TABLE IF NOT EXISTS";
	}
}
