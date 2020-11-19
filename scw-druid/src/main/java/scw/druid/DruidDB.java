package scw.druid;

import java.sql.Connection;
import java.sql.SQLException;

import com.alibaba.druid.pool.DruidDataSource;

import scw.db.ConfigurableDB;
import scw.db.DBUtils;

public class DruidDB extends ConfigurableDB {
	static {
		DruidDataSource.class.getName();
	}

	private DruidDataSource dataSource;
	
	public DruidDB(String configLocation) {
		super(configLocation);
		initializing();
	}
	
	public DruidDataSource getDatasource() {
		return dataSource;
	}
	
	@Override
	protected void initConfig() {
		if(getPropertyFactory() != null && dataSource == null){
			dataSource = new DruidDataSource();
			DBUtils.loadProperties(dataSource, getPropertyFactory());
			if (!dataSource.isPoolPreparedStatements()) {// 如果配置中没有开启psCache
				dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
			}
			dataSource.setRemoveAbandoned(false);
		}
		
		if(dataSource != null){
			setDataBase(DBUtils.automaticRecognition(dataSource.getDriverClassName(), dataSource.getUrl(),
					dataSource.getUsername(), dataSource.getPassword()));
		}
		super.initConfig();
	}

	public synchronized void destroy() {
		if (dataSource != null && !dataSource.isClosed()) {
			dataSource.close();
		}
		super.destroy();
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
}
