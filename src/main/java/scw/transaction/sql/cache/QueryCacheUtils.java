package scw.transaction.sql.cache;

import java.util.List;

import scw.sql.ResultSetMapper;
import scw.sql.RowMapper;
import scw.sql.Sql;
import scw.sql.SqlOperations;
import scw.transaction.Transaction;
import scw.transaction.TransactionManager;

/**
 * 事务内的查询缓存
 * 
 * @author shuchaowen
 *
 */
public final class QueryCacheUtils {
	private static volatile boolean cacheEnable = true;

	private QueryCacheUtils() {
	};
	
	public static boolean isGlobalCacheEnable(){
		return cacheEnable;
	}

	public static void setGlobalCacheEnable(boolean enable) {
		cacheEnable = enable;
	}

	public static boolean queryCacheEnable() {
		if (!cacheEnable) {
			return false;
		}

		Transaction transaction = TransactionManager.getCurrentTransaction();
		if (transaction == null) {
			return false;
		}

		MultipleConnectionQueryCache cache = (MultipleConnectionQueryCache) transaction
				.getResource(MultipleConnectionQueryCache.class);
		if (cache == null) {
			return false;
		}

		return cache.isEnable();
	}

	public static void setQueryCacheEnable(boolean enable) {
		Transaction transaction = TransactionManager.getCurrentTransaction();
		if (transaction == null) {
			return;
		}

		MultipleConnectionQueryCache cache = (MultipleConnectionQueryCache) transaction
				.getResource(MultipleConnectionQueryCache.class);
		if (cache == null) {
			return;
		}

		cache.setEnable(enable);
	}

	private static MultipleConnectionQueryCache getMultipleConnectionQueryCache(SqlOperations sqlOperations) {
		if(!cacheEnable){
			return null;
		}
		
		Transaction transaction = TransactionManager.getCurrentTransaction();
		if (transaction == null) {
			return null;
		}

		MultipleConnectionQueryCache cache = (MultipleConnectionQueryCache) transaction
				.getResource(MultipleConnectionQueryCache.class);
		if (cache == null) {
			cache = new MultipleConnectionQueryCache();
			transaction.bindResource(MultipleConnectionQueryCache.class, cache);
		}
		return cache;
	}

	public static <T> T query(SqlOperations sqlOperations, Sql sql, ResultSetMapper<T> resultSetMapper) {
		MultipleConnectionQueryCache cache = getMultipleConnectionQueryCache(sqlOperations);
		if (cache == null) {
			return sqlOperations.query(sql, resultSetMapper);
		} else {
			return cache.query(sqlOperations, sql, resultSetMapper);
		}
	}

	public static <T> List<T> query(SqlOperations sqlOperations, Sql sql, RowMapper<T> rowMapper) {
		MultipleConnectionQueryCache cache = getMultipleConnectionQueryCache(sqlOperations);
		if (cache == null) {
			return sqlOperations.query(sql, rowMapper);
		} else {
			return cache.query(sqlOperations, sql, rowMapper);
		}
	}
}
