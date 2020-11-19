package scw.db;

import java.util.concurrent.atomic.AtomicBoolean;

import scw.beans.BeanFactory;
import scw.core.Constants;
import scw.core.utils.StringUtils;
import scw.data.TemporaryCache;
import scw.db.database.DataBase;
import scw.lang.Nullable;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.sql.orm.cache.CacheManager;
import scw.sql.orm.cache.TemporaryCacheManager;
import scw.sql.orm.dialect.SqlDialect;
import scw.value.property.PropertyFactory;

public abstract class ConfigurableDB extends AbstractDB {
	private static Logger logger = LoggerFactory.getLogger(ConfigurableDB.class);
	private static final String DEFAULT_CACHE_PREFIX = "db:";
	private final AtomicBoolean init = new AtomicBoolean(false);
	private volatile PropertyFactory propertyFactory;
	private DataBase dataBase;
	private TemporaryCache temporaryCache;

	public ConfigurableDB() {
	}
	
	public ConfigurableDB(String configLocation) {
		this.propertyFactory = new PropertyFactory(false, true);
		loadProperties(configLocation);
	}

	public void setTemporaryCache(TemporaryCache temporaryCache) {
		this.temporaryCache = temporaryCache;
	}

	public void loadProperties(String configLocation) {
		if (propertyFactory == null) {
			synchronized (this) {
				if (propertyFactory == null) {
					this.propertyFactory = new PropertyFactory(false, true);
				}
			}
		}

		propertyFactory.loadProperties(configLocation, Constants.UTF_8_NAME).registerListener();
	}

	/**
	 * 可能为空
	 * 
	 * @return
	 */
	@Nullable
	public PropertyFactory getPropertyFactory() {
		return propertyFactory;
	}

	public void setPropertyFactory(PropertyFactory propertyFactory) {
		this.propertyFactory = propertyFactory;
	}

	public DataBase getDataBase() {
		return dataBase;
	}

	public void setDataBase(DataBase dataBase) {
		this.dataBase = dataBase;
	}
	
	@Override
	public SqlDialect getSqlDialect() {
		DataBase dataBase = getDataBase();
		if(dataBase != null){
			return dataBase.getSqlDialect();
		}
		return super.getSqlDialect();
	}
	
	private TemporaryCache getTemporaryCache(){
		if(temporaryCache != null){
			return temporaryCache;
		}
		
		BeanFactory beanFactory = getBeanFactory();
		if (beanFactory != null) {
			if (beanFactory.isInstance(TemporaryCache.class)) {
				return beanFactory.getInstance(TemporaryCache.class);
			}
		}
		return null;
	}

	@Override
	protected CacheManager createDefaultCacheManager() {
		TemporaryCache temporaryCache = getTemporaryCache();
		if(temporaryCache != null){
			String keyPrefix = getCachePrefix();
			logger.info("Use temporary cache [{}], key prefix [{}]", temporaryCache, keyPrefix);
			return new TemporaryCacheManager(temporaryCache, true, keyPrefix);
		}
		return super.createDefaultCacheManager();
	}

	protected String getCachePrefix() {
		if (propertyFactory == null) {
			return DEFAULT_CACHE_PREFIX;
		}
		return StringUtils.toString(propertyFactory.getString("cache.prefix"), "") + DEFAULT_CACHE_PREFIX;
	}
	
	public boolean isInitialized(){
		return init.get();
	}

	public final boolean initializing() {
		if (init.get()) {
			return false;
		}

		if (init.compareAndSet(false, true)) {
			initConfig();
			return true;
		}

		return false;
	}

	protected void initConfig() {
		if (dataBase != null) {
			dataBase.create();
		}

		if (propertyFactory == null) {
			return;
		}

		setCheckTableChange(propertyFactory.getValue("check.table.change", boolean.class, true));
		String create = StringUtils.toString(propertyFactory.getString("create"), null);
		if (StringUtils.isNotEmpty(create)) {
			createTable(create, propertyFactory.getValue("table.register.manager", boolean.class, true));
		}
	}
}
