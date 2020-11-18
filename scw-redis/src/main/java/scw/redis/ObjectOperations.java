package scw.redis;

import scw.io.Serializer;
import scw.util.StringCodec;

public final class ObjectOperations extends AbstractRedisOperationsWrapper<String, byte[], Object, byte[]> {
	private final RedisOperations<byte[], byte[]> redisOperations;
	private final StringCodec stringCodec;
	private final Serializer serializer;

	public ObjectOperations(RedisOperations<byte[], byte[]> redisOperations, Serializer serializer,
			StringCodec stringCodec) {
		this.redisOperations = redisOperations;
		this.stringCodec = stringCodec;
		this.serializer = serializer;
	}

	@Override
	protected RedisOperations<byte[], byte[]> getRedisOperations() {
		return redisOperations;
	}

	@Override
	protected byte[] encodeKey(String key) {
		if (key == null) {
			return null;
		}

		return stringCodec.encode(key);
	}

	@Override
	protected String decodeKey(byte[] key) {
		if (key == null) {
			return null;
		}

		return stringCodec.decode(key);
	}

	@Override
	protected byte[] encodeValue(Object value) {
		if (value == null) {
			return null;
		}

		return serializer.serialize(value);
	}

	@Override
	protected Object decodeValue(byte[] value) {
		if (value == null) {
			return null;
		}

		try {
			return serializer.deserialize(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
