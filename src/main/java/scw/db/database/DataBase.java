package scw.db.database;

/**
 * 数据库
 * @author shuchaowen
 *
 */
public interface DataBase {
	
	/**
	 * 获取一个未指定数据库连接的地址
	 * @return
	 */
	String getConnectionURL();
	
	/**
	 * 获取数据库名称
	 * @return
	 */
	String getDataBase();
	
	/**
	 * 获取数据库驱动
	 * @return
	 */
	String getDriverClassName();
	
	/**
	 * 获取连接数据库的账号
	 * @return
	 */
	String getUsername();
	
	/**
	 * 获取连接数据库的密码
	 * @return
	 */
	String getPassword();
	
	/**
	 * 获取数据库类型
	 * @return
	 */
	DataBaseType getDataBaseType();
	
	/**
	 * 创建数据库
	 */
	void create();
	
	/**
	 * 创建数据库
	 * @param database
	 */
	void create(String database);
}