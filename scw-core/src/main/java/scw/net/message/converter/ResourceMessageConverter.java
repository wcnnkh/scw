package scw.net.message.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import scw.http.MediaType;
import scw.io.FileSystemResource;
import scw.io.IOUtils;
import scw.io.Resource;
import scw.lang.NotSupportedException;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.net.FileMimeTypeUitls;
import scw.net.MimeType;
import scw.net.message.InputMessage;
import scw.net.message.OutputMessage;

public class ResourceMessageConverter extends AbstractMessageConverter<Resource> {
	private static Logger logger = LoggerUtils.getLogger(ResourceMessageConverter.class);

	public ResourceMessageConverter() {
		supportMimeTypes.add(MediaType.ALL);
	}

	@Override
	public boolean support(Class<?> clazz) {
		return Resource.class.isAssignableFrom(clazz) && File.class.isAssignableFrom(clazz);
	}
	
	@Override
	public boolean canWrite(MimeType contentType) {
		if(MediaType.MULTIPART_FORM_DATA.equalsTypeAndSubtype(contentType)){
			return false;
		}
		return super.canWrite(contentType);
	}

	@Override
	public boolean canRead(Type type, MimeType contentType) {
		return false;
	}

	@Override
	protected Resource readInternal(Type type, InputMessage inputMessage) throws IOException, MessageConvertException {
		throw new NotSupportedException(type.toString());
	}

	private Resource getResource(Object body) {
		if (body == null) {
			return null;
		}

		if (body instanceof Resource) {
			return (Resource) body;
		} else if (body instanceof File) {
			File file = (File) body;
			return new FileSystemResource(file);
		}

		throw new NotSupportedException(body.toString());
	}

	@Override
	public void write(Object body, MimeType contentType, OutputMessage outputMessage)
			throws IOException, MessageConvertException {
		Resource resource = getResource(body);
		MimeType mimeType = contentType == null ? FileMimeTypeUitls.getMimeType(resource) : contentType;
		super.write(resource, mimeType, outputMessage);
	}

	@Override
	protected void writeInternal(Resource body, MimeType contentType, OutputMessage outputMessage)
			throws IOException, MessageConvertException {
		Resource resource = (Resource) body;
		if (!resource.exists()) {
			logger.error("Not found {}", resource.getDescription());
			return;
		}

		InputStream is = resource.getInputStream();
		try {
			IOUtils.write(is, outputMessage.getBody());
		} finally {
			is.close();
		}
	}
}
