package scw.net.message.multipart;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import scw.http.ContentDisposition;
import scw.http.MediaType;
import scw.io.Resource;
import scw.net.FileMimeTypeUitls;
import scw.net.MimeType;

public class ResourceFileItem extends AbstractFileItem {
	private Resource resource;

	public ResourceFileItem(String fieldName, Resource resource) {
		super(fieldName);
		this.resource = resource;
		ContentDisposition contentDisposition = ContentDisposition.builder("form-data").name(fieldName)
				.filename(getName()).build();
		getHeaders().setContentDisposition(contentDisposition);
		MimeType mimeType = FileMimeTypeUitls.getMimeType(resource);
		if (mimeType != null) {
			getHeaders().setContentType(new MediaType(mimeType));
		}
	}

	public Resource getResource() {
		return resource;
	}

	public InputStream getBody() throws IOException {
		return resource.getInputStream();
	}

	public void close() {
		// ignore
	}

	@Override
	public String getName() {
		return resource.getFilename();
	}

	public long getSize() {
		try {
			return resource.getFile().length();
		} catch (FileNotFoundException e) {
			return -1;
		} catch (IOException e) {
			return -1;
		}
	}
}
