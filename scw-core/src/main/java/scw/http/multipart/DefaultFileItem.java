package scw.http.multipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import scw.http.ContentDisposition;
import scw.http.MediaType;
import scw.io.support.TemporaryFile;
import scw.net.FileMimeTypeUitls;
import scw.net.MimeType;

class DefaultFileItem extends FileItem {
	private final File file;

	public DefaultFileItem(String fieldName, File file) {
		this(fieldName, file, false);
	}

	public DefaultFileItem(String fieldName, File file, boolean temporaryFile) {
		super(fieldName);
		this.file = temporaryFile ? TemporaryFile.wrapper(file) : file;
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

	public void close() throws IOException {
		if (file instanceof TemporaryFile) {
			file.delete();
		}
	}
}
