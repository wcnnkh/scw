package scw.sql.orm.support;

import java.sql.Connection;
import java.sql.SQLException;

import scw.sql.ConnectionFactory;
import scw.sql.orm.cache.CacheManager;
import scw.sql.orm.cache.DefaultCacheManager;
import scw.sql.orm.dialect.SqlDialect;
import scw.sql.orm.support.generation.DefaultGeneratorService;
import scw.sql.orm.support.generation.GeneratorService;
import scw.sql.transaction.SqlTransactionUtils;

public class DefaultEntityOperations extends AbstractEntityOperations {
	private SqlDialect sqlDialect;
	private CacheManager cacheManager;
	private GeneratorService generatorService;
	private ConnectionFactory connectionFactory;

	public DefaultEntityOperations(SqlDialect sqlDialect, ConnectionFactory connectionFactory) {
		this(sqlDialect, new DefaultCacheManager(), new DefaultGeneratorService(), connectionFactory);
	}

	public DefaultEntityOperations(SqlDialect sqlDialect, CacheManager cacheManager, GeneratorService generatorService,
			ConnectionFactory connectionFactory) {
		this.sqlDialect = sqlDialect;
		this.cacheManager = cacheManager;
		this.generatorService = generatorService;
		this.connectionFactory = connectionFactory;
	}

	@Override
	public SqlDialect getSqlDialect() {
		return sqlDialect;
	}

	@Override
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	@Override
	public GeneratorService getGeneratorService() {
		return generatorService;
	}

	public Connection getUserConnection() throws SQLException {
		return SqlTransactionUtils.getTransactionConnection(connectionFactory);
	}
}
