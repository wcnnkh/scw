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
import scw.core.utils.StringUtils;
import scw.db.AbstractDB.AsyncExecuteEvent;
import scw.event.EventListener;
import scw.event.support.BasicEvent;
import scw.event.support.DefaultAsyncBasicEventDispatcher;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.memcached.Memcached;
import scw.redis.Redis;
import scw.sql.ConnectionFactory;
import scw.sql.Sql;
import scw.sql.orm.Column;
import scw.sql.orm.TableChange;
import scw.sql.orm.annotation.Table;
import scw.sql.orm.cache.CacheManager;
import scw.sql.orm.cache.DefaultCacheManager;
import scw.sql.orm.cache.TemporaryCacheManager;
import scw.sql.orm.enums.OperationType;
import scw.sql.orm.support.AbstractEntityOperations;
import scw.sql.orm.support.generation.DefaultGeneratorService;
import scw.sql.orm.support.generation.GeneratorService;
import scw.sql.transaction.SqlTransactionUtils;
import scw.util.ClassScanner;
import scw.value.property.PropertyFactory;

public abstract class AbstractDB extends AbstractEntityOperations
		implements DB, EventListener<AsyncExecuteEvent>, BeanFactoryAware, Destroy, ConnectionFactory {
	protected final Logger logger = LoggerUtils.getLogger(getClass());
	private final DefaultAsyncBasicEventDispatcher<AsyncExecuteEvent> asyncBasicEventDispatcher = new DefaultAsyncBasicEventDispatcher<AsyncExecuteEvent>(
			false, getClass().getName(), true);
	private static final String DEFAULT_CACHE_PREFIX = "db:";

	private BeanFactory beanFactory;
	private CacheManager cacheManager;
	private GeneratorService generatorService;
	private boolean checkTableChange = true;

	{
		asyncBasicEventDispatcher.registerListener(this);
	}

	public AbstractDB() {
		this.cacheManager = new DefaultCacheManager();
		this.generatorService = new DefaultGeneratorService();
	}

	public AbstractDB(PropertyFactory propertyFactory, Memcached memcached) {
		this.cacheManager = new TemporaryCacheManager(memcached, true, getCachePrefix(propertyFactory));
		this.generatorService = new MemcachedGeneratorService(memcached);
	}

	public AbstractDB(PropertyFactory propertyFactory, Redis redis) {
		this.cacheManager = new TemporaryCacheManager(redis, true, getCachePrefix(propertyFactory));
		this.generatorService = new RedisGeneratorService(redis);
	}

	public AbstractDB(CacheManager cacheManager, GeneratorService generatorService) {
		this.cacheManager = cacheManager;
		this.generatorService = generatorService;
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

	protected String getCachePrefix(PropertyFactory propertyFactory) {
		return StringUtils.toString(propertyFactory.getString("cache.prefix"), "") + DEFAULT_CACHE_PREFIX;
	}

	protected void createTableByProperties(PropertyFactory propertyFactory) {
		if (propertyFactory == null) {
			return;
		}

		setCheckTableChange(propertyFactory.getValue("check.table.change", boolean.class, true));
		String create = StringUtils.toString(propertyFactory.getString("create"), null);
		if (StringUtils.isNotEmpty(create)) {
			createTable(create, propertyFactory.getValue("table.register.manager", boolean.class, true));
		}
	}

	@Override
	public GeneratorService getGeneratorService() {
		return generatorService;
	}

	@Override
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public boolean createTable(Class<?> tableClass, boolean registerManager) {
		return createTable(tableClass, null, registerManager);
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
				} catch (Exception e) {
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
