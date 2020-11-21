package scw.json;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

import scw.core.utils.ClassUtils;
import scw.io.IOUtils;
import scw.value.ValueUtils;

public abstract class AbstractJSONSupport implements JSONSupport {

	@SuppressWarnings("unchecked")
	public final <T> T parseObject(String text, Class<T> type) {
		if (type == JsonObject.class) {
			return (T) parseObject(text);
		} else if (type == JsonArray.class) {
			return (T) parseArray(text);
		} else if(type == JsonElement.class){
			return (T) parseJson(text);
		}else if(type == String.class){
			return (T) text;
		} else if(ClassUtils.isPrimitiveOrWrapper(type)){
			return ValueUtils.parse(text, type);
		}
		return parseObjectInternal(text, (Class<T>) type);
	}

	protected abstract <T> T parseObjectInternal(String text, Class<T> type);

	@SuppressWarnings("unchecked")
	public final <T> T parseObject(String text, Type type) {
		if (type instanceof Class) {
			return parseObject(text, (Class<T>) type);
		}
		return (T) parseObjectInternal(text, type);
	}

	protected abstract Object parseObjectInternal(String text, Type type);

	public JsonArray parseArray(Reader reader) throws IOException {
		return parseArray(new String(IOUtils.toCharArray(reader)));
	}

	public JsonObject parseObject(Reader reader) throws IOException {
		return parseObject(new String(IOUtils.toCharArray(reader)));
	}

	@SuppressWarnings("unchecked")
	public final <T> T parseObject(Reader reader, Class<T> type) throws IOException {
		if (type == JsonObject.class) {
			return (T) parseObject(reader);
		} else if (type == JsonArray.class) {
			return (T) parseArray(reader);
		}
		return parseObjectInternal(reader, type);
	}

	protected <T> T parseObjectInternal(Reader reader, Class<T> type) throws IOException {
		return parseObject(new String(IOUtils.toCharArray(reader)), type);
	}

	@SuppressWarnings("unchecked")
	public final <T> T parseObject(Reader reader, Type type) throws IOException {
		if (type instanceof Class) {
			return parseObject(reader, (Class<T>) type);
		}

		if (type == JsonObject.class) {
			return (T) parseObject(reader);
		} else if (type == JsonArray.class) {
			return (T) parseArray(reader);
		}
		return (T) parseObjectInternal(reader, type);
	}

	protected Object parseObjectInternal(Reader reader, Type type) throws IOException {
		return parseObject(new String(IOUtils.toCharArray(reader)), type);
	}
	
	public final String toJSONString(Object obj) {
		if(obj == null){
			return null;
		}
		
		if(obj instanceof String){
			return (String)obj;
		}
		
		if(ClassUtils.isPrimitiveOrWrapper(obj.getClass())){
			return String.valueOf(obj);
		}
		
		if(obj instanceof JSONAware){
			return ((JSONAware) obj).toJSONString();
		}
		
		return toJsonStringInternal(obj);
	}
	
	protected abstract String toJsonStringInternal(Object obj);
}
