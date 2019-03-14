package scw.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;

import scw.beans.annotaion.Destroy;
import scw.common.utils.ConfigUtils;
import scw.common.utils.PropertiesUtils;
import scw.common.utils.StringUtils;
import scw.sql.orm.SqlFormat;
import scw.sql.orm.cache.Cache;

public final class DruidDB extends AbstractDB {
	private DruidDataSource datasource;

	/**
	 * 数据库配置文件目录 只支持mysql
	 * 
	 * @param propertiesFilePath
	 */
	public DruidDB(SqlFormat sqlFormat, String propertiesFilePath) {
		this(sqlFormat, propertiesFilePath, "UTF-8");
	}

	public DruidDB(SqlFormat sqlFormat, String propertiesFilePath, String charsetName) {
		this(null, sqlFormat, propertiesFilePath, charsetName);
	}

	public DruidDB(Cache cache, SqlFormat sqlFormat, String propertiesFilePath, String charsetName) {
		super(sqlFormat, cache);
		Properties properties = ConfigUtils.getProperties(propertiesFilePath, charsetName);
		String url = PropertiesUtils.getProperty(properties, "jdbcUrl", "url", "host");
		String username = PropertiesUtils.getProperty(properties, "username", "user", "name");
		String password = PropertiesUtils.getProperty(properties, "password", "pwd");
		String minSize = PropertiesUtils.getProperty(properties, "minSize", "initialSize", "min");
		String maxSize = PropertiesUtils.getProperty(properties, "maxSize", "maxActive", "max");
		String driver = PropertiesUtils.getProperty(properties, "driver", "driverClass", "driverClassName");
		String maxPoolPreparedStatementPerConnectionSize = PropertiesUtils.getProperty(properties,
				"maxPoolPreparedStatementPerConnectionSize");
		datasource = new DruidDataSource();
		datasource.setUrl(url);
		datasource.setDriverClassName(StringUtils.isEmpty(driver) ? "com.mysql.jdbc.Driver" : driver);
		datasource.setUsername(username);
		datasource.setPassword(password);
		datasource.setInitialSize(StringUtils.isEmpty(minSize) ? 10 : Integer.parseInt(minSize));
		datasource.setMinIdle(StringUtils.isEmpty(minSize) ? 10 : Integer.parseInt(minSize));
		datasource.setMaxActive(StringUtils.isEmpty(maxSize) ? 100 : Integer.parseInt(maxSize));
		datasource.setMaxPoolPreparedStatementPerConnectionSize(
				StringUtils.isEmpty(maxPoolPreparedStatementPerConnectionSize) ? 20
						: Integer.parseInt(maxPoolPreparedStatementPerConnectionSize));
	}

	public Connection getConnection() throws SQLException {
		return datasource.getConnection();
	}

	@Destroy
	public void close() throws Exception {
		datasource.close();
	}

}
