package scw.sql.orm.enums;

public enum IndexType {
	/**
	 * 默认索引
	 */
	DEFAULT, 
	/**
	 * 唯一索引
	 */
	UNIQUE, 
	/**
	 * 全文索引
	 */
	FULLTEXT, 
	/**
	 * 空间索引
	 */
	SPATIAL;
}
