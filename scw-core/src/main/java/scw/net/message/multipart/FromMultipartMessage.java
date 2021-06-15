package scw.net.message.multipart;

import java.io.IOException;
import java.io.InputStream;

import scw.http.ContentDisposition;
import scw.http.MediaType;
import scw.io.UnsafeByteArrayInputStream;

public class FromMultipartMessage extends AbstractMultipartMessage {
	private final byte[] body;

	public FromMultipartMessage(String name, byte[] body) {
		super(name);
		this.body = body;
		getHeaders().setContentDisposition(ContentDisposition.builder("form-data").name(name).build());
		getHeaders().setContentType(new MediaType(MediaType.TEXT_HTML));
		getHeaders().setContentLength(body.length);
	}

	@Override
	public String getOriginalFilename() {
		return null;
	}

	@Override
	public InputStream getBody() throws IOException {
		return new UnsafeByteArrayInputStream(body);
	}

}
