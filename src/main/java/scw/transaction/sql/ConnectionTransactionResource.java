package scw.transaction.sql;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import scw.sql.ConnectionFactory;
import scw.sql.Sql;
import scw.sql.SqlUtils;
import scw.transaction.Isolation;
import scw.transaction.Transaction;
import scw.transaction.TransactionDefinition;
import scw.transaction.TransactionException;
import scw.transaction.TransactionResource;
import scw.transaction.savepoint.ConnectionSavepoint;

public final class ConnectionTransactionResource implements TransactionResource {
	private static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";
	private final ConnectionFactory connectionFactory;
	private final TransactionDefinition transactionDefinition;
	private Connection connection;
	private int savepointCounter;
	private boolean active;
	private LinkedHashMap<String, Sql> sqlMap;

	public ConnectionTransactionResource(ConnectionFactory connectionFactory,
			TransactionDefinition transactionDefinition, boolean active) {
		this.active = active;
		this.connectionFactory = connectionFactory;
		this.transactionDefinition = transactionDefinition;
	}

	public ConnectionTransactionResource(ConnectionFactory connectionFactory,
			Transaction transaction) {
		this.active = transaction.isActive();
		this.connectionFactory = connectionFactory;
		this.transactionDefinition = transaction.getTransactionDefinition();
	}

	public boolean hasConnection() {
		return connection != null;
	}

	public Connection getConnection() throws SQLException {
		if (connection == null) {
			connection = connectionFactory.getConnection();
			connection.setAutoCommit(!active);

			if (transactionDefinition.isReadOnly()) {
				connection.setReadOnly(transactionDefinition.isReadOnly());
			}

			Isolation isolation = transactionDefinition.getIsolation();
			if (isolation != Isolation.DEFAULT) {
				connection.setTransactionIsolation(isolation.getLevel());
			}

			connection = (ConnectionProxy) Proxy.newProxyInstance(
					ConnectionProxy.class.getClassLoader(),
					new Class<?>[] { ConnectionProxy.class },
					new UnableToCloseConnectionProxyHandler(connection));
		}
		return connection;
	}

	public void addSql(Sql sql) {
		if (sql == null) {
			return;
		}

		if (sqlMap == null) {
			sqlMap = new LinkedHashMap<String, Sql>(8);
		}

		sqlMap.put(SqlUtils.getSqlId(sql), sql);
	}

	public void setActive(boolean active) {
		if (hasConnection()) {
			try {
				connection.setAutoCommit(!active);
			} catch (SQLException e) {
				throw new TransactionException(e);
			}
		}
		setActive(active);
	}

	public scw.transaction.savepoint.Savepoint createSavepoint()
			throws TransactionException {
		savepointCounter++;
		return new ConnectionSavepoint(connection, SAVEPOINT_NAME_PREFIX
				+ savepointCounter);
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void end() {
		if (hasConnection()) {
			try {
				connection.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (active) {
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			try {
				if (connection instanceof ConnectionProxy) {
					((ConnectionProxy) connection).getTargetConnection()
							.close();
				} else {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void rollback() {
		if (hasConnection()) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				throw new TransactionException(e);
			}
		}
	}

	public void process() {
		if (sqlMap != null && !sqlMap.isEmpty()) {
			try {
				connection = getConnection();
				for (Entry<String, Sql> entry : sqlMap.entrySet()) {
					PreparedStatement preparedStatement = SqlUtils
							.createPreparedStatement(connection,
									entry.getValue());
					try {
						preparedStatement.execute();
					} catch (SQLException e) {
						throw new TransactionException(SqlUtils.getSqlId(entry
								.getValue()), e);
					} finally {
						try {
							preparedStatement.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (SQLException e) {
				throw new TransactionException(e);
			}
		}
	}
}