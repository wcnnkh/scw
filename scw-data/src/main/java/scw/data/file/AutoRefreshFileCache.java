package scw.data.file;

import java.io.File;

import scw.core.Converter;
import scw.io.NoTypeSpecifiedSerializer;
import scw.io.SerializerUtils;

public class AutoRefreshFileCache extends FileCache {
	private final Converter<String, ?> converter;

	/**
	 * @param period 单位:秒
	 * @param converter
	 */
	protected AutoRefreshFileCache(int period, Converter<String, ?> converter) {
		super(period);
		this.converter = converter;
	}

	/**
	 * @param period 单位:秒
	 * @param cacheDirectory
	 * @param converter
	 */
	public AutoRefreshFileCache(int period, String cacheDirectory, Converter<String, ?> converter) {
		this(period, SerializerUtils.DEFAULT_SERIALIZER, cacheDirectory, converter);
	}

	/**
	 * @param period 单位:秒
	 * @param serializer
	 * @param charsetName
	 * @param cacheDirectory
	 * @param converter
	 */
	public AutoRefreshFileCache(int period, NoTypeSpecifiedSerializer serializer,
			String cacheDirectory, Converter<String, ?> converter) {
		super(period, serializer, cacheDirectory);
		this.converter = converter;
	}

	@Override
	protected void expireExecute(File file, long currentTimeMillis) {
		String key = getKey(file);
		refresh(key);
	}

	private Object refresh(String key) {
		Object value = converter.convert(key);
		if (value != null) {
			set(key, value);
		}
		return value;
	}

	@Override
	protected Object getNotFound(String key) {
		return refresh(key);
	}

	@Override
	protected File getNotExpireFile(String key) {
		File file = getFile(key);
		if (file.exists()) {
			return file;
		}
		return null;
	}
}
