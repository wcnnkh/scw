package scw.feign;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import feign.FeignException;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import scw.io.FastByteArrayOutputStream;
import scw.net.MimeType;
import scw.net.message.converter.MessageConverter;

public class FeignCodec implements Encoder, Decoder {
	private MessageConverter messageConverter;

	public FeignCodec(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
		FeignInputMessage inputMessage = new FeignInputMessage(response);
		if (messageConverter.canRead(type, inputMessage.getContentType())) {
			return messageConverter.read(type, inputMessage);
		}
		throw new DecodeException(response.status(), "not support decode type:" + type, response.request());
	}

	public void encode(Object body, Type bodyType, RequestTemplate template) throws EncodeException {
		FastByteArrayOutputStream byteBody = new FastByteArrayOutputStream();
		FeignOutputMessage outputMessage = new FeignOutputMessage(template, byteBody);
		if (messageConverter.canWrite(body, outputMessage.getContentType())) {
			try {
				messageConverter.write(body, outputMessage.getContentType(), outputMessage);

				MimeType mimeType = outputMessage.getContentType();
				Charset charset = mimeType.getCharset();
				if (charset == null) {
					charset = template.requestCharset();
				}

				template.body(byteBody.toByteArray(), charset);
				Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>();
				for (Entry<String, List<String>> entry : outputMessage.getHeaders().entrySet()) {
					headers.put(entry.getKey(), entry.getValue());
				}
				template.headers(headers);
			} catch (IOException e) {
				throw new EncodeException("bodyType=" + bodyType, e);
			}
		}
		throw new EncodeException("not support type:" + bodyType + ", body=" + body);
	}

}
