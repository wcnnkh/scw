package scw.hessian;

import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;

@SuppressWarnings("rawtypes")
public class HessianAddSerializerFactory extends AbstractSerializerFactory {
	private final Serializer serialzer;
	private final Deserializer deserialzer;
	private final Class type;

	public HessianAddSerializerFactory(Class type, Serializer serializer, Deserializer deserializer) {
		this.type = type;
		this.serialzer = serializer;
		this.deserialzer = deserializer;
	}

	@Override
	public Serializer getSerializer(Class cl) throws HessianProtocolException {
		if (cl == type) {
			return serialzer;
		}
		return null;
	}

	@Override
	public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
		if (cl == type) {
			return deserialzer;
		}

		return null;
	}

}
