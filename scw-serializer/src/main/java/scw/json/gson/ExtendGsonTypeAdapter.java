package scw.json.gson;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import scw.aop.ProxyUtils;
import scw.json.JSONAware;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExtendGsonTypeAdapter extends TypeAdapter<Object> {
	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {

		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
			if (JSONAware.class.isAssignableFrom(typeToken.getRawType())
					|| ProxyUtils.getProxyFactory().isProxy(typeToken.getRawType())) {
				return (TypeAdapter) new ExtendGsonTypeAdapter(gson);
			}
			return null;
		}
	};

	private final Gson context;

	private ExtendGsonTypeAdapter(Gson context) {
		this.context = context;
	}

	@Override
	public void write(JsonWriter out, Object value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}

		if (value instanceof JSONAware) {
			out.jsonValue(((JSONAware) value).toJSONString());
			return;
		}

		Class clazz = ProxyUtils.getProxyFactory().getUserClass(value.getClass());
		TypeAdapter<Object> typeAdapter = context.getAdapter(clazz);
		if (typeAdapter == null) {
			throw new UnsupportedOperationException(
					"Attempted to serialize java.lang.Class: " + clazz + ". Forgot to register a type adapter?");
		}
		typeAdapter.write(out, value);
	}

	@Override
	public Object read(JsonReader in) throws IOException {
		throw new UnsupportedOperationException("read");
	}

}
