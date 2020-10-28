package scw.sql;

import java.util.Collection;
import java.util.List;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface SqlOperations {
	/**
	 * 执行一条sql语句
	 * 
	 * @param sql
	 * @return 返回结果并不代表是否执行成功，意义请参考jdk文档<br/>
	 *         true if the first result is a ResultSet object; false if the
	 *         first result is an update count or there is no result
	 * @throws SqlException
	 */
	boolean execute(Sql sql) throws SqlException;

	void query(Sql sql, ResultSetCallback resultSetCallback) throws SqlException;

	void query(Sql sql, RowCallback rowCallback) throws SqlException;

	<T> T query(Sql sql, ResultSetMapper<T> resultSetMapper) throws SqlException;

	<T> List<T> query(Sql sql, RowMapper<T> rowMapper) throws SqlException;

	List<Object[]> query(Sql sql) throws SqlException;

	int update(Sql sql) throws SqlException;

	int[] batch(Collection<String> sqls) throws SqlException;

	int[] batch(String sql, Collection<Object[]> batchArgs) throws SqlException;
}
