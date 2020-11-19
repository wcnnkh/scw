package scw.sql.orm.support.generation;

import scw.core.instance.annotation.Configuration;
import scw.data.generator.SequenceIdGenerator;
import scw.memcached.Memcached;
import scw.memcached.locks.MemcachedLockFactory;

@Configuration(order=Integer.MIN_VALUE)
public class MemcachedGeneratorService extends DefaultGeneratorService {

	public MemcachedGeneratorService(Memcached memcached) {
		super(new SequenceIdGenerator(memcached), memcached, new MemcachedLockFactory(memcached));
	}
}
