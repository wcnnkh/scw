package scw.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class JavaSerializer extends Serializer {
	public final static JavaSerializer INSTANCE = new JavaSerializer();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(InputStream input) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(input);
			return (T) ois.readObject();
		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(byte[] data) throws ClassNotFoundException {
		UnsafeByteArrayInputStream bis = new UnsafeByteArrayInputStream(data);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bis);
			return (T) ois.readObject();
		} catch (IOException e) {
			// 不可能存在此错误
			throw new SerializerException(e);
		} finally {
			IOUtils.close(ois, bis);
		}
	}

	public void serialize(OutputStream out, Object data) throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(out);
			oos.writeObject(data);
			oos.flush();
		} finally {
			IOUtils.close(oos);
		}
	}

	public byte[] serialize(Object data) {
		UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(data);
			oos.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			// 不可能存在此错误
			throw new SerializerException(e);
		} finally {
			IOUtils.close(oos, bos);
		}
	}
}
