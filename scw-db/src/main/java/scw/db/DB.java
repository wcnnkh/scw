package scw.db;

import scw.aop.annotation.AopEnable;
import scw.sql.Sql;
import scw.sql.SqlOperations;
import scw.sql.orm.EntityOperations;

@AopEnable(false)
public interface DB extends EntityOperations, SqlOperations {
	/**
	 * 创建表
	 * @param tableClass
	 * @param registerManager 是否注册到{@see DBManager}
	 * @return
	 */
	boolean createTable(Class<?> tableClass, boolean registerManager);

	/**
	 * 创建表
	 * @param tableClass
	 * @param tableName 指定表名
	 * @param registerManager 是否注册到{@see DBManager}
	 * @return
	 */
	boolean createTable(Class<?> tableClass, String tableName, boolean registerManager);

	/**
	 * 扫描指定包下的@Table并创建表
	 * @param packageName 
	 * @param registerManager 是否注册到{@see DBManager}
	 */
	void createTable(String packageName, boolean registerManager);

	/**
	 * 异步执行
	 * @param asyncExecute
	 */
	void asyncExecute(AsyncExecute asyncExecute);

	/**
	 * 异步保存
	 * @param objs
	 */
	void asyncSave(Object... objs);

	/**
	 * 异步更新
	 * @param objs
	 */
	void asyncUpdate(Object... objs);

	/**
	 * 异步删除
	 * @param objs
	 */
	void asyncDelete(Object... objs);

	/**
	 * 异步保存或更新
	 * @param objs
	 */
	void asyncSaveOrUpdate(Object... objs);

	/**
	 * 异步执行
	 * @param sqls
	 */
	void asyncExecute(Sql... sqls);
}
