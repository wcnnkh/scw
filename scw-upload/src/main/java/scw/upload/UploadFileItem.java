package scw.upload;

import java.io.IOException;
import java.io.InputStream;

import scw.http.HttpHeaders;
import scw.net.message.multipart.FileItem;

public class UploadFileItem implements UploadItem {
	private final FileItem fileItem;

	public UploadFileItem(FileItem fileItem) {
		this.fileItem = fileItem;
	}

	public String getName() {
		return fileItem.getName();
	}

	public HttpHeaders getHeaders() {
		return fileItem.getHeaders();
	}

	public InputStream getBody() throws IOException {
		return fileItem.getBody();
	}

	public long size() {
		return fileItem.getSize();
	}

}
