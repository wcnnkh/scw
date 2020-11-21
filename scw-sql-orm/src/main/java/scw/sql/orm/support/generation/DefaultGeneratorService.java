package scw.sql.orm.support.generation;

import scw.aop.ProxyUtils;
import scw.core.instance.annotation.Configuration;
import scw.data.Counter;
import scw.data.generator.SequenceId;
import scw.data.generator.SequenceIdGenerator;
import scw.data.memory.MemoryDataTemplete;
import scw.locks.JdkLockFactory;
import scw.locks.Lock;
import scw.locks.LockFactory;

@Configuration(order=Integer.MIN_VALUE)
public class DefaultGeneratorService extends AbstractGeneratorService {
	private final SequenceIdGenerator sequeueIdGenerator;
	private final Counter counter;
	private final LockFactory lockFactory;
	
	public DefaultGeneratorService(){
		this(new SequenceIdGenerator(), new MemoryDataTemplete(), new JdkLockFactory());
	}

	public DefaultGeneratorService(SequenceIdGenerator sequeueIdGenerator, Counter counter,
			LockFactory lockFactory) {
		this.sequeueIdGenerator = sequeueIdGenerator;
		this.counter = counter;
		this.lockFactory = lockFactory;
	}

	protected String getCacheKey(GeneratorContext generatorContext) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("generator:");
		sb.append(generatorContext.getColumn().getField().getSetter().getDeclaringClass().getName());
		sb.append("&");
		sb.append(generatorContext.getColumn().getName());
		return sb.toString();
	}

	@Override
	public SequenceId generateSequeueId(GeneratorContext generatorContext) {
		Long createTime = getTemporaryVariable().getCreateTime(generatorContext);
		if(createTime == null || createTime == 0){
			return sequeueIdGenerator.next();
		}
		return sequeueIdGenerator.next(createTime);
	}

	protected long getMaxId(GeneratorContext generatorContext) {
		Long value = generatorContext.getEntityOperations().getMaxValue(Long.class,
				ProxyUtils.getProxyFactory().getUserClass(generatorContext.getBean().getClass()),
				generatorContext.getColumn().getName());
		return value == null ? 0 : value;
	};

	@Override
	public Number generateNumber(GeneratorContext generatorContext) {
		String key = getCacheKey(generatorContext);
		if (!counter.isExist(key)) {
			// 不存在
			Lock lock = lockFactory.getLock(key + "&lock");
			try {
				lock.lock();
				if (!counter.isExist(key)) {
					long maxId = getMaxId(generatorContext);
					return counter.incr(key, 1, maxId + 1);
				}
			} finally {
				lock.unlock();
			}
		}
		return counter.incr(key, 1);
	}
}
