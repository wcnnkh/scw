package scw.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import scw.aop.annotation.AopEnable;

/**
 * 指定对象类型的序列化
 * @author shuchaowen
 *
 */
@AopEnable(false)
public interface SpecifiedTypeSerializer {
	<T> void serialize(OutputStream out, Class<T> type, T data) throws IOException;

	<T> byte[] serialize(Class<T> type, T data);

	<T> T deserialize(Class<T> type, InputStream input) throws IOException;

	<T> T deserialize(Class<T> type, byte[] data);
}
