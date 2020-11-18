package scw.redis;

import scw.data.DataTemplete;
import scw.data.cas.CASOperations;

public interface Redis extends DataTemplete{
	RedisOperations<String, String> getStringOperations();

	RedisOperations<byte[], byte[]> getBinaryOperations();

	RedisOperations<String, Object> getObjectOperations();

	CASOperations getCASOperations();
}
