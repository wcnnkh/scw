package scw.db.druid;

import java.sql.Connection;
import java.sql.SQLException;

import com.alibaba.druid.pool.DruidDataSource;

import scw.core.annotation.Order;
import scw.db.AbstractDB;
import scw.db.DBUtils;
import scw.db.database.DataBase;
import scw.memcached.Memcached;
import scw.redis.Redis;
import scw.sql.orm.dialect.SqlDialect;
import scw.value.property.PropertyFactory;

public class DruidDB extends AbstractDB {
	static {
		DruidDataSource.class.getName();
	}

	private DruidDataSource datasource;
	private DataBase dataBase;

	@Order
	public DruidDB(String properties) {
		this(new PropertyFactory(false, true).loadProperties(properties, "UTF-8").registerListener());
	}

	@Order
	public DruidDB(String properties, Memcached memcached) {
		this(new PropertyFactory(false, true).loadProperties(properties, "UTF-8").registerListener(), memcached);
	}

	@Order
	public DruidDB(String properties, Redis redis) {
		this(new PropertyFactory(false, true).loadProperties(properties, "UTF-8").registerListener(), redis);
	}

	public DruidDB(PropertyFactory propertyFactory) {
		super();
		initConfig(propertyFactory);
	}

	public DruidDB(PropertyFactory propertyFactory, Memcached memcached) {
		super(propertyFactory, memcached);
		initConfig(propertyFactory);
	}

	public DruidDB(PropertyFactory propertyFactory, Redis redis) {
		super(propertyFactory, redis);
		initConfig(propertyFactory);
	}

	private void initConfig(PropertyFactory propertyFactory) {
		if (propertyFactory == null) {
			return;
		}

		datasource = new DruidDataSource();
		DBUtils.loadProperties(datasource, propertyFactory);
		initConfig(datasource);
		createTableByProperties(propertyFactory);
	}

	protected void initConfig(DruidDataSource dataSource) {
		if (!datasource.isPoolPreparedStatements()) {// 如果配置中没有开启psCache
			datasource.setMaxPoolPreparedStatementPerConnectionSize(20);
		}

		datasource.setRemoveAbandoned(false);
		this.dataBase = DBUtils.automaticRecognition(datasource.getDriverClassName(), datasource.getUrl(),
				datasource.getUsername(), datasource.getPassword());
		dataBase.create();
	}

	public DataBase getDataBase() {
		return dataBase;
	}

	public Connection getConnection() throws SQLException {
		return datasource.getConnection();
	}

	@Override
	public SqlDialect getSqlDialect() {
		return dataBase.getSqlDialect();
	}

	public synchronized void destroy() {
		if (datasource != null && !datasource.isClosed()) {
			datasource.close();
		}
		super.destroy();
	}
}
