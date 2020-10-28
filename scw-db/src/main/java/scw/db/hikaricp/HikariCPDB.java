package scw.db.hikaricp;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import scw.core.annotation.Order;
import scw.db.AbstractDB;
import scw.db.DBUtils;
import scw.db.database.DataBase;
import scw.memcached.Memcached;
import scw.redis.Redis;
import scw.sql.orm.dialect.SqlDialect;
import scw.value.property.PropertyFactory;

public class HikariCPDB extends AbstractDB {
	private HikariDataSource hds;
	private DataBase dataBase;

	static {
		HikariConfig.class.getName();
	}

	@Order
	public HikariCPDB(String propertiesFile) {
		this(new PropertyFactory(false, true).loadProperties(propertiesFile, "UTF-8").registerListener());
	}

	@Order
	public HikariCPDB(String propertiesFile, Redis redis) {
		this(new PropertyFactory(false, true).loadProperties(propertiesFile, "UTF-8").registerListener(), redis);
	}

	@Order
	public HikariCPDB(String propertiesFile, Memcached memcached) {
		this(new PropertyFactory(false, true).loadProperties(propertiesFile, "UTF-8").registerListener(), memcached);
	}

	public HikariCPDB(PropertyFactory propertyFactory) {
		super();
		initConfig(propertyFactory);
	}

	public HikariCPDB(PropertyFactory propertyFactory, Memcached memcached) {
		super(propertyFactory, memcached);
		initConfig(propertyFactory);
	}

	public HikariCPDB(PropertyFactory propertyFactory, Redis redis) {
		super(propertyFactory, redis);
		initConfig(propertyFactory);
	}

	protected void initConfig(HikariConfig config) {
		this.dataBase = DBUtils.automaticRecognition(config.getDriverClassName(), config.getJdbcUrl(),
				config.getUsername(), config.getPassword());
		dataBase.create();
		hds = new HikariDataSource(config);
	}

	private void initConfig(PropertyFactory propertyFactory) {
		if (propertyFactory == null) {
			return;
		}
		HikariConfig config = new HikariConfig();
		DBUtils.loadProperties(config, propertyFactory);
		initConfig(config);
		createTableByProperties(propertyFactory);
	}

	public DataBase getDataBase() {
		return dataBase;
	}

	public Connection getConnection() throws SQLException {
		return hds.getConnection();
	}

	@Override
	public synchronized void destroy() {
		if (!hds.isClosed()) {
			hds.close();
		}
		super.destroy();
	}

	@Override
	public SqlDialect getSqlDialect() {
		return dataBase.getSqlDialect();
	}
}
