package scw.io;

import java.io.IOException;
import java.lang.reflect.Type;

import scw.net.MimeType;
import scw.net.MimeTypeUtils;
import scw.net.message.InputMessage;
import scw.net.message.OutputMessage;
import scw.net.message.converter.AbstractMessageConverter;
import scw.net.message.converter.MessageConvertException;

public class SerialzerMessageConveter extends AbstractMessageConverter<Object> {
	private Serializer serializer;

	public SerialzerMessageConveter(Serializer serializer) {
		supportMimeTypes.add(MimeTypeUtils.APPLICATION_OCTET_STREAM, MimeTypeUtils.ALL);
		this.serializer = serializer;
	}

	@Override
	public boolean support(Class<?> clazz) {
		return true;
	}

	@Override
	protected Object readInternal(Type type, InputMessage inputMessage)
			throws IOException, MessageConvertException {
		try {
			return serializer.deserialize(inputMessage.getBody());
		} catch (ClassNotFoundException e) {
			throw new MessageConvertException(e);
		}
	}

	@Override
	protected void writeInternal(Object body, MimeType contentType,
			OutputMessage outputMessage) throws IOException,
			MessageConvertException {
		serializer.serialize(outputMessage.getBody(), body);
	}

}
