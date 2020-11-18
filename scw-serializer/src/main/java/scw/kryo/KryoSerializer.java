package scw.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import scw.io.Serializer;

public class KryoSerializer extends Serializer {
	private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
		protected Kryo initialValue() {
			Kryo kryo = new Kryo();
			return kryo;
		};
	};

	public static Kryo getKryo() {
		return kryoLocal.get();
	}

	private static final ThreadLocal<Output> outputLocal = new ThreadLocal<Output>() {
		protected Output initialValue() {
			Output output = new Output();
			output.setBuffer(new byte[1024], -1);
			return output;
		};
	};

	private static final ThreadLocal<Input> inputLocal = new ThreadLocal<Input>() {
		protected Input initialValue() {
			return new Input();
		};
	};

	public static Output getOutput() {
		Output output = outputLocal.get();
		output.clear();
		return output;
	}

	public static Output getOutput(OutputStream outputStream) {
		Output output = outputLocal.get();
		output.setOutputStream(outputStream);
		return output;
	}

	public static Input getInput(byte[] data) {
		Input input = inputLocal.get();
		input.setBuffer(data);
		return input;
	}

	public static Input getInput(InputStream inputStream) {
		Input input = inputLocal.get();
		input.setInputStream(inputStream);
		return input;
	}

	public void serialize(OutputStream out, Object data) {
		Output output = getOutput(out);
		getKryo().writeClassAndObject(output, data);
		output.flush();
	}

	public byte[] serialize(Object data) {
		Output output = getOutput();
		getKryo().writeClassAndObject(output, data);
		return output.toBytes();
	}

	@SuppressWarnings("unchecked")
	public <T> T deserialize(InputStream input) throws IOException {
		return (T) getKryo().readClassAndObject(getInput(input));
	}

	@SuppressWarnings("unchecked")
	public <T> T deserialize(byte[] data) {
		return (T) getKryo().readClassAndObject(getInput(data));
	}

	@Override
	public <T> byte[] serialize(Class<T> type, T data) {
		Output output = getOutput();
		getKryo().writeObjectOrNull(output, data, type);
		output.flush();
		return output.toBytes();
	}

	@Override
	public <T> void serialize(OutputStream out, Class<T> type, T data) {
		Output output = getOutput(out);
		getKryo().writeObjectOrNull(output, data, type);
		output.flush();
	}

	@Override
	public <T> T deserialize(Class<T> type, byte[] data) {
		return getKryo().readObjectOrNull(getInput(data), type);
	}

	@Override
	public <T> T deserialize(Class<T> type, InputStream input) {
		return getKryo().readObjectOrNull(getInput(input), type);
	}
}
