package scw.http.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import scw.io.FileUtils;
import scw.io.support.TemporaryFile;
import scw.util.XUtils;

public class FileCachingServerHttpRequest extends ServerHttpRequestWrapper {

	public FileCachingServerHttpRequest(ServerHttpRequest targetRequest) {
		super(targetRequest, true);
	}

	private TemporaryFile temporaryFile;

	public TemporaryFile getTemporaryFile() throws IOException {
		if (temporaryFile == null) {
			temporaryFile = TemporaryFile.createInTempDirectory(XUtils
					.getUUID());
			FileUtils.copyInputStreamToFile(super.getBody(), temporaryFile);
		}
		return temporaryFile;
	}

	@Override
	public InputStream getBody() throws IOException {
		return new FileInputStream(getTemporaryFile());
	}
}
