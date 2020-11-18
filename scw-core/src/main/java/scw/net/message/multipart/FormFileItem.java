package scw.net.message.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import scw.compatible.CompatibleUtils;
import scw.http.ContentDisposition;
import scw.http.MediaType;
import scw.io.UnsafeByteArrayInputStream;
import scw.json.JSONSupport;
import scw.net.message.converter.JsonMessageConverter;
import scw.value.ValueUtils;

public class FormFileItem extends AbstractFileItem {
	private String body;
	private Charset charset;

	public FormFileItem(String name, Object body, Charset charset, JSONSupport jsonSupport) {
		super(name);
		this.charset = charset;
		getHeaders().setContentDisposition(ContentDisposition.builder("form-data").name(name).build());
		if (ValueUtils.isBaseType(body.getClass())) {
			this.body = body.toString();
			getHeaders().setContentType(new MediaType(MediaType.TEXT_HTML, charset));
		} else {
			this.body = JsonMessageConverter.toJsonString(body, jsonSupport);
			getHeaders().setContentType(new MediaType(MediaType.APPLICATION_JSON, charset));
		}
		getHeaders().setContentLength(getBytes().length);
	}

	public byte[] getBytes() {
		return CompatibleUtils.getStringOperations().getBytes(this.body, charset);
	}

	public InputStream getBody() {
		return new UnsafeByteArrayInputStream(getBytes());
	}

	public void close() {
		// ignore
	}

	@Override
	public String getTextBody() throws IOException {
		return body;
	}

	public long getSize() {
		return getBytes().length;
	}
}
