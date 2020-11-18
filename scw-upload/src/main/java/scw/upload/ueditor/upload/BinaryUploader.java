package scw.upload.ueditor.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import scw.http.server.MultiPartServerHttpRequest;
import scw.net.message.multipart.FileItem;
import scw.upload.ueditor.PathFormat;
import scw.upload.ueditor.define.AppInfo;
import scw.upload.ueditor.define.BaseState;
import scw.upload.ueditor.define.FileType;
import scw.upload.ueditor.define.State;

public class BinaryUploader {

	public static final State save(MultiPartServerHttpRequest request,
			Map<String, Object> conf) {
		FileItem fileItem = request.getFirstFile();
		if (fileItem == null) {
			return new BaseState(false, AppInfo.NOTFOUND_UPLOAD_DATA);
		}
		
		try {
			String savePath = (String) conf.get("savePath");
			String originFileName = fileItem.getName();
			String suffix = FileType.getSuffixByFilename(originFileName);

			originFileName = originFileName.substring(0,
					originFileName.length() - suffix.length());
			savePath = savePath + suffix;

			long maxSize = ((Long) conf.get("maxSize")).longValue();

			if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
				return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
			}

			savePath = PathFormat.parse(savePath, originFileName);

			String physicalPath = (String) conf.get("rootPath") + savePath;

			InputStream is = fileItem.getBody();
			State storageState = StorageManager.saveFileByInputStream(is,
					physicalPath, maxSize);
			is.close();

			if (storageState.isSuccess()) {
				storageState.putInfo("url", PathFormat.format(savePath));
				storageState.putInfo("type", suffix);
				storageState.putInfo("original", originFileName + suffix);
			}

			return storageState;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BaseState(false, AppInfo.IO_ERROR);
	}

	private static boolean validType(String type, String[] allowTypes) {
		List<String> list = Arrays.asList(allowTypes);

		return list.contains(type);
	}
}
