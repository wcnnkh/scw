package scw.net.message.multipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import scw.http.ContentDisposition;
import scw.http.MediaType;
import scw.net.FileMimeTypeUitls;
import scw.net.MimeType;

public class DefaultFileItem extends AbstractFileItem {
	private final File file;

	public DefaultFileItem(String fieldName, File file) {
		super(fieldName);
		this.file = file;
		ContentDisposition contentDisposition = ContentDisposition.builder("form-data").name(fieldName)
				.filename(file.getName()).build();
		getHeaders().setContentDisposition(contentDisposition);
		MimeType mimeType = FileMimeTypeUitls.getMimeType(file.getName());
		if (mimeType != null) {
			getHeaders().setContentType(new MediaType(mimeType));
		}
		getHeaders().setContentLength(file.length());
	}

	public InputStream getBody() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public String getName() {
		return file.getName();
	}

	public void close() {
		//ignore
	}

	public long getSize() {
		return file.length();
	}
}
