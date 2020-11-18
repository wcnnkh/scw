package scw.net.message.converter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import scw.core.Constants;
import scw.core.ResolvableType;
import scw.http.HttpHeaders;
import scw.io.IOUtils;
import scw.json.JSONSupport;
import scw.json.JSONUtils;
import scw.net.MimeType;
import scw.net.MimeTypeUtils;
import scw.net.MimeTypes;
import scw.net.message.Headers;
import scw.net.message.InputMessage;
import scw.net.message.Message;
import scw.net.message.OutputMessage;

public abstract class AbstractMessageConverter<T> implements MessageConverter {
	public static final MimeType TEXT_ALL = new MimeType("text", "*");
	private Charset defaultCharset = Constants.DEFAULT_CHARSET;
	private JSONSupport jsonSupport = JSONUtils.getJsonSupport();
	protected final MimeTypes supportMimeTypes = new MimeTypes();
	private boolean supportBytes = false;

	public boolean isSupportBytes() {
		return supportBytes;
	}

	public void setSupportBytes(boolean supportBytes) {
		this.supportBytes = supportBytes;
	}

	public final MimeTypes getSupportMimeTypes() {
		return supportMimeTypes.readyOnly();
	}

	public JSONSupport getJsonSupport() {
		return jsonSupport;
	}

	public void setJsonSupport(JSONSupport jsonSupport) {
		this.jsonSupport = jsonSupport;
	}

	public Charset getDefaultCharset() {
		return defaultCharset;
	}

	public void setDefaultCharset(Charset defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	public boolean canRead(MimeType contentType) {
		if (contentType == null) {
			return true;
		}

		for (MimeType mimeType : getSupportMimeTypes()) {
			if (mimeType.includes(contentType)) {
				return true;
			}
		}
		return false;
	}

	public boolean canWrite(MimeType contentType) {
		if (contentType == null
				|| MimeTypeUtils.ALL.equalsTypeAndSubtype(contentType)) {
			return true;
		}

		for (MimeType mimeType : getSupportMimeTypes()) {
			if (mimeType.isCompatibleWith(contentType)) {
				return true;
			}
		}
		return false;
	}

	public abstract boolean support(Class<?> clazz);
	
	public boolean canWrite(Class<?> clazz){
		return support(clazz);
	}
	
	public boolean canRead(Type type){
		if(type instanceof Class){
			return support((Class<?>)type);
		}
		
		ResolvableType resolvableType = ResolvableType.forType(type);
		return support(resolvableType.getRawClass());
	}

	public boolean canRead(Type type, MimeType contentType) {
		if(type == byte[].class && !isSupportBytes()){
			return false;
		}
		
		return canRead(type) && canRead(contentType);
	}

	public boolean canWrite(Object body, MimeType contentType) {
		if (body == null) {
			return false;
		}
		
		if(body.getClass() == byte[].class && !isSupportBytes()){
			return false;
		}

		return canWrite(body.getClass()) && canWrite(contentType);
	}

	public Object read(Type type, InputMessage inputMessage)
			throws IOException, MessageConvertException {
		return readInternal(type, inputMessage);
	}

	@SuppressWarnings("unchecked")
	public void write(Object body, MimeType contentType,
			OutputMessage outputMessage) throws IOException,
			MessageConvertException {
		T t = (T) body;
		MimeType contentTypeToUse = contentType;
		if(contentType == null){
			contentTypeToUse = outputMessage.getContentType();
		}else if (outputMessage.getContentType() == null) {
			if (contentTypeToUse == null || contentTypeToUse.isWildcardType()
					|| contentTypeToUse.isWildcardSubtype()) {
				contentTypeToUse = getDefaultContentType(t);
			}

			if (contentTypeToUse != null && !contentTypeToUse.isWildcardType()
					&& !contentTypeToUse.isWildcardSubtype()) {
				if (contentTypeToUse.getCharset() == null) {
					Charset defaultCharset = getDefaultCharset();
					if (defaultCharset != null) {
						contentTypeToUse = new MimeType(contentTypeToUse,
								defaultCharset);
					}
				}
				outputMessage.setContentType(contentTypeToUse);
			}
		}

		if (outputMessage.getContentLength() < 0) {
			Headers headers = outputMessage.getHeaders();
			if (headers != null
					&& headers.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
				Long contentLength = getContentLength(t,
						outputMessage.getContentType());
				if (contentLength != null && contentLength >= 0) {
					outputMessage.setContentLength(contentLength);
				}
			}
		}
		writeInternal(t, contentTypeToUse, outputMessage);
	}

	protected MimeType getDefaultContentType(T body) throws IOException {
		MimeType mimeType = getSupportMimeTypes().getMimeTypes().first();
		if(mimeType.isWildcardType() || mimeType.isWildcardSubtype()){
			return null;
		}
		return mimeType;
	}

	protected Long getContentLength(T body, MimeType contentType)
			throws IOException {
		return null;
	}

	protected Charset getCharset(Message message) {
		MimeType mimeType = message.getContentType();
		if (mimeType == null) {
			return getDefaultCharset();
		}

		Charset charset = mimeType.getCharset();
		if (charset == null) {
			return getDefaultCharset();
		}
		return charset;
	}

	protected String readTextBody(InputMessage inputMessage) throws IOException {
		return IOUtils.toString(inputMessage.getBody(),
				getCharset(inputMessage).name());
	}

	protected void writeTextBody(String text, MimeType contentType,
			OutputMessage outputMessage) throws IOException {
		if (text == null) {
			return;
		}

		IOUtils.write(text, outputMessage.getBody(), getCharset(outputMessage)
				.name());
	}

	protected abstract T readInternal(Type type, InputMessage inputMessage)
			throws IOException, MessageConvertException;

	protected abstract void writeInternal(T body, MimeType contentType,
			OutputMessage outputMessage) throws IOException,
			MessageConvertException;
}
