package scw.data.generator;

import scw.core.Assert;
import scw.core.utils.StringUtils;
import scw.core.utils.XTime;
import scw.data.Counter;

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
		this(new AtomicLongIdGenerator());
	}

	public SequenceIdGenerator(IdGenerator<Long> idGenerator) {
		this(idGenerator, DEFAULT_TIME_FORMAT);
	}
	
	public SequenceIdGenerator(Counter counter) {
		this(counter, DEFAULT_TIME_FORMAT);
	}

	public SequenceIdGenerator(Counter counter, String timeformat) {
		this(new CounterIdGenerator(counter, SequenceIdGenerator.class.getName() + "#" + timeformat, 0), timeformat);
	}

	public SequenceIdGenerator(IdGenerator<Long> idGenerator, String timeformat) {
		Assert.notNull(timeformat);
		Assert.notNull(idGenerator);
		this.idGenerator = idGenerator;
		this.time_format = timeformat;
	}

	public SequenceId next() {
		return next(System.currentTimeMillis());
	}
	
	public SequenceId next(long currentTimeMillis){
		int number = idGenerator.next().intValue();
		if (number < 0) {
			number = Integer.MAX_VALUE + number;
		}
		
		String id = XTime.format(currentTimeMillis, time_format) + StringUtils.complemented(number + "", '0', 10);
		return new SequenceId(currentTimeMillis, id);
	}
}
