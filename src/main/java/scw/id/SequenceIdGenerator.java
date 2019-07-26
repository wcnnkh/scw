package scw.id;

import scw.core.utils.Assert;
import scw.core.utils.StringUtils;
import scw.core.utils.XTime;
import scw.data.memcached.Memcached;
import scw.data.redis.Redis;

/**
 * 一个纯数字的流水号生成器,最后10位是一个定长的流水号，除去最后10位，前面剩下的是时间格式化后的结果
 * 
 * @author shuchaowen
 *
 */
public final class SequenceIdGenerator implements IdGenerator<SequenceId> {
	private static final String DEFAULT_TIME_FORMAT = "yyyyMMddHHmmss";
	private final IdGenerator<Long> idGenerator;
	private final String time_format;

	public SequenceIdGenerator() {
		this.idGenerator = new LongIdGenerator();
		this.time_format = DEFAULT_TIME_FORMAT;
	}

	public SequenceIdGenerator(Memcached memcached) {
		this.idGenerator = new MemcachedIdGenerator(memcached, this.getClass()
				.getName(), 0);
		this.time_format = DEFAULT_TIME_FORMAT;
	}

	public SequenceIdGenerator(Redis redis) {
		this.idGenerator = new RedisIdGenerator(redis, this.getClass()
				.getName(), 0);
		this.time_format = DEFAULT_TIME_FORMAT;
	}

	public SequenceIdGenerator(Memcached memcached, String key,
			String timeformat) {
		Assert.notNull(timeformat);
		Assert.notNull(key);
		this.idGenerator = new MemcachedIdGenerator(memcached, key, 0);
		this.time_format = timeformat;
	}

	public SequenceIdGenerator(Redis redis, String key, String timeformat) {
		Assert.notNull(timeformat);
		Assert.notNull(key);
		this.idGenerator = new RedisIdGenerator(redis, key, 0);
		this.time_format = timeformat;
	}

	public SequenceId next() {
		long t = System.currentTimeMillis();
		int number = idGenerator.next().intValue();
		if (number < 0) {
			number = Integer.MAX_VALUE + number;
		}

		String id = XTime.format(t, time_format)
				+ StringUtils.complemented(number + "", '0', 10);
		return new SequenceId(t, id);
	}
}