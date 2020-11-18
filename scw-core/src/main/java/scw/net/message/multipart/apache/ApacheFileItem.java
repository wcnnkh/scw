package scw.net.message.multipart.apache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.fileupload.FileItemHeaders;

import scw.http.HttpHeaders;
import scw.http.MediaType;
import scw.net.message.multipart.FileItem;

public class ApacheFileItem implements FileItem {
	private final org.apache.commons.fileupload.FileItem fileItem;

	public ApacheFileItem(org.apache.commons.fileupload.FileItem fileItem) {
		this.fileItem = fileItem;
		FileItemHeaders fileItemHeaders = fileItem.getHeaders();
		Iterator<String> iterator = fileItemHeaders.getHeaderNames();
		while (iterator.hasNext()) {
			String name = iterator.next();
			Iterator<String> valueIterator = fileItemHeaders.getHeaders(name);
			while (valueIterator.hasNext()) {
				getHeaders().add(name, valueIterator.next());
			}
		}
		getHeaders().readyOnly();
	}

	private HttpHeaders httpHeaders;

	public HttpHeaders getHeaders() {
		if (httpHeaders == null) {
			httpHeaders = new HttpHeaders();
			FileItemHeaders fileItemHeaders = fileItem.getHeaders();
			Iterator<String> iterator = fileItemHeaders.getHeaderNames();
			while (iterator.hasNext()) {
				String name = iterator.next();
				Iterator<String> valueIterator = fileItemHeaders.getHeaders(name);
				while (valueIterator.hasNext()) {
					httpHeaders.add(name, valueIterator.next());
				}
			}
			getHeaders().readyOnly();
		}
		return httpHeaders;
	}

	public org.apache.commons.fileupload.FileItem getFileItem() {
		return fileItem;
	}

	public String getName() {
		return fileItem.getName();
	}

	public String getFieldName() {
		return fileItem.getFieldName();
	}

	public long getContentLength() {
		return fileItem.getSize();
	}

	public String getTextBody() {
		return fileItem.getString();
	}

	public byte[] getBytes() {
		return fileItem.get();
	}

	public InputStream getBody() throws IOException {
		return fileItem.getInputStream();
	}

	public boolean isFormField() {
		return fileItem.isFormField();
	}

	public void close() {
		fileItem.delete();
	}

	public long getSize() {
		return fileItem.getSize();
	}

	@Override
	public String toString() {
		return fileItem.toString();
	}

	public MediaType getContentType() {
		return getHeaders().getContentType();
	}
}
