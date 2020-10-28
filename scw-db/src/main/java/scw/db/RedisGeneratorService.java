package scw.db;

import scw.data.generator.SequenceIdGenerator;
import scw.redis.Redis;
import scw.redis.RedisDataTemplete;
import scw.redis.locks.RedisLockFactory;
import scw.sql.orm.support.generation.DefaultGeneratorService;

public class RedisGeneratorService extends DefaultGeneratorService {

	public RedisGeneratorService(Redis redis) {
		super(new SequenceIdGenerator(redis), new RedisDataTemplete(redis), new RedisLockFactory(redis));
	}
}
