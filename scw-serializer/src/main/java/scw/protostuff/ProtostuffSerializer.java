package scw.protostuff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import scw.io.Bits;
import scw.io.Serializer;
import scw.io.SerializerUtils;

public class ProtostuffSerializer extends Serializer {
	private static final ThreadLocal<LinkedBuffer> bufferLocal = new ThreadLocal<LinkedBuffer>() {
		protected LinkedBuffer initialValue() {
			return LinkedBuffer.allocate(1024);
		};
	};

	public static LinkedBuffer getLinkedBuffer() {
		return bufferLocal.get().clear();
	}

	@Override
	public <T> byte[] serialize(Class<T> type, T data) {
		return ProtostuffIOUtil.toByteArray(data, RuntimeSchema.getSchema(type), getLinkedBuffer());
	}

	@Override
	public <T> T deserialize(Class<T> type, byte[] data) {
		Schema<T> schema = RuntimeSchema.getSchema(type);
		T t = schema.newMessage();
		ProtostuffIOUtil.mergeFrom(data, t, schema);
		return t;
	}

	@Override
	public <T> void serialize(OutputStream out, Class<T> type, T data) throws IOException {
		ProtostuffIOUtil.writeTo(out, data, RuntimeSchema.getSchema(type), getLinkedBuffer());
		out.flush();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public byte[] serialize(Object data) {
		if (data == null) {
			return null;
		}

		byte[] nameBytes = SerializerUtils.class2bytes(data.getClass());
		byte[] objBytes = serialize((Class) data.getClass(), data);
		byte[] buff = new byte[4 + nameBytes.length + objBytes.length];
		Bits.putInt(buff, 0, nameBytes.length);
		System.arraycopy(nameBytes, 0, buff, 4, nameBytes.length);
		System.arraycopy(objBytes, 0, buff, 4 + nameBytes.length, objBytes.length);
		return buff;
	}

	@Override
	public void serialize(OutputStream out, Object data) throws IOException {
		byte[] bs = serialize(data);
		if (bs == null) {
			return;
		}
		out.write(bs);
		out.flush();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> T deserialize(byte[] data) {
		int len = Bits.getInt(data, 0);
		byte[] nameBytes = new byte[len];
		System.arraycopy(data, 4, nameBytes, 0, len);
		Class<?> type;
		try {
			type = SerializerUtils.bytes2class(nameBytes);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		Schema schema = RuntimeSchema.getSchema(type);
		Object t = schema.newMessage();
		ProtostuffIOUtil.mergeFrom(data, 4 + len, data.length - 4 - len, t, schema);
		return (T) t;
	}

	@Override
	public <T> T deserialize(Class<T> type, InputStream input) throws IOException {
		Schema<T> schema = RuntimeSchema.getSchema(type);
		T t = schema.newMessage();
		ProtostuffIOUtil.mergeFrom(input, t, schema, getLinkedBuffer());
		return t;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> T deserialize(InputStream input) throws IOException {
		byte[] lenBytes = new byte[2];
		if (input.read(lenBytes) == -1) {
			throw new RuntimeException("解析错误");
		}

		byte[] nameBytes = new byte[Bits.getInt(lenBytes, 0)];
		input.read(nameBytes);

		Class<?> type;
		try {
			type = SerializerUtils.bytes2class(nameBytes);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		Schema schema = RuntimeSchema.getSchema(type);
		Object t = schema.newMessage();
		ProtostuffIOUtil.mergeFrom(input, t, schema, getLinkedBuffer());
		return (T) t;
	}
}
