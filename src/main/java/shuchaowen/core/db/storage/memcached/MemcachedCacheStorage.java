package shuchaowen.core.db.storage.memcached;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import shuchaowen.core.cache.Memcached;
import shuchaowen.core.db.AbstractDB;
import shuchaowen.core.db.PrimaryKeyParameter;
import shuchaowen.core.db.PrimaryKeyValue;
import shuchaowen.core.db.result.Result;
import shuchaowen.core.db.result.ResultIterator;
import shuchaowen.core.db.storage.CacheUtils;
import shuchaowen.core.db.storage.CommonStorage;
import shuchaowen.core.db.storage.Storage;
import shuchaowen.core.exception.ShuChaoWenRuntimeException;
import shuchaowen.core.util.ClassUtils;
import shuchaowen.core.util.Logger;

public class MemcachedCacheStorage extends CommonStorage{
	private final Map<String, Boolean> loadKeyTagMap = new HashMap<String, Boolean>();
	private final Memcached memcached;
	
	public MemcachedCacheStorage(AbstractDB db, Memcached memcached, Storage execute) {
		super(db, null, execute);
		this.memcached = memcached;
	}
	
	/**
	 * 将此表的主键交给缓存来管理
	 * 
	 * @param tableClass
	 */
	public final void loadKeysToCache(Class<?> tableClass) {
		String name = ClassUtils.getCGLIBRealClassName(tableClass);
		if (loadKeyTagMap.containsKey(name)) {
			throw new ShuChaoWenRuntimeException(name + " Already exist");
		}

		synchronized (loadKeyTagMap) {
			if (loadKeyTagMap.containsKey(name)) {
				throw new ShuChaoWenRuntimeException(name + " Already exist");
			}

			loadKeyTagMap.put(name, null);
		}

		// loader
		loadTableKeysToCache(tableClass);
	}

	protected void loadTableKeysToCache(final Class<?> tableClass) {
		final String name = ClassUtils.getCGLIBRealClassName(tableClass);
		Logger.info("loading [" + name + "] keys to cache");
		getDb().iterator(tableClass, new ResultIterator() {

			public void next(Result result) {
				Object bean = result.get(tableClass);
				saveToCache(Arrays.asList(bean));
			}
		});
	}
	
	public <T> Map<PrimaryKeyParameter, T> getByIdFromCache(Class<T> type,
			Collection<PrimaryKeyParameter> primaryKeyParameters) {
		Map<String, PrimaryKeyParameter> keyMap = new HashMap<String, PrimaryKeyParameter>();
		for (PrimaryKeyParameter parameter : primaryKeyParameters) {
			keyMap.put(CacheUtils.getObjectKey(type, parameter), parameter);
		}

		Map<String, byte[]> map = memcached.get(keyMap.keySet());
		if (map == null || map.isEmpty()) {
			return null;
		}

		Map<PrimaryKeyParameter, T> dataMap = new HashMap<PrimaryKeyParameter, T>();
		for (Entry<String, byte[]> entry : map.entrySet()) {
			dataMap.put(keyMap.get(entry.getKey()), CacheUtils.decode(type, entry.getValue()));
		}
		return dataMap;
	}

	public void saveToCache(Collection<?> beans) {
		for (Object bean : beans) {
			String name = ClassUtils.getCGLIBRealClassName(bean.getClass());
			if(loadKeyTagMap.containsKey(name)){
				memcached.add(getObjectKey(bean), CacheUtils.encode(bean));
			}
		}
	}

	public void updateToCache(Collection<?> beans){
		for (Object bean : beans) {
			String name = ClassUtils.getCGLIBRealClassName(bean.getClass());
			if(loadKeyTagMap.containsKey(name)){
				memcached.add(getObjectKey(bean), CacheUtils.encode(bean));
			}
		}
	}

	public void saveOrUpdateToCache(Collection<?> beans){
		for (Object bean : beans) {
			String name = ClassUtils.getCGLIBRealClassName(bean.getClass());
			if(loadKeyTagMap.containsKey(name)){
				memcached.set(getObjectKey(bean), CacheUtils.encode(bean));
			}
		}
	}

	public void deleteToCache(Collection<?> beans) {
		for (Object bean : beans) {
			String name = ClassUtils.getCGLIBRealClassName(bean.getClass());
			if(loadKeyTagMap.containsKey(name)){
				memcached.delete(getObjectKey(bean));
			}
		}
	}

	protected String getObjectKey(Object bean) {
		try {
			return CacheUtils.getObjectKey(bean);
		} catch (Exception e) {
			throw new ShuChaoWenRuntimeException(e);
		}
	}

	@Override
	public <T> T getById(Class<T> type, Object... params) {
		String name = ClassUtils.getCGLIBRealClassName(type);
		if(loadKeyTagMap.containsKey(name)){
			byte[] data = memcached.get(CacheUtils.getObjectKey(type, params));
			if(data == null){
				return null;
			}
			
			return CacheUtils.decode(type, data);
		}else{
			return super.getById(type, params);
		}
	}

	@Override
	public <T> PrimaryKeyValue<T> getById(Class<T> type,
			Collection<PrimaryKeyParameter> primaryKeyParameters) {
		Map<String, PrimaryKeyParameter> keyMap = new HashMap<String, PrimaryKeyParameter>();
		for(PrimaryKeyParameter parameter : primaryKeyParameters){
			keyMap.put(CacheUtils.getObjectKey(type, parameter), parameter);
		}
		
		PrimaryKeyValue<T> primaryKeyValue = new PrimaryKeyValue<T>();
		Map<String, byte[]> cacheMap = memcached.get(keyMap.keySet());
		if(cacheMap != null && !cacheMap.isEmpty()){
			for(Entry<String, byte[]> entry : cacheMap.entrySet()){
				primaryKeyValue.put(keyMap.get(entry.getKey()), CacheUtils.decode(type, entry.getValue()));
			}
		}
		return primaryKeyValue;
	}

	@Override
	public void save(Collection<?> beans) {
		super.save(beans);
		saveToCache(beans);
	}

	@Override
	public void delete(Collection<?> beans) {
		super.delete(beans);
		deleteToCache(beans);
	}

	@Override
	public void update(Collection<?> beans) {
		super.update(beans);
		updateToCache(beans);
	}

	@Override
	public void saveOrUpdate(Collection<?> beans) {
		super.saveOrUpdate(beans);
		saveOrUpdateToCache(beans);
	}
}
