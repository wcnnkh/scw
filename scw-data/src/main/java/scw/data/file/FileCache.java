package scw.data.file;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import scw.beans.Destroy;
import scw.beans.Init;
import scw.core.GlobalPropertyFactory;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.data.ExpiredCache;
import scw.io.FileUtils;
import scw.io.NoTypeSpecifiedSerializer;
import scw.io.SerializerUtils;
import scw.lang.NestedRuntimeException;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.net.uri.UriUtils;
import scw.value.property.SystemPropertyFactory;

@SuppressWarnings("unchecked")
public class FileCache extends TimerTask implements ExpiredCache, Init, Destroy {
	private static Logger logger = LoggerFactory.getLogger(FileCache.class);
	private Timer timer;
	private final int exp;// 0表示不过期
	private final NoTypeSpecifiedSerializer serializer;
	private final String cacheDirectory;

	/**
	 * @param exp
	 *            单位:秒
	 */
	protected FileCache(int exp) {
		this(exp, SerializerUtils.DEFAULT_SERIALIZER,
				SystemPropertyFactory.getInstance().getTempDirectoryPath()
						+ GlobalPropertyFactory.getInstance().getSystemLocalId() + File.separator + "file_cache_"
						+ exp);
	}

	/**
	 * @param exp
	 *            单位:秒
	 * @param cacheDirectory
	 */
	public FileCache(int exp, String cacheDirectory) {
		this(exp, SerializerUtils.DEFAULT_SERIALIZER, cacheDirectory);
	}

	/**
	 * @param exp
	 *            单位:秒
	 * @param serializer
	 * @param charsetName
	 * @param cacheDirectory
	 */
	public FileCache(int exp, NoTypeSpecifiedSerializer serializer, String cacheDirectory) {
		this.exp = exp;
		this.serializer = serializer;
		this.cacheDirectory = StringUtils.cleanPath(cacheDirectory);
		logger.info("{} exp is {} use cache directory: {}", getClass().getName(), exp, this.cacheDirectory);
	}

	public void init() {
		if (exp > 0) {
			timer = new Timer(getClass().getName());
			timer.schedule(this, exp * 1000L, exp * 1000L);
		}
	}

	public final int getExp() {
		return exp;
	}

	public final NoTypeSpecifiedSerializer getSerializer() {
		return serializer;
	}

	public final String getCacheDirectory() {
		return cacheDirectory;
	}

	protected final String getKey(File file) {
		return UriUtils.decode(file.getName());
	}

	protected final File getFile(String key) {
		StringBuilder sb = new StringBuilder();
		sb.append(cacheDirectory);
		sb.append(File.separator);
		sb.append(hashPath(key));
		sb.append(File.separator);
		sb.append(UriUtils.encode(key));
		return new File(sb.toString());
	}

	protected int hashPath(String key) {
		return key.hashCode() % 1024;
	}

	protected final Object readObject(File file) {
		if (file.exists()) {
			byte[] data;
			try {
				data = FileUtils.readFileToByteArray(file);
				return serializer.deserialize(data);
			} catch (Exception e) {
				throw new NestedRuntimeException(e);
			}
		}
		return null;
	}

	protected final void writeObject(File file, Object value, boolean touch) {
		try {
			FileUtils.writeByteArrayToFile(file, serializer.serialize(value));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (touch) {
			touchFile(file);
		}
	}

	protected final void touchFile(File file) {
		if (!file.setLastModified(System.currentTimeMillis())) {
			logger.warn("touch file fail:{}", file.getPath());
		}
	}

	protected final boolean isExpire(File file) {
		if (exp <= 0) {
			return false;
		}

		return System.currentTimeMillis() - file.lastModified() > exp;
	}

	protected File getNotExpireFile(String key) {
		File file = getFile(key);
		if (!file.exists()) {
			return null;
		}

		if (isExpire(file)) {
			return null;
		}
		return file;
	}

	protected Object getNotFound(String key) {
		return null;
	}

	public <T> T get(String key) {
		File file = getNotExpireFile(key);
		if (file == null) {
			return (T) getNotFound(key);
		}

		return (T) readObject(file);
	}

	public <T> T getAndTouch(String key) {
		File file = getNotExpireFile(key);
		if (file == null) {
			return (T) getNotFound(key);
		}

		touchFile(file);
		return (T) readObject(file);
	}

	public void set(String key, Object value) {
		File file = getNotExpireFile(key);
		writeObject(getFile(key), value, file == null);
	}

	public boolean add(String key, Object value) {
		File file = getNotExpireFile(key);
		if (file == null) {
			writeObject(getFile(key), value, true);
		}
		return false;
	}

	public boolean touch(String key) {
		File file = getNotExpireFile(key);
		if (file == null) {
			return false;
		}

		touchFile(file);
		return true;
	}

	public boolean delete(String key) {
		File file = getFile(key);
		if (file.exists()) {
			return file.delete();
		}
		return false;
	}

	public boolean isExist(String key) {
		return getNotExpireFile(key) != null;
	}

	public <T> Map<String, T> get(Collection<String> keyCollections) {
		if (CollectionUtils.isEmpty(keyCollections)) {
			return Collections.EMPTY_MAP;
		}

		Map<String, T> map = new HashMap<String, T>(keyCollections.size());
		for (String key : keyCollections) {
			T value = get(key);
			if (value == null) {
				continue;
			}

			map.put(key, value);
		}
		return map;
	}

	/**
	 * 过期后会调用此函数
	 * 
	 * @param file
	 * @param currentTimeMillis
	 */
	protected void expireExecute(File file, long currentTimeMillis) {
		file.delete();
	}

	protected void scanner(File file, long currentTimeMillis) {
		if (isExpire(file)) {
			try {
				logger.debug("Start processing expired cache:" + file.getPath());
				expireExecute(file, currentTimeMillis);
			} catch (Exception e) {
				logger.error(e, "处理[{}]异常", file.getPath());
			}
		}
	}

	private void sannerExpireFile(String rootPath, long currentTimeMillis) {
		File rootFile = new File(rootPath);
		if (!rootFile.exists()) {
			return;
		}

		for (File file : rootFile.listFiles()) {
			if (file == null) {
				continue;
			}

			if (file.isDirectory()) {
				sannerExpireFile(file.getPath(), currentTimeMillis);
			} else {
				scanner(file, currentTimeMillis);
			}
		}
	}

	@Override
	public void run() {
		try {
			sannerExpireFile(cacheDirectory, scheduledExecutionTime());
		} catch (Exception e) {
			logger.error(e, "执行过期缓存扫描异常");
		}
	}

	public void destroy() {
		cancel();
		if (timer != null) {
			timer.cancel();
		}
	}

	public static ExpiredCache create(String cacheDirectorySuffix, int exp) {
		return new FileCache(exp,
				SystemPropertyFactory.getInstance().getTempDirectoryPath() + File.separator + cacheDirectorySuffix);
	}

	public void delete(Collection<String> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return;
		}

		for (String key : keys) {
			delete(key);
		}
	}

	public int getMaxExpirationDate() {
		return exp;
	}
}
