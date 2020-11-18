package scw.data.memory;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import scw.beans.Destroy;
import scw.core.GlobalPropertyFactory;
import scw.data.cas.CAS;
import scw.logger.Logger;
import scw.logger.LoggerFactory;

public final class MemoryDataManager implements Destroy {
	// 单位：秒
	private static final int DEFAULT_CLEAR_PERIOD = GlobalPropertyFactory.getInstance()
			.getValue("memory.temporary.cache.clear.period", int.class, 60);
	private static final Timer TIMER = new Timer(MemoryDataManager.class.getSimpleName(), true);

	private static Logger logger = LoggerFactory.getLogger(MemoryDataManager.class);

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				TIMER.cancel();
			}
		});
	}

	private final ConcurrentHashMap<String, MemoryData> cacheMap = new ConcurrentHashMap<String, MemoryData>();
	private TimerTask timerTask;

	public MemoryDataManager() {
		this(DEFAULT_CLEAR_PERIOD);
	}

	/**
	 * @param clearPeriodSecond
	 *            单位：秒
	 */
	public MemoryDataManager(int clearPeriodSecond) {
		this.timerTask = new ClearExpireKeyTask();
		int period = Math.max(1, clearPeriodSecond);
		TIMER.schedule(timerTask, period * 1000L, period * 1000L);
	}

	public MemoryData getMemoryCache(String key) {
		MemoryData memoryData = cacheMap.get(key);
		if (memoryData == null) {
			return null;
		}

		if (memoryData.isExpire()) {
			return null;
		}

		return memoryData;
	}

	public MemoryData createDefaultMemoryCache(String key) {
		MemoryData memoryData = new DefaultMemoryData();
		MemoryData old = cacheMap.putIfAbsent(key, memoryData);
		return old == null ? memoryData : old;
	}

	public MemoryData createCounterMemoryCache(String key) {
		MemoryData memoryData = new CounterMemoryData();
		MemoryData old = cacheMap.putIfAbsent(key, memoryData);
		return old == null ? memoryData : old;
	}

	public boolean delete(String key) {
		return cacheMap.remove(key) != null;
	}

	public boolean delete(String key, long cas) {
		MemoryData memoryData = getMemoryCache(key);
		if (memoryData == null) {
			return false;
		}

		if (memoryData.incrCasAndCompare(cas)) {
			return cacheMap.remove(key) != null;
		}
		return false;
	}

	public void destroy() {
		if (timerTask != null) {
			timerTask.cancel();
		}
	}

	private final class ClearExpireKeyTask extends TimerTask {

		@Override
		public void run() {
			try {
				Iterator<Entry<String, MemoryData>> iterator = cacheMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, MemoryData> entry = iterator.next();
					MemoryData memoryData = entry.getValue();
					if (memoryData.isExpire()) {
						CAS<?> cas = memoryData.get();
						long c = 0;
						if (cas != null) {
							c = cas.getCas();
						}

						boolean b = memoryData.incrCasAndCompare(c);
						if (b) {
							iterator.remove();
							logger.debug("Deleting expired key:{}", entry.getKey());
						}
					}
				}
			} catch (Exception e) {
				logger.error(e, "clear expired key error");
			}
		}
	}
}
