package scw.net.message.multipart;

import java.io.IOException;
import java.util.List;

import scw.net.message.InputMessage;

public interface FileItemParser {
	List<FileItem> parse(InputMessage inputMessage) throws IOException;
}