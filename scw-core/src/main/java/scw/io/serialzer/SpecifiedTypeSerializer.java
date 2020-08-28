package scw.io.serialzer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import scw.beans.annotation.AopEnable;

@AopEnable(false)
public interface SpecifiedTypeSerializer {
	<T> void serialize(OutputStream out, Class<T> type, T data) throws IOException;

	<T> byte[] serialize(Class<T> type, T data) throws IOException;

	<T> T deserialize(Class<T> type, InputStream input) throws IOException, ClassNotFoundException;

	<T> T deserialize(Class<T> type, byte[] data) throws IOException, ClassNotFoundException;
}