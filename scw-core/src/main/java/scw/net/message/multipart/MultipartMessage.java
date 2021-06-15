package scw.net.message.multipart;

import scw.io.Resource;
import scw.lang.Nullable;
import scw.net.message.InputMessage;

public interface MultipartMessage extends InputMessage {
	String getName();

	@Nullable
	String getOriginalFilename();

	default boolean isFile() {
		return getOriginalFilename() != null;
	}

	long getSize();

	default Resource getResource() {
		return new MultipartFileResource(this);
	}
}
