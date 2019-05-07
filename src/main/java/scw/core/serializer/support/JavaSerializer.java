package scw.core.serializer.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import scw.core.io.StreamUtils;
import scw.core.io.UnsafeByteArrayInputStream;
import scw.core.io.UnsafeByteArrayOutputStream;
import scw.core.serializer.Serializer;
import scw.core.utils.XUtils;

public class JavaSerializer extends Serializer {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(InputStream input) throws IOException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(input);
			return (T) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(byte[] data) {
		UnsafeByteArrayInputStream bis = new UnsafeByteArrayInputStream(data);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bis);
			return (T) ois.readObject();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			XUtils.close(ois, bis);
		}
	}

	public void serialize(OutputStream out, Object data) throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(out);
			oos.writeObject(data);
			oos.flush();
		} finally {
			if (oos != null) {
				oos.close();
			}
		}
	}

	public byte[] serialize(Object data) {
		UnsafeByteArrayOutputStream bos = StreamUtils.getUnsafeByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(data);
			oos.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			XUtils.close(oos, bos);
		}
	}
}