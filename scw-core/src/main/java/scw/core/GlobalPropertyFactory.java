package scw.core;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import scw.core.utils.ClassUtils;
import scw.core.utils.StringUtils;
import scw.io.FileUtils;
import scw.util.ClassScanner;
import scw.util.FormatUtils;
import scw.value.property.PropertyFactory;
import scw.value.property.SystemPropertyFactory;

public final class GlobalPropertyFactory extends PropertyFactory {
	private static final String CLASSES_DIRECTORY = "scw.classes.directory";
	private static final String SYSTEM_ID_PROPERTY = "scw.private.system.id";
	private static final String BASE_PACKAGE_NAME = "scw.base.package";
	private static final String WEB_ROOT = "web.root";

	private static GlobalPropertyFactory instance = new GlobalPropertyFactory();

	public static GlobalPropertyFactory getInstance() {
		return instance;
	}

	static {
		instance.loadProperties("global.properties", "UTF-8").registerListener();
		instance.loadProperties(instance.getValue("scw.properties.private", String.class, "/private.properties"),
				"UTF-8").registerListener();
	}

	private GlobalPropertyFactory() {
		super(true, false);
		addFirstBasePropertyFactory(SystemPropertyFactory.getInstance());
		FormatUtils.info(GlobalPropertyFactory.class, "using classes directory:{}", getClassesDirectory());
		FormatUtils.info(GlobalPropertyFactory.class, "using work path:{}", getWorkPath());
	}

	public String getClassesDirectory() {
		String path = getString(CLASSES_DIRECTORY);
		if (path != null) {
			return path;
		}

		URL url = ClassUtils.getDefaultClassLoader().getResource("");
		return url == null ? SystemPropertyFactory.getInstance().getUserDir() : url.getPath();
	}

	/**
	 * 获取系统本地标识，注意，仅对本地有效
	 * 
	 * @return
	 */
	public String getSystemLocalId() {
		String systemOnlyId = getString(SYSTEM_ID_PROPERTY);
		if (StringUtils.isEmpty(systemOnlyId)) {
			try {
				systemOnlyId = scw.util.Base64
						.encode((SystemPropertyFactory.getInstance().getUserDir() + "&" + getWorkPath())
								.getBytes(Constants.DEFAULT_CHARSET_NAME));
				if (systemOnlyId.endsWith("==")) {
					systemOnlyId = systemOnlyId.substring(0, systemOnlyId.length() - 2);
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			put(SYSTEM_ID_PROPERTY, systemOnlyId);
		}
		return systemOnlyId;
	}

	/**
	 * 可能会返回空
	 * 
	 * @return
	 */
	public String getBasePackageName() {
		return getValue(BASE_PACKAGE_NAME, String.class, ClassScanner.ALL);
	}

	public void setBasePackageName(String packageName) {
		if (StringUtils.isEmpty(packageName)) {
			return;
		}

		putIfAbsent(BASE_PACKAGE_NAME, packageName);
	}

	private String getDefaultWorkPath() {
		String path = getClassesDirectory();
		File file = new File(path);
		if(file.isFile()){
			return null;
		}
		
		if (!file.getName().equals("classes")) {
			return file.getPath();
		}

		if (file.getParent() == null) {
			return file.getPath();
		}

		file = file.getParentFile();
		if (file.getName().equals("WEB-INF")) {
			return file.getParent() == null ? path : file.getParent();
		}

		if (file.getParent() != null) {
			File wi = FileUtils.searchDirectory(file.getParentFile(), "WEB-INF");
			if (wi != null) {
				return wi.getParent();
			}
		}
		return path;
	}

	public String getWorkPath() {
		String path = getString(WEB_ROOT);
		if (path == null) {
			path = getDefaultWorkPath();
			if (StringUtils.isEmpty(path)) {
				path = SystemPropertyFactory.getInstance().getUserDir();
			}

			if (path != null) {
				put(WEB_ROOT, path);
			}
		}
		return path;
	}
}
