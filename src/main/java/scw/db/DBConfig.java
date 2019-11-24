package scw.db;

import java.util.Collection;

import scw.beans.annotation.AutoImpl;
import scw.db.cache.CacheManager;
import scw.db.database.DataBase;
import scw.mq.queue.Queue;
import scw.sql.orm.ORMFilter;
import scw.transaction.sql.ConnectionFactory;

@AutoImpl(className={"scw.db.support.HikariCPDBConfig", "scw.db.support.DruidDBConfig"})
public interface DBConfig extends ConnectionFactory{
	DataBase getDataBase();
	
	String getSannerTablePackage();
	
	Queue<AsyncExecute> getAsyncQueue();
	
	CacheManager getCacheManager();

	Collection<ORMFilter> getORMFilter();
}