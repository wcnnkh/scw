package scw.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import scw.core.Assert;
import scw.core.reflect.ReflectionUtils;
import scw.core.utils.ClassUtils;
import scw.core.utils.StringUtils;
import scw.io.support.ResourceOperations;
import scw.lang.NestedRuntimeException;
import scw.lang.Nullable;
import scw.net.InetUtils;
import scw.util.FormatUtils;
import scw.value.property.SystemPropertyFactory;

/**
 * 资源工具
 * 
 * @author scw
 */
public final class ResourceUtils {
	private ResourceUtils() {
	};

	/** Pseudo URL prefix for loading from the class path: "classpath:". */
	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	public static final String URL_PROTOCOL_RSRC = "rsrc";

	public static final String RSRC_URL_PREFIX = URL_PROTOCOL_RSRC + ":";

	/** URL protocol for a file in the file system: "file". */
	public static final String URL_PROTOCOL_FILE = "file";

	public static final String FILE_URL_PREFIX = URL_PROTOCOL_FILE + ":";

	/** URL protocol for an entry from a jar file: "jar". */
	public static final String URL_PROTOCOL_JAR = "jar";

	public static final String JAR_URL_PREFIX = URL_PROTOCOL_JAR + ":";

	/** URL protocol for an entry from a war file: "war". */
	public static final String URL_PROTOCOL_WAR = "war";

	/** URL prefix for loading from a war file on Tomcat: "war:". */
	public static final String WAR_URL_PREFIX = URL_PROTOCOL_WAR + ":";

	/** URL protocol for an entry from a zip file: "zip". */
	public static final String URL_PROTOCOL_ZIP = "zip";

	/** URL protocol for an entry from a WebSphere jar file: "wsjar". */
	public static final String URL_PROTOCOL_WSJAR = "wsjar";

	/** URL protocol for an entry from a JBoss jar file: "vfszip". */
	public static final String URL_PROTOCOL_VFSZIP = "vfszip";

	/** URL protocol for a JBoss file system resource: "vfsfile". */
	public static final String URL_PROTOCOL_VFSFILE = "vfsfile";

	/** URL protocol for a general JBoss VFS resource: "vfs". */
	public static final String URL_PROTOCOL_VFS = "vfs";

	/** File extension for a regular jar file: ".jar". */
	public static final String JAR_FILE_EXTENSION = ".jar";

	/** Separator between JAR URL and file path within the JAR: "!/". */
	public static final String JAR_URL_SEPARATOR = "!/";

	/** Special separator between WAR URL and jar part on Tomcat. */
	public static final String WAR_URL_SEPARATOR = "*/";

	/**
	 * 因为eclipse默认打包为可执行jar会将资源打包在resources目录下，所以会尝试在此目录下查找资源
	 * @see #getResource(Class, String)
	 * @see #getResource(ClassLoader, String)
	 * @see #getResourceAsStream(Class, String)
	 * @see #getResourceAsStream(ClassLoader, String)
	 * @see #getResources(ClassLoader, String)
	 * @see #getClassLoaderResources(String)
	 * @see #getClassLoaderResource(String)
	 * @see #getClassLoaderResourceAsStream(String)
	 */
	private static final String RESOURCE_PREFIX = "resources/";

	private static final ResourceOperations RESOURCE_OPERATIONS = new ResourceOperations(
			SystemPropertyFactory.getInstance().getValue(
					"resource.cache.enable", boolean.class, true));

	public static final ResourceOperations getResourceOperations() {
		return RESOURCE_OPERATIONS;
	}

	/**
	 * Return whether the given resource location is a URL: either a special
	 * "classpath" pseudo URL or a standard URL.
	 * 
	 * @param resourceLocation
	 *            the location String to check
	 * @return whether the location qualifies as a URL
	 * @see #CLASSPATH_URL_PREFIX
	 * @see java.net.URL
	 */
	public static boolean isUrl(@Nullable String resourceLocation) {
		if (resourceLocation == null) {
			return false;
		}
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			return true;
		}

		return InetUtils.isUrl(resourceLocation);
	}

	/**
	 * Resolve the given resource location to a {@code java.net.URL}.
	 * <p>
	 * Does not check whether the URL actually exists; simply returns the URL
	 * that the given location would correspond to.
	 * 
	 * @param resourceLocation
	 *            the resource location to resolve: either a "classpath:" pseudo
	 *            URL, a "file:" URL, or a plain file path
	 * @return a corresponding URL object
	 * @throws FileNotFoundException
	 *             if the resource cannot be resolved to a URL
	 */
	public static URL getURL(String resourceLocation)
			throws FileNotFoundException {
		Assert.notNull(resourceLocation, "Resource location must not be null");
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX
					.length());
			ClassLoader cl = ClassUtils.getDefaultClassLoader();
			URL url = (cl != null ? cl.getResource(path) : ClassLoader
					.getSystemResource(path));
			if (url == null) {
				String description = "class path resource [" + path + "]";
				throw new FileNotFoundException(
						description
								+ " cannot be resolved to URL because it does not exist");
			}
			return url;
		}
		try {
			// try URL
			return new URL(resourceLocation);
		} catch (MalformedURLException ex) {
			// no URL -> treat as file path
			try {
				return new File(resourceLocation).toURI().toURL();
			} catch (MalformedURLException ex2) {
				throw new FileNotFoundException("Resource location ["
						+ resourceLocation
						+ "] is neither a URL not a well-formed file path");
			}
		}
	}

	/**
	 * Resolve the given resource location to a {@code java.io.File}, i.e. to a
	 * file in the file system.
	 * <p>
	 * Does not check whether the file actually exists; simply returns the File
	 * that the given location would correspond to.
	 * 
	 * @param resourceLocation
	 *            the resource location to resolve: either a "classpath:" pseudo
	 *            URL, a "file:" URL, or a plain file path
	 * @return a corresponding File object
	 * @throws FileNotFoundException
	 *             if the resource cannot be resolved to a file in the file
	 *             system
	 */
	public static File getFile(String resourceLocation)
			throws FileNotFoundException {
		Assert.notNull(resourceLocation, "Resource location must not be null");
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX
					.length());
			String description = "class path resource [" + path + "]";
			ClassLoader cl = ClassUtils.getDefaultClassLoader();
			URL url = (cl != null ? cl.getResource(path) : ClassLoader
					.getSystemResource(path));
			if (url == null) {
				throw new FileNotFoundException(
						description
								+ " cannot be resolved to absolute file path because it does not exist");
			}
			return getFile(url, description);
		}
		try {
			// try URL
			return getFile(new URL(resourceLocation));
		} catch (MalformedURLException ex) {
			// no URL -> treat as file path
			return new File(resourceLocation);
		}
	}

	/**
	 * Resolve the given resource URL to a {@code java.io.File}, i.e. to a file
	 * in the file system.
	 * 
	 * @param resourceUrl
	 *            the resource URL to resolve
	 * @return a corresponding File object
	 * @throws FileNotFoundException
	 *             if the URL cannot be resolved to a file in the file system
	 */
	public static File getFile(URL resourceUrl) throws FileNotFoundException {
		return getFile(resourceUrl, "URL");
	}

	/**
	 * Resolve the given resource URL to a {@code java.io.File}, i.e. to a file
	 * in the file system.
	 * 
	 * @param resourceUrl
	 *            the resource URL to resolve
	 * @param description
	 *            a description of the original resource that the URL was
	 *            created for (for example, a class path location)
	 * @return a corresponding File object
	 * @throws FileNotFoundException
	 *             if the URL cannot be resolved to a file in the file system
	 */
	public static File getFile(URL resourceUrl, String description)
			throws FileNotFoundException {
		Assert.notNull(resourceUrl, "Resource URL must not be null");
		if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
			throw new FileNotFoundException(description
					+ " cannot be resolved to absolute file path "
					+ "because it does not reside in the file system: "
					+ resourceUrl);
		}
		try {
			return new File(toURI(resourceUrl).getSchemeSpecificPart());
		} catch (URISyntaxException ex) {
			// Fallback for URLs that are not valid URIs (should hardly ever
			// happen).
			return new File(resourceUrl.getFile());
		}
	}

	/**
	 * Resolve the given resource URI to a {@code java.io.File}, i.e. to a file
	 * in the file system.
	 * 
	 * @param resourceUri
	 *            the resource URI to resolve
	 * @return a corresponding File object
	 * @throws FileNotFoundException
	 *             if the URL cannot be resolved to a file in the file system
	 */
	public static File getFile(URI resourceUri) throws FileNotFoundException {
		return getFile(resourceUri, "URI");
	}

	/**
	 * Resolve the given resource URI to a {@code java.io.File}, i.e. to a file
	 * in the file system.
	 * 
	 * @param resourceUri
	 *            the resource URI to resolve
	 * @param description
	 *            a description of the original resource that the URI was
	 *            created for (for example, a class path location)
	 * @return a corresponding File object
	 * @throws FileNotFoundException
	 *             if the URL cannot be resolved to a file in the file system
	 */
	public static File getFile(URI resourceUri, String description)
			throws FileNotFoundException {
		Assert.notNull(resourceUri, "Resource URI must not be null");
		if (!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
			throw new FileNotFoundException(description
					+ " cannot be resolved to absolute file path "
					+ "because it does not reside in the file system: "
					+ resourceUri);
		}
		return new File(resourceUri.getSchemeSpecificPart());
	}

	/**
	 * Determine whether the given URL points to a resource in the file system,
	 * i.e. has protocol "file", "vfsfile" or "vfs".
	 * 
	 * @param url
	 *            the URL to check
	 * @return whether the URL has been identified as a file system URL
	 */
	public static boolean isFileURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_FILE.equals(protocol)
				|| URL_PROTOCOL_VFSFILE.equals(protocol) || URL_PROTOCOL_VFS
					.equals(protocol));
	}

	/**
	 * Determine whether the given URL points to a resource in a jar file. i.e.
	 * has protocol "jar", "war, ""zip", "vfszip" or "wsjar".
	 * 
	 * @param url
	 *            the URL to check
	 * @return whether the URL has been identified as a JAR URL
	 */
	public static boolean isJarURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_JAR.equals(protocol)
				|| URL_PROTOCOL_WAR.equals(protocol)
				|| URL_PROTOCOL_ZIP.equals(protocol)
				|| URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR
					.equals(protocol));
	}

	/**
	 * Determine whether the given URL points to a jar file itself, that is, has
	 * protocol "file" and ends with the ".jar" extension.
	 * 
	 * @param url
	 *            the URL to check
	 * @return whether the URL has been identified as a JAR file URL
	 */
	public static boolean isJarFileURL(URL url) {
		return (URL_PROTOCOL_FILE.equals(url.getProtocol()) && url.getPath()
				.toLowerCase().endsWith(JAR_FILE_EXTENSION));
	}

	/**
	 * Extract the URL for the actual jar file from the given URL (which may
	 * point to a resource in a jar file or to a jar file itself).
	 * 
	 * @param jarUrl
	 *            the original URL
	 * @return the URL for the actual jar file
	 * @throws MalformedURLException
	 *             if no valid jar file URL could be extracted
	 */
	public static URL extractJarFileURL(URL jarUrl)
			throws MalformedURLException {
		String urlFile = jarUrl.getFile();
		int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
		if (separatorIndex != -1) {
			String jarFile = urlFile.substring(0, separatorIndex);
			try {
				return new URL(jarFile);
			} catch (MalformedURLException ex) {
				// Probably no protocol in original jar URL, like
				// "jar:C:/mypath/myjar.jar".
				// This usually indicates that the jar file resides in the file
				// system.
				if (!jarFile.startsWith("/")) {
					jarFile = "/" + jarFile;
				}
				return new URL(FILE_URL_PREFIX + jarFile);
			}
		} else {
			return jarUrl;
		}
	}

	/**
	 * Extract the URL for the outermost archive from the given jar/war URL
	 * (which may point to a resource in a jar file or to a jar file itself).
	 * <p>
	 * In the case of a jar file nested within a war file, this will return a
	 * URL to the war file since that is the one resolvable in the file system.
	 * 
	 * @param jarUrl
	 *            the original URL
	 * @return the URL for the actual jar file
	 * @throws MalformedURLException
	 *             if no valid jar file URL could be extracted
	 * @see #extractJarFileURL(URL)
	 */
	public static URL extractArchiveURL(URL jarUrl)
			throws MalformedURLException {
		String urlFile = jarUrl.getFile();

		int endIndex = urlFile.indexOf(WAR_URL_SEPARATOR);
		if (endIndex != -1) {
			// Tomcat's
			// "war:file:...mywar.war*/WEB-INF/lib/myjar.jar!/myentry.txt"
			String warFile = urlFile.substring(0, endIndex);
			if (URL_PROTOCOL_WAR.equals(jarUrl.getProtocol())) {
				return new URL(warFile);
			}
			int startIndex = warFile.indexOf(WAR_URL_PREFIX);
			if (startIndex != -1) {
				return new URL(warFile.substring(startIndex
						+ WAR_URL_PREFIX.length()));
			}
		}

		// Regular "jar:file:...myjar.jar!/myentry.txt"
		return extractJarFileURL(jarUrl);
	}

	/**
	 * Create a URI instance for the given URL, replacing spaces with "%20" URI
	 * encoding first.
	 * 
	 * @param url
	 *            the URL to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException
	 *             if the URL wasn't a valid URI
	 * @see java.net.URL#toURI()
	 */
	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}

	/**
	 * Create a URI instance for the given location String, replacing spaces
	 * with "%20" URI encoding first.
	 * 
	 * @param location
	 *            the location String to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException
	 *             if the location wasn't a valid URI
	 */
	public static URI toURI(String location) throws URISyntaxException {
		return new URI(StringUtils.replace(location, " ", "%20"));
	}

	/**
	 * Set the {@link URLConnection#setUseCaches "useCaches"} flag on the given
	 * connection, preferring {@code false} but leaving the flag at {@code true}
	 * for JNLP based resources.
	 * 
	 * @param con
	 *            the URLConnection to set the flag on
	 */
	public static void useCachesIfNecessary(URLConnection con) {
		con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
	}

	private static String cleanClassLoaderResourceName(String name) {
		String nameToUse = name;
		while (nameToUse.length() != 0 && nameToUse.startsWith("/")) {
			nameToUse = nameToUse.substring(1);
		}
		return nameToUse;
	}

	public static URL getResource(ClassLoader classLoader, String name) {
		String nameToUse = cleanClassLoaderResourceName(name);
		URL url = classLoader.getResource(nameToUse);
		if (url == null) {
			url = classLoader.getResource(RESOURCE_PREFIX + nameToUse);
		}
		return url;
	}
	
	public static URL getResource(Class<?> clazz, String name){
		if(clazz == null || name == null){
			return null;
		}
		
		String nameToUse = cleanClassLoaderResourceName(name);
		URL url = clazz.getResource(nameToUse);
		if(url == null){
			url = clazz.getClassLoader().getResource(nameToUse);
		}
		
		if(url == null){
			url = clazz.getResource(RESOURCE_PREFIX + nameToUse);
		}
		
		if(url == null){
			url = clazz.getClassLoader().getResource(RESOURCE_PREFIX + nameToUse);
		}
		return url;
	}

	public static InputStream getResourceAsStream(ClassLoader classLoader,
			String name) {
		if(classLoader == null || name == null){
			return null;
		}
		
		String nameToUse = cleanClassLoaderResourceName(name);
		InputStream is = classLoader.getResourceAsStream(nameToUse);
		if (is == null) {
			is = classLoader.getResourceAsStream(RESOURCE_PREFIX + nameToUse);
		}
		return is;
	}
	
	public static InputStream getResourceAsStream(Class<?> clazz, String name){
		if(clazz == null || name == null){
			return null;
		}
		
		String nameToUse = cleanClassLoaderResourceName(name);
		InputStream is = clazz.getResourceAsStream(nameToUse);
		if(is == null){
			is = clazz.getClassLoader().getResourceAsStream(nameToUse);
		}
		
		if(is == null){
			is = clazz.getResourceAsStream(RESOURCE_PREFIX + nameToUse);
		}
		
		if(is == null){
			is = clazz.getClassLoader().getResourceAsStream(RESOURCE_PREFIX + nameToUse);
		}
		return is;
	}

	/**
	 * @param classLoader
	 * @param name
	 * @return 不会为空
	 * @see Enumeration#hasMoreElements()
	 * @throws IOException
	 */
	public static Enumeration<URL> getResources(ClassLoader classLoader, String name) throws IOException{
		if(classLoader == null || name == null){
			return Collections.emptyEnumeration();
		}
		
		String nameToUse = cleanClassLoaderResourceName(name);
		Enumeration<URL> urls = classLoader.getResources(nameToUse);
		if(urls == null || !urls.hasMoreElements()){
			urls = classLoader.getResources(RESOURCE_PREFIX + nameToUse);
		}
		
		if(urls == null){
			return Collections.emptyEnumeration();
		}
		return urls;
	}
	
	public static URL getClassLoaderResource(String name){
		if(name == null){
			return null;
		}
		
		URL url = getResource(ClassUtils.getDefaultClassLoader(), name);
		if(url == null){
			String nameToUse = cleanClassLoaderResourceName(name);
			url = ClassLoader.getSystemResource(nameToUse);
			if(url == null){
				url = ClassLoader.getSystemResource(RESOURCE_PREFIX + nameToUse);
			}
		}
		return url;
	}
	
	/**
	 * @param name
	 * @return 不会为空
	 * @see Enumeration#hasMoreElements()
	 * @throws IOException
	 */
	public static Enumeration<URL> getClassLoaderResources(String name) throws IOException{
		if(name == null){
			return null;
		}
		
		Enumeration<URL> urls = getResources(ClassUtils.getDefaultClassLoader(), name);
		if(urls == null || !urls.hasMoreElements()){
			String nameToUse = cleanClassLoaderResourceName(name);
			urls = ClassLoader.getSystemResources(nameToUse);
			if(urls == null || !urls.hasMoreElements()){
				urls = ClassLoader.getSystemResources(RESOURCE_PREFIX + nameToUse);
			}
		}
		return urls.hasMoreElements()? urls : null;
	}
	
	public static InputStream getClassLoaderResourceAsStream(String name){
		if(name == null){
			return null;
		}
		
		InputStream is = getResourceAsStream(ClassUtils.getDefaultClassLoader(), name);
		if(is == null){
			String nameToUse = cleanClassLoaderResourceName(name);
			is = ClassLoader.getSystemResourceAsStream(nameToUse);
			if(is == null){
				is = ClassLoader.getSystemResourceAsStream(RESOURCE_PREFIX + nameToUse);
			}
		}
		return is;
	}
	
	public static List<Resource> toUrlResources(Enumeration<URL> urls){
		if(urls == null || !urls.hasMoreElements()){
			return Collections.emptyList();
		}
		
		List<Resource> list = new ArrayList<Resource>(8);
		while(urls.hasMoreElements()){
			URL url = urls.nextElement();
			if(url == null){
				continue;
			}
			
			list.add(new UrlResource(url));
		}
		return list;
	}
	
	public static List<String> getLines(Resource resource, String charsetName) {
		if (resource == null || !resource.exists()) {
			return Collections.emptyList();
		}

		InputStream is = null;
		try {
			is = resource.getInputStream();
			return IOUtils.readLines(is, charsetName);
		} catch (IOException e) {
			throw new NestedRuntimeException(resource.getDescription(), e);
		} finally {
			if (!resource.isOpen()) {
				IOUtils.close(is);
			}
		}
	}

	public static List<String> getLines(Resource resource, Charset charset) {
		return getLines(resource, charset.name());
	}

	public static String getContent(Resource resource, String charsetName) {
		if (resource == null || !resource.exists()) {
			return null;
		}

		InputStream is = null;
		try {
			is = resource.getInputStream();
			return IOUtils.readContent(is, charsetName);
		} catch (IOException e) {
			throw new NestedRuntimeException(resource.getDescription(), e);
		} finally {
			if (!resource.isOpen()) {
				IOUtils.close(is);
			}
		}
	}

	public static String getContent(Resource resource, Charset charset) {
		return getContent(resource, charset.name());
	}

	public static byte[] getBytes(Resource resource) {
		if (resource == null || !resource.exists()) {
			return null;
		}

		InputStream is = null;
		try {
			is = resource.getInputStream();
			return IOUtils.toByteArray(is);
		} catch (IOException e) {
			throw new NestedRuntimeException(resource.getDescription(), e);
		} finally {
			if (!resource.isOpen()) {
				IOUtils.close(is);
			}
		}
	}

	public static UnsafeByteArrayInputStream getInputStream(Resource resource) {
		byte[] data = getBytes(resource);
		if (data == null) {
			return null;
		}

		return new UnsafeByteArrayInputStream(data);
	}

	public static void loadProperties(Properties properties, Resource resource,
			String charsetName) {
		if (!resource.exists()) {
			return;
		}

		InputStream is = null;
		try {
			is = resource.getInputStream();
			if (resource.getFilename().endsWith(".xml")) {
				properties.loadFromXML(is);
			} else {
				if (StringUtils.isEmpty(charsetName)) {
					properties.load(is);
				} else {
					Method method = ReflectionUtils.getMethod(Properties.class,
							"load", Reader.class);
					if (method == null) {
						FormatUtils.warn(
								ResourceUtils.class,
								"jdk1.6及以上的版本才支持指定字符集: {}"
										+ resource.getDescription());
						properties.load(is);
					} else {
						InputStreamReader isr = null;
						try {
							isr = new InputStreamReader(is, charsetName);
							method.invoke(properties, isr);
						} finally {
							if (!resource.isOpen()) {
								IOUtils.close(isr);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new NestedRuntimeException(resource.getDescription(), e);
		} finally {
			if (!resource.isOpen()) {
				IOUtils.close(is);
			}
		}
	}

	public static String getContent(String location, Charset charset) {
		Resource resource = getResourceOperations().getResource(location);
		return getContent(resource, charset);
	}

	public static String getContent(String location, String charsetName) {
		Resource resource = getResourceOperations().getResource(location);
		return getContent(resource, charsetName);
	}

	public static List<String> getLines(String location, Charset charset) {
		Resource resource = getResourceOperations().getResource(location);
		return getLines(resource, charset);
	}

	public static List<String> getLines(String location, String charsetName) {
		Resource resource = getResourceOperations().getResource(location);
		return getLines(resource, charsetName);
	}
}
