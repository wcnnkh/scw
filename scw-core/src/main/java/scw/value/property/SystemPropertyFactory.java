package scw.value.property;

import java.util.Iterator;

import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.util.EnumerationConvert;
import scw.util.MultiIterator;

public final class SystemPropertyFactory extends ExtendPropertyFactory {
	public static final String PROPERTY_MAVEN_HOME = "maven.home";
	public static final String PROPERTY_PATH_SEPARATOR = "path.separator";
	public static final String PROPERTY_JAVA_CLASS_PATH = "java.class.path";
	public static final String PROPERTY_JAVA_IO_TMPDIR = "java.io.tmpdir";
	public static final String PROPERTY_USER_DIR = "user.dir";
	public static final String PROPERTY_USER_HOME = "user.home";
	public static final String PROPERTY_OS_NAME = "os.name";

	private static SystemPropertyFactory instance = new SystemPropertyFactory();

	public static SystemPropertyFactory getInstance() {
		return instance;
	}

	private SystemPropertyFactory() {
		super(true, true);
	};

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<String> iterator() {
		return new MultiIterator<String>(
				CollectionUtils
						.toIterator(EnumerationConvert.convertToStringEnumeration(System.getProperties().keys())),
				System.getenv().keySet().iterator(), super.iterator());
	}

	@Override
	protected Object getExtendValue(String key) {
		String v = System.getProperty(key);
		if (v == null) {
			v = System.getenv(key);
		}
		return v;
	}

	/**
	 * 获取环境变量分割符
	 * 
	 * @return
	 */
	public String getPathSeparator() {
		return getString(PROPERTY_PATH_SEPARATOR);
	}

	public String getJavaClassPath() {
		return getString(PROPERTY_JAVA_CLASS_PATH);
	}

	public String[] getJavaClassPathArray() {
		String classPath = getJavaClassPath();
		if (StringUtils.isEmpty(classPath)) {
			return null;
		}

		return StringUtils.split(classPath, getPathSeparator());
	}

	public String getMavenHome() {
		return getString(PROPERTY_MAVEN_HOME);
	}

	public String getTempDirectoryPath() {
		return getString(PROPERTY_JAVA_IO_TMPDIR);
	}

	public String getUserDir() {
		return getString(PROPERTY_USER_DIR);
	}

	public String getUserHome() {
		return getString(PROPERTY_USER_HOME);
	}

	public String getOSName() {
		return getString(PROPERTY_OS_NAME);
	}

	public boolean isWin() {
		return getOSName().toLowerCase().startsWith("win");
	}

	public boolean isMac() {
		return getOSName().toLowerCase().startsWith("mac");
	}
}
