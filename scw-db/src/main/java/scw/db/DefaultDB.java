package scw.db;

import java.sql.Connection;
import java.sql.SQLException;

import scw.sql.ConnectionFactory;
import scw.sql.orm.dialect.SqlDialect;

public class DefaultDB extends AbstractDB {
	private final ConnectionFactory connectionFactory;
	private final SqlDialect sqlDialect;

	public DefaultDB(ConnectionFactory connectionFactory, SqlDialect sqlDialect) {
		this.connectionFactory = connectionFactory;
		this.sqlDialect = sqlDialect;
	}

	public Connection getConnection() throws SQLException {
		return connectionFactory.getConnection();
	}

	@Override
	public SqlDialect getSqlDialect() {
		return sqlDialect;
	}
}
