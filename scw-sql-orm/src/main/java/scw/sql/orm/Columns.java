package scw.sql.orm;

import java.util.Set;

import scw.util.Accept;

public interface Columns extends Iterable<Column> {
	/**
	 * 返回所有字段
	 * 
	 * @return
	 */
	Set<Column> toSet();

	/**
	 * 返回数组库字段
	 * 
	 * @return
	 */
	Set<Column> getColumns();

	/**
	 * 返回主键字段
	 * 
	 * @return
	 */
	Set<Column> getPrimaryKeys();

	/**
	 * 返回非主键字段
	 * 
	 * @return
	 */
	Set<Column> getNotPrimaryKeys();
	
	Column find(String name);
	
	Column find(Accept<Column> accept);
}
