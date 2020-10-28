package scw.db;

import scw.data.generator.SequenceIdGenerator;
import scw.memcached.Memcached;
import scw.memcached.locks.MemcachedLockFactory;
import scw.sql.orm.support.generation.DefaultGeneratorService;

public class MemcachedGeneratorService extends DefaultGeneratorService {

	public MemcachedGeneratorService(Memcached memcached) {
		super(new SequenceIdGenerator(memcached), memcached, new MemcachedLockFactory(memcached));
	}
}
