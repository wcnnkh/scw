package scw.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import scw.aop.ProxyUtils;
import scw.beans.BeanDefinition;
import scw.beans.BeanFactory;
import scw.beans.BeanFactoryAware;
import scw.beans.Destroy;
import scw.core.utils.CollectionUtils;
import scw.db.AbstractDB.AsyncExecuteEvent;
import scw.event.EventListener;
import scw.event.support.BasicEvent;
import scw.event.support.DefaultAsyncBasicEventDispatcher;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.sql.ConnectionFactory;
import scw.sql.Sql;
import scw.sql.orm.Column;
import scw.sql.orm.TableChange;
import scw.sql.orm.annotation.Table;
import scw.sql.orm.cache.CacheManager;
import scw.sql.orm.cache.DefaultCacheManager;
import scw.sql.orm.dialect.SqlDialect;
import scw.sql.orm.dialect.mysql.MySqlSqlDialect;
import scw.sql.orm.enums.OperationType;
import scw.sql.orm.support.AbstractEntityOperations;
import scw.sql.orm.support.generation.DefaultGeneratorService;
import scw.sql.orm.support.generation.GeneratorService;
import scw.sql.transaction.SqlTransactionUtils;
import scw.util.ClassScanner;

public abstract class AbstractDB extends AbstractEntityOperations
		implements DB, EventListener<AsyncExecuteEvent>, BeanFactoryAware, Destroy, ConnectionFactory {
	private static Logger logger = LoggerFactory.getLogger(AbstractDB.class);
	private final DefaultAsyncBasicEventDispatcher<AsyncExecuteEvent> asyncBasicEventDispatcher = new DefaultAsyncBasicEventDispatcher<AsyncExecuteEvent>(
			false, getClass().getName(), true);
	private volatile CacheManager cacheManager;
	private volatile GeneratorService generatorService;
	private BeanFactory beanFactory;
	private boolean checkTableChange = true;
	private volatile SqlDialect sqlDialect;

	{
		asyncBasicEventDispatcher.registerListener(this);
	}

	protected CacheManager createDefaultCacheManager() {
		if (beanFactory != null) {
			if (beanFactory.isInstance(CacheManager.class)) {
				return beanFactory.getInstance(CacheManager.class);
			}
		}
		return null;
	}

	public CacheManager getCacheManager() {
		if (cacheManager == null) {
			synchronized (this) {
				if (cacheManager == null) {
					cacheManager = createDefaultCacheManager();
					if (cacheManager == null) {
						cacheManager = new DefaultCacheManager();
					}
					logger.info("Create default cache management: {}", cacheManager);
				}
			}
		}
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	protected GeneratorService createDefaultGeneratorService() {
		if (beanFactory != null) {
			if (beanFactory.isInstance(GeneratorService.class)) {
				return beanFactory.getInstance(GeneratorService.class);
			}
		}
		return null;
	}

	public GeneratorService getGeneratorService() {
		if (generatorService == null) {
			synchronized (this) {
				if (generatorService == null) {
					generatorService = createDefaultGeneratorService();
					if (generatorService == null) {
						generatorService = new DefaultGeneratorService();
					}
					logger.info("Create default generator service: {}", generatorService);
				}
			}
		}
		return generatorService;
	}

	public void setGeneratorService(GeneratorService generatorService) {
		this.generatorService = generatorService;
	}

	protected SqlDialect createDefaultSqlDialect() {
		if (beanFactory != null) {
			if (beanFactory.isInstance(SqlDialect.class)) {
				return beanFactory.getInstance(SqlDialect.class);
			}
		}
		return null;
	}

	public SqlDialect getSqlDialect() {
		if (sqlDialect == null) {
			synchronized (this) {
				if (sqlDialect == null) {
					this.sqlDialect = createDefaultSqlDialect();
					if (sqlDialect == null) {
						sqlDialect = new MySqlSqlDialect();
					}
					logger.info("Create default sql dialect: {}", sqlDialect);
				}
			}
		}
		return sqlDialect;
	}

	public void setSqlDialect(SqlDialect sqlDialect) {
		this.sqlDialect = sqlDialect;
	}

	public boolean isCheckTableChange() {
		return checkTableChange;
	}

	public void setCheckTableChange(boolean checkTableChange) {
		this.checkTableChange = checkTableChange;
	}

	public void onEvent(AsyncExecuteEvent event) {
		processing(event.getAsyncExecute(), false);
	}

	public boolean createTable(Class<?> tableClass, boolean registerManager) {
		return createTable(tableClass, null, registerManager);
	}

	public final BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void processing(AsyncExecute asyncExecute, boolean isLogger) {
		if (asyncExecute == null) {
			return;
		}

		if (beanFactory != null) {
			Class<?> clazz = ProxyUtils.getProxyFactory().getUserClass(asyncExecute.getClass());
			BeanDefinition definition = beanFactory.getDefinition(clazz);
			if (definition != null) {
				try {
					definition.dependence(asyncExecute);
					definition.init(asyncExecute);
				} catch (Throwable e) {
					logger.error(e, "dependence {} error", clazz);
				}
			}
		}

		if (isLogger) {
			logger.info("async processing: {}", asyncExecute);
		}

		try {
			asyncExecute.execute(this);
		} catch (Exception e) {
			logger.error(e, "async processing error");
		}
	}

	public synchronized void destroy() {
		asyncBasicEventDispatcher.destroy();
	}

	@Override
	public boolean createTable(Class<?> tableClass, String tableName) {
		return createTable(tableClass, tableName, true);
	}

	public boolean createTable(Class<?> tableClass, String tableName, boolean registerManager) {
		if (registerManager) {
			DBManager.register(tableClass, this);
		}

		boolean b = super.createTable(tableClass, tableName);
		// 检查表变更
		if (isCheckTableChange()) {
			checkTableChange(tableClass);
		}
		return b;
	}

	@Override
	public void createTable(String packageName) {
		createTable(packageName, true);
	}

	public void createTable(String packageName, boolean registerManager) {
		Collection<Class<?>> list = ClassScanner.getInstance().getClasses(packageName);
		for (Class<?> tableClass : list) {
			Table table = tableClass.getAnnotation(Table.class);
			if (table == null) {
				continue;
			}

			if (registerManager) {
				DBManager.register(tableClass, this);
			}

			createTable(tableClass, false);
		}
	}

	// 检查表变更
	protected void checkTableChange(Class<?> tableClass) {
		TableChange tableChange = getTableChange(tableClass);
		List<String> addList = new LinkedList<String>();
		if (!CollectionUtils.isEmpty(tableChange.getAddColumnss())) {
			for (Column column : tableChange.getAddColumnss()) {
				addList.add(column.getName());
			}
		}

		if (!CollectionUtils.isEmpty(tableChange.getDeleteColumns()) || !CollectionUtils.isEmpty(addList)) {
			// 如果存在字段变更
			if (logger.isWarnEnabled()) {
				logger.warn("There are field changes class={}, addList={}, deleteList={}", tableClass.getName(),
						Arrays.toString(addList.toArray()), Arrays.toString(tableChange.getDeleteColumns().toArray()));
			}
		}
	}

	public final void asyncDelete(Object... objs) {
		TransactionAsyncExecute asyncExecute = new TransactionAsyncExecute();
		for (Object bean : objs) {
			asyncExecute.add(new BeanAsyncExecute(bean, OperationType.DELETE));
		}
		asyncExecute(asyncExecute);
	}

	public final void asyncExecute(Sql... sqls) {
		TransactionAsyncExecute asyncExecute = new TransactionAsyncExecute();
		for (Sql sql : sqls) {
			asyncExecute.add(new SqlAsyncExecute(sql));
		}
		asyncExecute(asyncExecute);
	}

	public final void asyncSave(Object... objs) {
		TransactionAsyncExecute asyncExecute = new TransactionAsyncExecute();
		for (Object bean : objs) {
			asyncExecute.add(new BeanAsyncExecute(bean, OperationType.SAVE));
		}
		asyncExecute(asyncExecute);
	}

	public final void asyncUpdate(Object... objs) {
		TransactionAsyncExecute asyncExecute = new TransactionAsyncExecute();
		for (Object bean : objs) {
			asyncExecute.add(new BeanAsyncExecute(bean, OperationType.UPDATE));
		}
		asyncExecute(asyncExecute);
	}

	public final void asyncSaveOrUpdate(Object... objs) {
		TransactionAsyncExecute asyncExecute = new TransactionAsyncExecute();
		for (Object bean : objs) {
			asyncExecute.add(new BeanAsyncExecute(bean, OperationType.SAVE_OR_UPDATE));
		}
		asyncExecute(asyncExecute);
	}

	public void asyncExecute(AsyncExecute asyncExecute) {
		if (!asyncBasicEventDispatcher.isStarted()) {
			throw new RuntimeException("Asynchronous processing has stopped!");
		}

		asyncBasicEventDispatcher.publishEvent(new AsyncExecuteEvent(asyncExecute));
	}

	@Deprecated
	public Select createSelect() {
		return new MysqlSelect(this);
	}

	@Override
	protected final Connection getUserConnection() throws SQLException {
		return SqlTransactionUtils.getTransactionConnection(this);
	}

	public static class AsyncExecuteEvent extends BasicEvent {
		private final AsyncExecute asyncExecute;

		public AsyncExecuteEvent(AsyncExecute asyncExecute) {
			this.asyncExecute = asyncExecute;
		}

		public AsyncExecute getAsyncExecute() {
			return asyncExecute;
		}
	}
}
