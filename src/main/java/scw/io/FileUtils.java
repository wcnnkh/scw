package scw.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import scw.core.Base64;
import scw.core.exception.AlreadyExistsException;
import scw.io.serializer.SerializerUtils;

public final class FileUtils {

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	private FileUtils() {
	}

	/**
	 * The number of bytes in a kilobyte.
	 */
	public static final long ONE_KB = 1024;

	/**
	 * The number of bytes in a megabyte.
	 */
	public static final long ONE_MB = ONE_KB * ONE_KB;

	/**
	 * The number of bytes in a gigabyte.
	 */
	public static final long ONE_GB = ONE_KB * ONE_MB;

	/**
	 * The number of bytes in a terabyte.
	 */
	public static final long ONE_TB = ONE_KB * ONE_GB;

	/**
	 * The number of bytes in a petabyte.
	 */
	public static final long ONE_PB = ONE_KB * ONE_TB;

	/**
	 * The number of bytes in an exabyte.
	 */
	public static final long ONE_EB = ONE_KB * ONE_PB;

	/**
	 * The number of bytes in a zettabyte.
	 */
	public static final BigInteger ONE_ZB = BigInteger.valueOf(ONE_KB).multiply(BigInteger.valueOf(ONE_EB));

	/**
	 * The number of bytes in a yottabyte.
	 */
	public static final BigInteger ONE_YB = ONE_ZB.multiply(BigInteger.valueOf(ONE_EB));

	/**
	 * An empty array of type <code>File</code>.
	 */
	public static final File[] EMPTY_FILE_ARRAY = new File[0];

	public static File getFile(File directory, String... names) {
		if (directory == null) {
			throw new NullPointerException("directorydirectory must not be null");
		}
		if (names == null) {
			throw new NullPointerException("names must not be null");
		}
		File file = directory;
		for (String name : names) {
			file = new File(file, name);
		}
		return file;
	}

	public static File getFile(String... names) {
		if (names == null) {
			throw new NullPointerException("names must not be null");
		}
		File file = null;
		for (String name : names) {
			if (file == null) {
				file = new File(name);
			} else {
				file = new File(file, name);
			}
		}
		return file;
	}

	public static String getTempDirectoryPath() {
		return System.getProperty("java.io.tmpdir");
	}

	public static File getTempDirectory() {
		return new File(getTempDirectoryPath());
	}

	public static String getUserDirectoryPath() {
		return System.getProperty("user.home");
	}

	public static File getUserDirectory() {
		return new File(getUserDirectoryPath());
	}

	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canRead() == false) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return new FileInputStream(file);
	}

	public static FileOutputStream openOutputStream(File file) throws IOException {
		return openOutputStream(file, false);
	}

	public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canWrite() == false) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			File parent = file.getParentFile();
			if (parent != null) {
				if (!parent.mkdirs() && !parent.isDirectory()) {
					throw new IOException("Directory '" + parent + "' could not be created");
				}
			}
		}
		return new FileOutputStream(file, append);
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns a human-readable version of the file size, where the input
	 * represents a specific number of bytes.
	 * 
	 * If the size is over 1GB, the size is returned as the number of whole GB,
	 * i.e. the size is rounded down to the nearest GB boundary.
	 * 
	 * Similarly for the 1MB and 1KB boundaries.
	 *
	 * @param size
	 *            the number of bytes
	 * @return a human-readable display value (includes units - GB, MB, KB or
	 *         bytes)
	 */
	// See https://issues.apache.org/jira/browse/IO-226 - should the rounding be
	// changed?
	public static String byteCountToDisplaySize(long size) {
		String displaySize;

		if (size / ONE_EB > 0) {
			displaySize = String.valueOf(size / ONE_EB) + " EB";
		} else if (size / ONE_PB > 0) {
			displaySize = String.valueOf(size / ONE_PB) + " PB";
		} else if (size / ONE_TB > 0) {
			displaySize = String.valueOf(size / ONE_TB) + " TB";
		} else if (size / ONE_GB > 0) {
			displaySize = String.valueOf(size / ONE_GB) + " GB";
		} else if (size / ONE_MB > 0) {
			displaySize = String.valueOf(size / ONE_MB) + " MB";
		} else if (size / ONE_KB > 0) {
			displaySize = String.valueOf(size / ONE_KB) + " KB";
		} else {
			displaySize = String.valueOf(size) + " bytes";
		}
		return displaySize;
	}

	public static void touch(File file) throws IOException {
		if (!file.exists()) {
			OutputStream out = openOutputStream(file);
			IOUtils.close(out);
		}
		boolean success = file.setLastModified(System.currentTimeMillis());
		if (!success) {
			throw new IOException("Unable to set the last modification time for " + file);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Converts a Collection containing java.io.File instanced into array
	 * representation. This is to account for the difference between
	 * File.listFiles() and FileUtils.listFiles().
	 *
	 * @param files
	 *            a Collection containing java.io.File instances
	 * @return an array of java.io.File
	 */
	public static File[] convertFileCollectionToFileArray(Collection<File> files) {
		return files.toArray(new File[files.size()]);
	}

	// -----------------------------------------------------------------------
	/**
	 * Compares the contents of two files to determine if they are equal or not.
	 * <p>
	 * This method checks to see if the two files are different lengths or if
	 * they point to the same file, before resorting to byte-by-byte comparison
	 * of the contents.
	 * <p>
	 * Code origin: Avalon
	 *
	 * @param file1
	 *            the first file
	 * @param file2
	 *            the second file
	 * @return true if the content of the files are equal or they both don't
	 *         exist, false otherwise
	 * @throws IOException
	 *             in case of an I/O error
	 */
	public static boolean contentEquals(File file1, File file2) throws IOException {
		boolean file1Exists = file1.exists();
		if (file1Exists != file2.exists()) {
			return false;
		}

		if (!file1Exists) {
			// two not existing files are equal
			return true;
		}

		if (file1.isDirectory() || file2.isDirectory()) {
			// don't want to compare directory contents
			throw new IOException("Can't compare directories, only files");
		}

		if (file1.length() != file2.length()) {
			// lengths differ, cannot be equal
			return false;
		}

		if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
			// same file
			return true;
		}

		InputStream input1 = null;
		InputStream input2 = null;
		try {
			input1 = new FileInputStream(file1);
			input2 = new FileInputStream(file2);
			return IOUtils.contentEquals(input1, input2);

		} finally {
			IOUtils.closeQuietly(input1);
			IOUtils.closeQuietly(input2);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Compares the contents of two files to determine if they are equal or not.
	 * <p>
	 * This method checks to see if the two files point to the same file, before
	 * resorting to line-by-line comparison of the contents.
	 * <p>
	 *
	 * @param file1
	 *            the first file
	 * @param file2
	 *            the second file
	 * @param charsetName
	 *            the character encoding to be used. May be null, in which case
	 *            the platform default is used
	 * @return true if the content of the files are equal or neither exists,
	 *         false otherwise
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 2.2
	 * @see IOUtils#contentEqualsIgnoreEOL(Reader, Reader)
	 */
	public static boolean contentEqualsIgnoreEOL(File file1, File file2, String charsetName) throws IOException {
		boolean file1Exists = file1.exists();
		if (file1Exists != file2.exists()) {
			return false;
		}

		if (!file1Exists) {
			// two not existing files are equal
			return true;
		}

		if (file1.isDirectory() || file2.isDirectory()) {
			// don't want to compare directory contents
			throw new IOException("Can't compare directories, only files");
		}

		if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
			// same file
			return true;
		}

		Reader input1 = null;
		Reader input2 = null;
		try {
			if (charsetName == null) {
				input1 = new InputStreamReader(new FileInputStream(file1));
				input2 = new InputStreamReader(new FileInputStream(file2));
			} else {
				input1 = new InputStreamReader(new FileInputStream(file1), charsetName);
				input2 = new InputStreamReader(new FileInputStream(file2), charsetName);
			}
			return IOUtils.contentEqualsIgnoreEOL(input1, input2);

		} finally {
			IOUtils.closeQuietly(input1);
			IOUtils.closeQuietly(input2);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Convert from a <code>URL</code> to a <code>File</code>.
	 * <p>
	 * From version 1.1 this method will decode the URL. Syntax such as
	 * <code>file:///my%20docs/file.txt</code> will be correctly decoded to
	 * <code>/my docs/file.txt</code>. Starting with version 1.5, this method
	 * uses UTF-8 to decode percent-encoded octets to characters. Additionally,
	 * malformed percent-encoded octets are handled leniently by passing them
	 * through literally.
	 *
	 * @param url
	 *            the file URL to convert, <code>null</code> returns
	 *            <code>null</code>
	 * @return the equivalent <code>File</code> object, or <code>null</code> if
	 *         the URL's protocol is not <code>file</code>
	 */
	public static File toFile(URL url, Charset charset) {
		if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
			return null;
		} else {
			String filename = url.getFile().replace('/', File.separatorChar);
			filename = decodeUrl(filename, charset);
			return new File(filename);
		}
	}

	/**
	 * Decodes the specified URL as per RFC 3986, i.e. transforms
	 * percent-encoded octets to characters by decoding with the UTF-8 character
	 * set. This function is primarily intended for usage with
	 * {@link java.net.URL} which unfortunately does not enforce proper URLs. As
	 * such, this method will leniently accept invalid characters or malformed
	 * percent-encoded octets and simply pass them literally through to the
	 * result string. Except for rare edge cases, this will make unencoded URLs
	 * pass through unaltered.
	 * 
	 * @param url
	 *            The URL to decode, may be <code>null</code>.
	 * @return The decoded URL or <code>null</code> if the input was
	 *         <code>null</code>.
	 */
	static String decodeUrl(String url, Charset charset) {
		String decoded = url;
		if (url != null && url.indexOf('%') >= 0) {
			int n = url.length();
			StringBuffer buffer = new StringBuffer();
			ByteBuffer bytes = ByteBuffer.allocate(n);
			for (int i = 0; i < n;) {
				if (url.charAt(i) == '%') {
					try {
						do {
							byte octet = (byte) Integer.parseInt(url.substring(i + 1, i + 3), 16);
							bytes.put(octet);
							i += 3;
						} while (i < n && url.charAt(i) == '%');
						continue;
					} catch (RuntimeException e) {
						// malformed percent-encoded octet, fall through and
						// append characters literally
					} finally {
						if (bytes.position() > 0) {
							bytes.flip();
							buffer.append(charset.decode(bytes).toString());
							bytes.clear();
						}
					}
				}
				buffer.append(url.charAt(i++));
			}
			decoded = buffer.toString();
		}
		return decoded;
	}

	/**
	 * Converts each of an array of <code>URL</code> to a <code>File</code>.
	 * <p>
	 * Returns an array of the same size as the input. If the input is
	 * <code>null</code>, an empty array is returned. If the input contains
	 * <code>null</code>, the output array contains <code>null</code> at the
	 * same index.
	 * <p>
	 * This method will decode the URL. Syntax such as
	 * <code>file:///my%20docs/file.txt</code> will be correctly decoded to
	 * <code>/my docs/file.txt</code>.
	 *
	 * @param urls
	 *            the file URLs to convert, <code>null</code> returns empty
	 *            array
	 * @return a non-<code>null</code> array of Files matching the input, with a
	 *         <code>null</code> item if there was a <code>null</code> at that
	 *         index in the input array
	 * @throws IllegalArgumentException
	 *             if any file is not a URL file
	 * @throws IllegalArgumentException
	 *             if any file is incorrectly encoded
	 * @since 1.1
	 */
	public static File[] toFiles(URL[] urls, Charset charset) {
		if (urls == null || urls.length == 0) {
			return EMPTY_FILE_ARRAY;
		}
		File[] files = new File[urls.length];
		for (int i = 0; i < urls.length; i++) {
			URL url = urls[i];
			if (url != null) {
				if (url.getProtocol().equals("file") == false) {
					throw new IllegalArgumentException("URL could not be converted to a File: " + url);
				}
				files[i] = toFile(url, charset);
			}
		}
		return files;
	}

	/**
	 * Converts each of an array of <code>File</code> to a <code>URL</code>.
	 * <p>
	 * Returns an array of the same size as the input.
	 *
	 * @param files
	 *            the files to convert
	 * @return an array of URLs matching the input
	 * @throws IOException
	 *             if a file cannot be converted
	 */
	public static URL[] toURLs(File[] files) throws IOException {
		URL[] urls = new URL[files.length];

		for (int i = 0; i < urls.length; i++) {
			urls[i] = files[i].toURI().toURL();
		}

		return urls;
	}

	// -----------------------------------------------------------------------
	/**
	 * Copies a file to a directory preserving the file date.
	 * <p>
	 * This method copies the contents of the specified source file to a file of
	 * the same name in the specified destination directory. The destination
	 * directory is created if it does not exist. If the destination file
	 * exists, then this method will overwrite it.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the file's last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that the operation will succeed. If the modification
	 * operation fails, no indication is provided.
	 *
	 * @param srcFile
	 *            an existing file to copy, must not be <code>null</code>
	 * @param destDir
	 *            the directory to place the copy in, must not be
	 *            <code>null</code>
	 *
	 * @throws NullPointerException
	 *             if source or destination is null
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @see #copyFile(File, File, boolean)
	 */
	public static void copyFileToDirectory(File srcFile, File destDir, long bufSize) throws IOException {
		copyFileToDirectory(srcFile, destDir, true, bufSize);
	}

	/**
	 * Copies a file to a directory optionally preserving the file date.
	 * <p>
	 * This method copies the contents of the specified source file to a file of
	 * the same name in the specified destination directory. The destination
	 * directory is created if it does not exist. If the destination file
	 * exists, then this method will overwrite it.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * <code>true</code> tries to preserve the file's last modified date/times
	 * using {@link File#setLastModified(long)}, however it is not guaranteed
	 * that the operation will succeed. If the modification operation fails, no
	 * indication is provided.
	 *
	 * @param srcFile
	 *            an existing file to copy, must not be <code>null</code>
	 * @param destDir
	 *            the directory to place the copy in, must not be
	 *            <code>null</code>
	 * @param preserveFileDate
	 *            true if the file date of the copy should be the same as the
	 *            original
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @see #copyFile(File, File, boolean)
	 * @since 1.3
	 */
	public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate, long bufSize)
			throws IOException {
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (destDir.exists() && destDir.isDirectory() == false) {
			throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
		}
		File destFile = new File(destDir, srcFile.getName());
		copyFile(srcFile, destFile, preserveFileDate, bufSize);
	}

	/**
	 * Copies a file to a new location preserving the file date.
	 * <p>
	 * This method copies the contents of the specified source file to the
	 * specified destination file. The directory holding the destination file is
	 * created if it does not exist. If the destination file exists, then this
	 * method will overwrite it.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the file's last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that the operation will succeed. If the modification
	 * operation fails, no indication is provided.
	 * 
	 * @param srcFile
	 *            an existing file to copy, must not be <code>null</code>
	 * @param destFile
	 *            the new file, must not be <code>null</code>
	 * 
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @see #copyFileToDirectory(File, File)
	 */
	public static void copyFile(File srcFile, File destFile, long bufSize) throws IOException {
		copyFile(srcFile, destFile, true, bufSize);
	}

	/**
	 * Copies a file to a new location.
	 * <p>
	 * This method copies the contents of the specified source file to the
	 * specified destination file. The directory holding the destination file is
	 * created if it does not exist. If the destination file exists, then this
	 * method will overwrite it.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * <code>true</code> tries to preserve the file's last modified date/times
	 * using {@link File#setLastModified(long)}, however it is not guaranteed
	 * that the operation will succeed. If the modification operation fails, no
	 * indication is provided.
	 *
	 * @param srcFile
	 *            an existing file to copy, must not be <code>null</code>
	 * @param destFile
	 *            the new file, must not be <code>null</code>
	 * @param preserveFileDate
	 *            true if the file date of the copy should be the same as the
	 *            original
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @see #copyFileToDirectory(File, File, boolean)
	 */
	public static void copyFile(File srcFile, File destFile, boolean preserveFileDate, long bufSize)
			throws IOException {
		if (srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destFile == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (srcFile.exists() == false) {
			throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
		}
		if (srcFile.isDirectory()) {
			throw new IOException("Source '" + srcFile + "' exists but is a directory");
		}
		if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
			throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
		}
		File parentFile = destFile.getParentFile();
		if (parentFile != null) {
			if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
				throw new IOException("Destination '" + parentFile + "' directory cannot be created");
			}
		}
		if (destFile.exists() && destFile.canWrite() == false) {
			throw new IOException("Destination '" + destFile + "' exists but is read-only");
		}
		doCopyFile(srcFile, destFile, preserveFileDate, bufSize);
	}

	/**
	 * Copy bytes from a <code>File</code> to an <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * </p>
	 * 
	 * @param input
	 *            the <code>File</code> to read from
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException
	 *             if the input or output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since 2.1
	 */
	public static long copyFile(File input, OutputStream output) throws IOException {
		final FileInputStream fis = new FileInputStream(input);
		try {
			return IOUtils.copyLarge(fis, output);
		} finally {
			fis.close();
		}
	}

	/**
	 * Internal copy file method.
	 * 
	 * @param srcFile
	 *            the validated source file, must not be <code>null</code>
	 * @param destFile
	 *            the validated destination file, must not be <code>null</code>
	 * @param preserveFileDate
	 *            whether to preserve the file date
	 * @throws IOException
	 *             if an error occurs
	 */
	private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate, long bufSize)
			throws IOException {
		if (destFile.exists() && destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile + "' exists but is a directory");
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			input = fis.getChannel();
			output = fos.getChannel();
			long size = input.size();
			long pos = 0;
			long count = 0;
			while (pos < size) {
				count = size - pos > bufSize ? bufSize : size - pos;
				pos += output.transferFrom(input, pos, count);
			}
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(fis);
		}

		if (srcFile.length() != destFile.length()) {
			throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
		}
		if (preserveFileDate) {
			destFile.setLastModified(srcFile.lastModified());
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Copies a directory to within another directory preserving the file dates.
	 * <p>
	 * This method copies the source directory and all its contents to a
	 * directory of the same name in the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that those operations will succeed. If the modification
	 * operation fails, no indication is provided.
	 *
	 * @param srcDir
	 *            an existing directory to copy, must not be <code>null</code>
	 * @param destDir
	 *            the directory to place the copy in, must not be
	 *            <code>null</code>
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since 1.2
	 */
	public static void copyDirectoryToDirectory(File srcDir, File destDir, long bufSize) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (srcDir.exists() && srcDir.isDirectory() == false) {
			throw new IllegalArgumentException("Source '" + destDir + "' is not a directory");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (destDir.exists() && destDir.isDirectory() == false) {
			throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
		}
		copyDirectory(srcDir, new File(destDir, srcDir.getName()), true, bufSize);
	}

	/**
	 * Copies a whole directory to a new location preserving the file dates.
	 * <p>
	 * This method copies the specified directory and all its child directories
	 * and files to the specified destination. The destination is the new
	 * location and name of the directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that those operations will succeed. If the modification
	 * operation fails, no indication is provided.
	 *
	 * @param srcDir
	 *            an existing directory to copy, must not be <code>null</code>
	 * @param destDir
	 *            the new directory, must not be <code>null</code>
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since 1.1
	 */
	public static void copyDirectory(File srcDir, File destDir, long bufSize) throws IOException {
		copyDirectory(srcDir, destDir, true, bufSize);
	}

	/**
	 * Copies a whole directory to a new location.
	 * <p>
	 * This method copies the contents of the specified source directory to
	 * within the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * <code>true</code> tries to preserve the files' last modified date/times
	 * using {@link File#setLastModified(long)}, however it is not guaranteed
	 * that those operations will succeed. If the modification operation fails,
	 * no indication is provided.
	 *
	 * @param srcDir
	 *            an existing directory to copy, must not be <code>null</code>
	 * @param destDir
	 *            the new directory, must not be <code>null</code>
	 * @param preserveFileDate
	 *            true if the file date of the copy should be the same as the
	 *            original
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since 1.1
	 */
	public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate, long bufSize)
			throws IOException {
		copyDirectory(srcDir, destDir, null, preserveFileDate, bufSize);
	}

	/**
	 * Copies a filtered directory to a new location preserving the file dates.
	 * <p>
	 * This method copies the contents of the specified source directory to
	 * within the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that those operations will succeed. If the modification
	 * operation fails, no indication is provided.
	 *
	 * <h4>Example: Copy directories only</h4>
	 * 
	 * <pre>
	 * // only copy the directory structure
	 * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
	 * </pre>
	 *
	 * <h4>Example: Copy directories and txt files</h4>
	 * 
	 * <pre>
	 * // Create a filter for &quot;.txt&quot; files
	 * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(&quot;.txt&quot;);
	 * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	 * 
	 * // Create a filter for either directories or &quot;.txt&quot; files
	 * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	 * 
	 * // Copy using the filter
	 * FileUtils.copyDirectory(srcDir, destDir, filter);
	 * </pre>
	 *
	 * @param srcDir
	 *            an existing directory to copy, must not be <code>null</code>
	 * @param destDir
	 *            the new directory, must not be <code>null</code>
	 * @param filter
	 *            the filter to apply, null means copy all directories and files
	 *            should be the same as the original
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since 1.4
	 */
	public static void copyDirectory(File srcDir, File destDir, FileFilter filter, long bufSize) throws IOException {
		copyDirectory(srcDir, destDir, filter, true, bufSize);
	}

	/**
	 * Copies a filtered directory to a new location.
	 * <p>
	 * This method copies the contents of the specified source directory to
	 * within the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * <code>true</code> tries to preserve the files' last modified date/times
	 * using {@link File#setLastModified(long)}, however it is not guaranteed
	 * that those operations will succeed. If the modification operation fails,
	 * no indication is provided.
	 *
	 * <h4>Example: Copy directories only</h4>
	 * 
	 * <pre>
	 * // only copy the directory structure
	 * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
	 * </pre>
	 *
	 * <h4>Example: Copy directories and txt files</h4>
	 * 
	 * <pre>
	 * // Create a filter for &quot;.txt&quot; files
	 * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(&quot;.txt&quot;);
	 * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	 * 
	 * // Create a filter for either directories or &quot;.txt&quot; files
	 * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	 * 
	 * // Copy using the filter
	 * FileUtils.copyDirectory(srcDir, destDir, filter, false);
	 * </pre>
	 * 
	 * @param srcDir
	 *            an existing directory to copy, must not be <code>null</code>
	 * @param destDir
	 *            the new directory, must not be <code>null</code>
	 * @param filter
	 *            the filter to apply, null means copy all directories and files
	 * @param preserveFileDate
	 *            true if the file date of the copy should be the same as the
	 *            original
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since 1.4
	 */
	public static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate,
			long bufSize) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (srcDir.exists() == false) {
			throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
		}
		if (srcDir.isDirectory() == false) {
			throw new IOException("Source '" + srcDir + "' exists but is not a directory");
		}
		if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
			throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
		}

		// Cater for destination being directory within the source directory
		// (see IO-141)
		List<String> exclusionList = null;
		if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
			File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
			if (srcFiles != null && srcFiles.length > 0) {
				exclusionList = new ArrayList<String>(srcFiles.length);
				for (File srcFile : srcFiles) {
					File copiedFile = new File(destDir, srcFile.getName());
					exclusionList.add(copiedFile.getCanonicalPath());
				}
			}
		}
		doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList, bufSize);
	}

	/**
	 * Internal copy directory method.
	 * 
	 * @param srcDir
	 *            the validated source directory, must not be <code>null</code>
	 * @param destDir
	 *            the validated destination directory, must not be
	 *            <code>null</code>
	 * @param filter
	 *            the filter to apply, null means copy all directories and files
	 * @param preserveFileDate
	 *            whether to preserve the file date
	 * @param exclusionList
	 *            List of files and directories to exclude from the copy, may be
	 *            null
	 * @throws IOException
	 *             if an error occurs
	 * @since 1.1
	 */
	private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate,
			List<String> exclusionList, long bufSize) throws IOException {
		// recurse
		File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
		if (srcFiles == null) { // null if abstract pathname does not denote a
								// directory, or if an I/O error occurs
			throw new IOException("Failed to list contents of " + srcDir);
		}
		if (destDir.exists()) {
			if (destDir.isDirectory() == false) {
				throw new IOException("Destination '" + destDir + "' exists but is not a directory");
			}
		} else {
			if (!destDir.mkdirs() && !destDir.isDirectory()) {
				throw new IOException("Destination '" + destDir + "' directory cannot be created");
			}
		}
		if (destDir.canWrite() == false) {
			throw new IOException("Destination '" + destDir + "' cannot be written to");
		}
		for (File srcFile : srcFiles) {
			File dstFile = new File(destDir, srcFile.getName());
			if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
				if (srcFile.isDirectory()) {
					doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList, bufSize);
				} else {
					doCopyFile(srcFile, dstFile, preserveFileDate, bufSize);
				}
			}
		}

		// Do this last, as the above has probably affected directory metadata
		if (preserveFileDate) {
			destDir.setLastModified(srcDir.lastModified());
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Copies bytes from the URL <code>source</code> to a file
	 * <code>destination</code>. The directories up to <code>destination</code>
	 * will be created if they don't already exist. <code>destination</code>
	 * will be overwritten if it already exists.
	 * <p>
	 * Warning: this method does not set a connection or read timeout and thus
	 * might block forever. Use {@link #copyURLToFile(URL, File, int, int)} with
	 * reasonable timeouts to prevent this.
	 *
	 * @param source
	 *            the <code>URL</code> to copy bytes from, must not be
	 *            <code>null</code>
	 * @param destination
	 *            the non-directory <code>File</code> to write bytes to
	 *            (possibly overwriting), must not be <code>null</code>
	 * @throws IOException
	 *             if <code>source</code> URL cannot be opened
	 * @throws IOException
	 *             if <code>destination</code> is a directory
	 * @throws IOException
	 *             if <code>destination</code> cannot be written
	 * @throws IOException
	 *             if <code>destination</code> needs creating but can't be
	 * @throws IOException
	 *             if an IO error occurs during copying
	 */
	public static void copyURLToFile(URL source, File destination) throws IOException {
		InputStream input = source.openStream();
		copyInputStreamToFile(input, destination);
	}

	/**
	 * Copies bytes from the URL <code>source</code> to a file
	 * <code>destination</code>. The directories up to <code>destination</code>
	 * will be created if they don't already exist. <code>destination</code>
	 * will be overwritten if it already exists.
	 *
	 * @param source
	 *            the <code>URL</code> to copy bytes from, must not be
	 *            <code>null</code>
	 * @param destination
	 *            the non-directory <code>File</code> to write bytes to
	 *            (possibly overwriting), must not be <code>null</code>
	 * @param connectionTimeout
	 *            the number of milliseconds until this method will timeout if
	 *            no connection could be established to the <code>source</code>
	 * @param readTimeout
	 *            the number of milliseconds until this method will timeout if
	 *            no data could be read from the <code>source</code>
	 * @throws IOException
	 *             if <code>source</code> URL cannot be opened
	 * @throws IOException
	 *             if <code>destination</code> is a directory
	 * @throws IOException
	 *             if <code>destination</code> cannot be written
	 * @throws IOException
	 *             if <code>destination</code> needs creating but can't be
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since 2.0
	 */
	public static void copyURLToFile(URL source, File destination, int connectionTimeout, int readTimeout)
			throws IOException {
		URLConnection connection = source.openConnection();
		connection.setConnectTimeout(connectionTimeout);
		connection.setReadTimeout(readTimeout);
		InputStream input = connection.getInputStream();
		copyInputStreamToFile(input, destination);
	}

	/**
	 * Copies bytes from an {@link InputStream} <code>source</code> to a file
	 * <code>destination</code>. The directories up to <code>destination</code>
	 * will be created if they don't already exist. <code>destination</code>
	 * will be overwritten if it already exists.
	 *
	 * @param source
	 *            the <code>InputStream</code> to copy bytes from, must not be
	 *            <code>null</code>
	 * @param destination
	 *            the non-directory <code>File</code> to write bytes to
	 *            (possibly overwriting), must not be <code>null</code>
	 * @throws IOException
	 *             if <code>destination</code> is a directory
	 * @throws IOException
	 *             if <code>destination</code> cannot be written
	 * @throws IOException
	 *             if <code>destination</code> needs creating but can't be
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since 2.0
	 */
	public static void copyInputStreamToFile(InputStream source, File destination) throws IOException {
		try {
			FileOutputStream output = openOutputStream(destination);
			try {
				IOUtils.copy(source, output);
				output.close(); // don't swallow close Exception if copy
								// completes normally
			} finally {
				IOUtils.closeQuietly(output);
			}
		} finally {
			IOUtils.closeQuietly(source);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Deletes a directory recursively.
	 *
	 * @param directory
	 *            directory to delete
	 * @throws IOException
	 *             in case deletion is unsuccessful
	 */
	public static void deleteDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			return;
		}

		if (!isSymlink(directory)) {
			cleanDirectory(directory);
		}

		if (!directory.delete()) {
			String message = "Unable to delete directory " + directory + ".";
			throw new IOException(message);
		}
	}

	/**
	 * Deletes a file, never throwing an exception. If file is a directory,
	 * delete it and all sub-directories.
	 * <p>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>No exceptions are thrown when a file or directory cannot be
	 * deleted.</li>
	 * </ul>
	 *
	 * @param file
	 *            file or directory to delete, can be <code>null</code>
	 * @return <code>true</code> if the file or directory was deleted, otherwise
	 *         <code>false</code>
	 *
	 * @since 1.4
	 */
	public static boolean deleteQuietly(File file) {
		if (file == null) {
			return false;
		}
		try {
			if (file.isDirectory()) {
				cleanDirectory(file);
			}
		} catch (Exception ignored) {
		}

		try {
			return file.delete();
		} catch (Exception ignored) {
			return false;
		}
	}

	/**
	 * Determines whether the {@code parent} directory contains the
	 * {@code child} element (a file or directory).
	 * <p>
	 * Files are normalized before comparison.
	 * </p>
	 * 
	 * Edge cases:
	 * <ul>
	 * <li>A {@code directory} must not be null: if null, throw
	 * IllegalArgumentException</li>
	 * <li>A {@code directory} must be a directory: if not a directory, throw
	 * IllegalArgumentException</li>
	 * <li>A directory does not contain itself: return false</li>
	 * <li>A null child file is not contained in any parent: return false</li>
	 * </ul>
	 * 
	 * @param directory
	 *            the file to consider as the parent.
	 * @param child
	 *            the file to consider as the child.
	 * @return true is the candidate leaf is under by the specified composite.
	 *         False otherwise.
	 * @throws IOException
	 *             if an IO error occurs while checking the files.
	 * @since 2.2
	 * @see FilenameUtils#directoryContains(String, String)
	 */
	public static boolean directoryContains(final File directory, final File child) throws IOException {

		// Fail fast against NullPointerException
		if (directory == null) {
			throw new IllegalArgumentException("Directory must not be null");
		}

		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + directory);
		}

		if (child == null) {
			return false;
		}

		if (!directory.exists() || !child.exists()) {
			return false;
		}

		// Canonicalize paths (normalizes relative paths)
		String canonicalParent = directory.getCanonicalPath();
		String canonicalChild = child.getCanonicalPath();

		return FilenameUtils.directoryContains(canonicalParent, canonicalChild);
	}

	/**
	 * Cleans a directory without deleting it.
	 *
	 * @param directory
	 *            directory to clean
	 * @throws IOException
	 *             in case cleaning is unsuccessful
	 */
	public static void cleanDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
		}

		IOException exception = null;
		for (File file : files) {
			try {
				forceDelete(file);
			} catch (IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			throw exception;
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Waits for NFS to propagate a file creation, imposing a timeout.
	 * <p>
	 * This method repeatedly tests {@link File#exists()} until it returns true
	 * up to the maximum time specified in seconds.
	 *
	 * @param file
	 *            the file to check, must not be <code>null</code>
	 * @param seconds
	 *            the maximum time in seconds to wait
	 * @return true if file exists
	 * @throws NullPointerException
	 *             if the file is <code>null</code>
	 */
	public static boolean waitFor(File file, int seconds) {
		int timeout = 0;
		int tick = 0;
		while (!file.exists()) {
			if (tick++ >= 10) {
				tick = 0;
				if (timeout++ > seconds) {
					return false;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignore) {
				// ignore exception
			} catch (Exception ex) {
				break;
			}
		}
		return true;
	}

	// -----------------------------------------------------------------------
	/**
	 * Reads the contents of a file into a String. The file is always closed.
	 *
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @return the file contents, never <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 */
	public static String readFileToString(File file, String encoding) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.toString(in, encoding);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Reads the contents of a file into a String using the default encoding for
	 * the VM. The file is always closed.
	 *
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 1.3.1
	 */
	public static String readFileToString(File file) throws IOException {
		return readFileToString(file, null);
	}

	/**
	 * Reads the contents of a file into a byte array. The file is always
	 * closed.
	 *
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 1.1
	 */
	public static byte[] readFileToByteArray(File file) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.toByteArray(in, file.length());
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Reads the contents of a file line by line to a List of Strings. The file
	 * is always closed.
	 *
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @return the list of Strings representing each line in the file, never
	 *         <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 * @since 1.1
	 */
	public static List<String> readLines(File file, String encoding) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.readLines(in, encoding);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Reads the contents of a file line by line to a List of Strings using the
	 * default encoding for the VM. The file is always closed.
	 *
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @return the list of Strings representing each line in the file, never
	 *         <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 1.3
	 */
	public static List<String> readLines(File file) throws IOException {
		return readLines(file, null);
	}

	// -----------------------------------------------------------------------
	/**
	 * Writes a String to a file creating the file if it does not exist.
	 *
	 * NOTE: As from v1.3, the parent directories of the file will be created if
	 * they do not exist.
	 *
	 * @param file
	 *            the file to write
	 * @param data
	 *            the content to write to the file
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 */
	public static void writeStringToFile(File file, String data, String encoding) throws IOException {
		writeStringToFile(file, data, encoding, false);
	}

	/**
	 * Writes a String to a file creating the file if it does not exist.
	 *
	 * @param file
	 *            the file to write
	 * @param data
	 *            the content to write to the file
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @param append
	 *            if <code>true</code>, then the String will be added to the end
	 *            of the file rather than overwriting
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 * @since 2.1
	 */
	public static void writeStringToFile(File file, String data, String encoding, boolean append) throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(file, append);
			IOUtils.write(data, out, encoding);
			out.close(); // don't swallow close Exception if copy completes
							// normally
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Writes a String to a file creating the file if it does not exist using
	 * the default encoding for the VM.
	 * 
	 * @param file
	 *            the file to write
	 * @param data
	 *            the content to write to the file
	 * @throws IOException
	 *             in case of an I/O error
	 */
	public static void writeStringToFile(File file, String data) throws IOException {
		writeStringToFile(file, data, null, false);
	}

	/**
	 * Writes a String to a file creating the file if it does not exist using
	 * the default encoding for the VM.
	 * 
	 * @param file
	 *            the file to write
	 * @param data
	 *            the content to write to the file
	 * @param append
	 *            if <code>true</code>, then the String will be added to the end
	 *            of the file rather than overwriting
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 2.1
	 */
	public static void writeStringToFile(File file, String data, boolean append) throws IOException {
		writeStringToFile(file, data, null, append);
	}

	/**
	 * Writes a CharSequence to a file creating the file if it does not exist
	 * using the default encoding for the VM.
	 * 
	 * @param file
	 *            the file to write
	 * @param data
	 *            the content to write to the file
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 2.0
	 */
	public static void write(File file, CharSequence data) throws IOException {
		write(file, data, null, false);
	}

	/**
	 * Writes a CharSequence to a file creating the file if it does not exist
	 * using the default encoding for the VM.
	 * 
	 * @param file
	 *            the file to write
	 * @param data
	 *            the content to write to the file
	 * @param append
	 *            if <code>true</code>, then the data will be added to the end
	 *            of the file rather than overwriting
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 2.1
	 */
	public static void write(File file, CharSequence data, boolean append) throws IOException {
		write(file, data, null, append);
	}

	/**
	 * Writes a CharSequence to a file creating the file if it does not exist.
	 *
	 * @param file
	 *            the file to write
	 * @param data
	 *            the content to write to the file
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 * @since 2.0
	 */
	public static void write(File file, CharSequence data, String encoding) throws IOException {
		write(file, data, encoding, false);
	}

	/**
	 * Writes a CharSequence to a file creating the file if it does not exist.
	 *
	 * @param file
	 *            the file to write
	 * @param data
	 *            the content to write to the file
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @param append
	 *            if <code>true</code>, then the data will be added to the end
	 *            of the file rather than overwriting
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 * @since IO 2.1
	 */
	public static void write(File file, CharSequence data, String encoding, boolean append) throws IOException {
		String str = data == null ? null : data.toString();
		writeStringToFile(file, str, encoding, append);
	}

	/**
	 * Writes a byte array to a file creating the file if it does not exist.
	 * <p>
	 * NOTE: As from v1.3, the parent directories of the file will be created if
	 * they do not exist.
	 *
	 * @param file
	 *            the file to write to
	 * @param data
	 *            the content to write to the file
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 1.1
	 */
	public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
		writeByteArrayToFile(file, data, false);
	}

	/**
	 * Writes a byte array to a file creating the file if it does not exist.
	 *
	 * @param file
	 *            the file to write to
	 * @param data
	 *            the content to write to the file
	 * @param append
	 *            if <code>true</code>, then bytes will be added to the end of
	 *            the file rather than overwriting
	 * @throws IOException
	 *             in case of an I/O error
	 * @since IO 2.1
	 */
	public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(file, append);
			out.write(data);
			out.close(); // don't swallow close Exception if copy completes
							// normally
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line. The specified character
	 * encoding and the default line ending will be used.
	 * <p>
	 * NOTE: As from v1.3, the parent directories of the file will be created if
	 * they do not exist.
	 *
	 * @param file
	 *            the file to write to
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @param lines
	 *            the lines to write, <code>null</code> entries produce blank
	 *            lines
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 * @since 1.1
	 */
	public static void writeLines(File file, String encoding, Collection<?> lines) throws IOException {
		writeLines(file, encoding, lines, null, false);
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line, optionally appending. The
	 * specified character encoding and the default line ending will be used.
	 *
	 * @param file
	 *            the file to write to
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @param lines
	 *            the lines to write, <code>null</code> entries produce blank
	 *            lines
	 * @param append
	 *            if <code>true</code>, then the lines will be added to the end
	 *            of the file rather than overwriting
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 * @since 2.1
	 */
	public static void writeLines(File file, String encoding, Collection<?> lines, boolean append) throws IOException {
		writeLines(file, encoding, lines, null, append);
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line. The default VM encoding and
	 * the default line ending will be used.
	 *
	 * @param file
	 *            the file to write to
	 * @param lines
	 *            the lines to write, <code>null</code> entries produce blank
	 *            lines
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 1.3
	 */
	public static void writeLines(File file, Collection<?> lines) throws IOException {
		writeLines(file, null, lines, null, false);
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line. The default VM encoding and
	 * the default line ending will be used.
	 *
	 * @param file
	 *            the file to write to
	 * @param lines
	 *            the lines to write, <code>null</code> entries produce blank
	 *            lines
	 * @param append
	 *            if <code>true</code>, then the lines will be added to the end
	 *            of the file rather than overwriting
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 2.1
	 */
	public static void writeLines(File file, Collection<?> lines, boolean append) throws IOException {
		writeLines(file, null, lines, null, append);
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line. The specified character
	 * encoding and the line ending will be used.
	 * <p>
	 * NOTE: As from v1.3, the parent directories of the file will be created if
	 * they do not exist.
	 *
	 * @param file
	 *            the file to write to
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @param lines
	 *            the lines to write, <code>null</code> entries produce blank
	 *            lines
	 * @param lineEnding
	 *            the line separator to use, <code>null</code> is system default
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 * @since 1.1
	 */
	public static void writeLines(File file, String encoding, Collection<?> lines, String lineEnding)
			throws IOException {
		writeLines(file, encoding, lines, lineEnding, false);
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line. The specified character
	 * encoding and the line ending will be used.
	 *
	 * @param file
	 *            the file to write to
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @param lines
	 *            the lines to write, <code>null</code> entries produce blank
	 *            lines
	 * @param lineEnding
	 *            the line separator to use, <code>null</code> is system default
	 * @param append
	 *            if <code>true</code>, then the lines will be added to the end
	 *            of the file rather than overwriting
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 * @since 2.1
	 */
	public static void writeLines(File file, String encoding, Collection<?> lines, String lineEnding, boolean append)
			throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(file, append);
			IOUtils.writeLines(lines, lineEnding, out, encoding);
			out.close(); // don't swallow close Exception if copy completes
							// normally
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line. The default VM encoding and
	 * the specified line ending will be used.
	 *
	 * @param file
	 *            the file to write to
	 * @param lines
	 *            the lines to write, <code>null</code> entries produce blank
	 *            lines
	 * @param lineEnding
	 *            the line separator to use, <code>null</code> is system default
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 1.3
	 */
	public static void writeLines(File file, Collection<?> lines, String lineEnding) throws IOException {
		writeLines(file, null, lines, lineEnding, false);
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line. The default VM encoding and
	 * the specified line ending will be used.
	 *
	 * @param file
	 *            the file to write to
	 * @param lines
	 *            the lines to write, <code>null</code> entries produce blank
	 *            lines
	 * @param lineEnding
	 *            the line separator to use, <code>null</code> is system default
	 * @param append
	 *            if <code>true</code>, then the lines will be added to the end
	 *            of the file rather than overwriting
	 * @throws IOException
	 *             in case of an I/O error
	 * @since 2.1
	 */
	public static void writeLines(File file, Collection<?> lines, String lineEnding, boolean append)
			throws IOException {
		writeLines(file, null, lines, lineEnding, append);
	}

	// -----------------------------------------------------------------------
	/**
	 * Deletes a file. If file is a directory, delete it and all
	 * sub-directories.
	 * <p>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>You get exceptions when a file or directory cannot be deleted.
	 * (java.io.File methods returns a boolean)</li>
	 * </ul>
	 *
	 * @param file
	 *            file or directory to delete, must not be <code>null</code>
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws FileNotFoundException
	 *             if the file was not found
	 * @throws IOException
	 *             in case deletion is unsuccessful
	 */
	public static void forceDelete(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: " + file);
				}
				String message = "Unable to delete file: " + file;
				throw new IOException(message);
			}
		}
	}

	/**
	 * Schedules a file to be deleted when JVM exits. If file is directory
	 * delete it and all sub-directories.
	 *
	 * @param file
	 *            file or directory to delete, must not be <code>null</code>
	 * @throws NullPointerException
	 *             if the file is <code>null</code>
	 * @throws IOException
	 *             in case deletion is unsuccessful
	 */
	public static void forceDeleteOnExit(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectoryOnExit(file);
		} else {
			file.deleteOnExit();
		}
	}

	/**
	 * Schedules a directory recursively for deletion on JVM exit.
	 *
	 * @param directory
	 *            directory to delete, must not be <code>null</code>
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws IOException
	 *             in case deletion is unsuccessful
	 */
	private static void deleteDirectoryOnExit(File directory) throws IOException {
		if (!directory.exists()) {
			return;
		}

		directory.deleteOnExit();
		if (!isSymlink(directory)) {
			cleanDirectoryOnExit(directory);
		}
	}

	/**
	 * Cleans a directory without deleting it.
	 *
	 * @param directory
	 *            directory to clean, must not be <code>null</code>
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws IOException
	 *             in case cleaning is unsuccessful
	 */
	private static void cleanDirectoryOnExit(File directory) throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
		}

		IOException exception = null;
		for (File file : files) {
			try {
				forceDeleteOnExit(file);
			} catch (IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			throw exception;
		}
	}

	/**
	 * Makes a directory, including any necessary but nonexistent parent
	 * directories. If a file already exists with specified name but it is not a
	 * directory then an IOException is thrown. If the directory cannot be
	 * created (or does not already exist) then an IOException is thrown.
	 *
	 * @param directory
	 *            directory to create, must not be <code>null</code>
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws IOException
	 *             if the directory cannot be created or the file already exists
	 *             but is not a directory
	 */
	public static void forceMkdir(File directory) throws IOException {
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				String message = "File " + directory + " exists and is "
						+ "not a directory. Unable to create directory.";
				throw new IOException(message);
			}
		} else {
			if (!directory.mkdirs()) {
				// Double-check that some other thread or process hasn't made
				// the directory in the background
				if (!directory.isDirectory()) {
					String message = "Unable to create directory " + directory;
					throw new IOException(message);
				}
			}
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns the size of the specified file or directory. If the provided
	 * {@link File} is a regular file, then the file's length is returned. If
	 * the argument is a directory, then the size of the directory is calculated
	 * recursively. If a directory or subdirectory is security restricted, its
	 * size will not be included.
	 * 
	 * @param file
	 *            the regular file or directory to return the size of (must not
	 *            be <code>null</code>).
	 * 
	 * @return the length of the file, or recursive size of the directory,
	 *         provided (in bytes).
	 * 
	 * @throws NullPointerException
	 *             if the file is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the file does not exist.
	 * 
	 * @since 2.0
	 */
	public static long sizeOf(File file) {

		if (!file.exists()) {
			String message = file + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (file.isDirectory()) {
			return sizeOfDirectory(file);
		} else {
			return file.length();
		}

	}

	/**
	 * Counts the size of a directory recursively (sum of the length of all
	 * files).
	 *
	 * @param directory
	 *            directory to inspect, must not be <code>null</code>
	 * @return size of directory in bytes, 0 if directory is security restricted
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 */
	public static long sizeOfDirectory(File directory) {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		long size = 0;

		File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			return 0L;
		}
		for (File file : files) {
			size += sizeOf(file);
		}

		return size;
	}

	// -----------------------------------------------------------------------
	/**
	 * Tests if the specified <code>File</code> is newer than the reference
	 * <code>File</code>.
	 *
	 * @param file
	 *            the <code>File</code> of which the modification date must be
	 *            compared, must not be <code>null</code>
	 * @param reference
	 *            the <code>File</code> of which the modification date is used,
	 *            must not be <code>null</code>
	 * @return true if the <code>File</code> exists and has been modified more
	 *         recently than the reference <code>File</code>
	 * @throws IllegalArgumentException
	 *             if the file is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the reference file is <code>null</code> or doesn't exist
	 */
	public static boolean isFileNewer(File file, File reference) {
		if (reference == null) {
			throw new IllegalArgumentException("No specified reference file");
		}
		if (!reference.exists()) {
			throw new IllegalArgumentException("The reference file '" + reference + "' doesn't exist");
		}
		return isFileNewer(file, reference.lastModified());
	}

	/**
	 * Tests if the specified <code>File</code> is newer than the specified
	 * <code>Date</code>.
	 * 
	 * @param file
	 *            the <code>File</code> of which the modification date must be
	 *            compared, must not be <code>null</code>
	 * @param date
	 *            the date reference, must not be <code>null</code>
	 * @return true if the <code>File</code> exists and has been modified after
	 *         the given <code>Date</code>.
	 * @throws IllegalArgumentException
	 *             if the file is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 */
	public static boolean isFileNewer(File file, Date date) {
		if (date == null) {
			throw new IllegalArgumentException("No specified date");
		}
		return isFileNewer(file, date.getTime());
	}

	/**
	 * Tests if the specified <code>File</code> is newer than the specified time
	 * reference.
	 *
	 * @param file
	 *            the <code>File</code> of which the modification date must be
	 *            compared, must not be <code>null</code>
	 * @param timeMillis
	 *            the time reference measured in milliseconds since the epoch
	 *            (00:00:00 GMT, January 1, 1970)
	 * @return true if the <code>File</code> exists and has been modified after
	 *         the given time reference.
	 * @throws IllegalArgumentException
	 *             if the file is <code>null</code>
	 */
	public static boolean isFileNewer(File file, long timeMillis) {
		if (file == null) {
			throw new IllegalArgumentException("No specified file");
		}
		if (!file.exists()) {
			return false;
		}
		return file.lastModified() > timeMillis;
	}

	// -----------------------------------------------------------------------
	/**
	 * Tests if the specified <code>File</code> is older than the reference
	 * <code>File</code>.
	 *
	 * @param file
	 *            the <code>File</code> of which the modification date must be
	 *            compared, must not be <code>null</code>
	 * @param reference
	 *            the <code>File</code> of which the modification date is used,
	 *            must not be <code>null</code>
	 * @return true if the <code>File</code> exists and has been modified before
	 *         the reference <code>File</code>
	 * @throws IllegalArgumentException
	 *             if the file is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the reference file is <code>null</code> or doesn't exist
	 */
	public static boolean isFileOlder(File file, File reference) {
		if (reference == null) {
			throw new IllegalArgumentException("No specified reference file");
		}
		if (!reference.exists()) {
			throw new IllegalArgumentException("The reference file '" + reference + "' doesn't exist");
		}
		return isFileOlder(file, reference.lastModified());
	}

	/**
	 * Tests if the specified <code>File</code> is older than the specified
	 * <code>Date</code>.
	 * 
	 * @param file
	 *            the <code>File</code> of which the modification date must be
	 *            compared, must not be <code>null</code>
	 * @param date
	 *            the date reference, must not be <code>null</code>
	 * @return true if the <code>File</code> exists and has been modified before
	 *         the given <code>Date</code>.
	 * @throws IllegalArgumentException
	 *             if the file is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the date is <code>null</code>
	 */
	public static boolean isFileOlder(File file, Date date) {
		if (date == null) {
			throw new IllegalArgumentException("No specified date");
		}
		return isFileOlder(file, date.getTime());
	}

	/**
	 * Tests if the specified <code>File</code> is older than the specified time
	 * reference.
	 *
	 * @param file
	 *            the <code>File</code> of which the modification date must be
	 *            compared, must not be <code>null</code>
	 * @param timeMillis
	 *            the time reference measured in milliseconds since the epoch
	 *            (00:00:00 GMT, January 1, 1970)
	 * @return true if the <code>File</code> exists and has been modified before
	 *         the given time reference.
	 * @throws IllegalArgumentException
	 *             if the file is <code>null</code>
	 */
	public static boolean isFileOlder(File file, long timeMillis) {
		if (file == null) {
			throw new IllegalArgumentException("No specified file");
		}
		if (!file.exists()) {
			return false;
		}
		return file.lastModified() < timeMillis;
	}

	// -----------------------------------------------------------------------
	/**
	 * Computes the checksum of a file using the CRC32 checksum routine. The
	 * value of the checksum is returned.
	 *
	 * @param file
	 *            the file to checksum, must not be <code>null</code>
	 * @return the checksum value
	 * @throws NullPointerException
	 *             if the file or checksum is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the file is a directory
	 * @throws IOException
	 *             if an IO error occurs reading the file
	 * @since 1.3
	 */
	public static long checksumCRC32(File file) throws IOException {
		CRC32 crc = new CRC32();
		checksum(file, crc);
		return crc.getValue();
	}

	/**
	 * Computes the checksum of a file using the specified checksum object.
	 * Multiple files may be checked using one <code>Checksum</code> instance if
	 * desired simply by reusing the same checksum object. For example:
	 * 
	 * <pre>
	 * long csum = FileUtils.checksum(file, new CRC32()).getValue();
	 * </pre>
	 *
	 * @param file
	 *            the file to checksum, must not be <code>null</code>
	 * @param checksum
	 *            the checksum object to be used, must not be <code>null</code>
	 * @return the checksum specified, updated with the content of the file
	 * @throws NullPointerException
	 *             if the file or checksum is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the file is a directory
	 * @throws IOException
	 *             if an IO error occurs reading the file
	 * @since 1.3
	 */
	public static Checksum checksum(File file, Checksum checksum) throws IOException {
		if (file.isDirectory()) {
			throw new IllegalArgumentException("Checksums can't be computed on directories");
		}
		InputStream in = null;
		try {
			in = new CheckedInputStream(new FileInputStream(file), checksum);
			IOUtils.copy(in, new NullOutputStream());
		} finally {
			IOUtils.closeQuietly(in);
		}
		return checksum;
	}

	/**
	 * Moves a directory.
	 * <p>
	 * When the destination directory is on another file system, do a "copy and
	 * delete".
	 *
	 * @param srcDir
	 *            the directory to be moved
	 * @param destDir
	 *            the destination directory
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws FileExistsException
	 *             if the destination directory exists
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs moving the file
	 * @since 1.4
	 */
	public static void moveDirectory(File srcDir, File destDir, long bufSize) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (!srcDir.exists()) {
			throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
		}
		if (!srcDir.isDirectory()) {
			throw new IOException("Source '" + srcDir + "' is not a directory");
		}
		if (destDir.exists()) {
			throw new AlreadyExistsException("Destination '" + destDir + "' already exists");
		}
		boolean rename = srcDir.renameTo(destDir);
		if (!rename) {
			if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
				throw new IOException("Cannot move directory: " + srcDir + " to a subdirectory of itself: " + destDir);
			}
			copyDirectory(srcDir, destDir, bufSize);
			deleteDirectory(srcDir);
			if (srcDir.exists()) {
				throw new IOException(
						"Failed to delete original directory '" + srcDir + "' after copy to '" + destDir + "'");
			}
		}
	}

	/**
	 * Moves a directory to another directory.
	 *
	 * @param src
	 *            the file to be moved
	 * @param destDir
	 *            the destination file
	 * @param createDestDir
	 *            If <code>true</code> create the destination directory,
	 *            otherwise if <code>false</code> throw an IOException
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws FileExistsException
	 *             if the directory exists in the destination directory
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs moving the file
	 * @since 1.4
	 */
	public static void moveDirectoryToDirectory(File src, File destDir, boolean createDestDir, long bufSize)
			throws IOException {
		if (src == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination directory must not be null");
		}
		if (!destDir.exists() && createDestDir) {
			destDir.mkdirs();
		}
		if (!destDir.exists()) {
			throw new FileNotFoundException(
					"Destination directory '" + destDir + "' does not exist [createDestDir=" + createDestDir + "]");
		}
		if (!destDir.isDirectory()) {
			throw new IOException("Destination '" + destDir + "' is not a directory");
		}
		moveDirectory(src, new File(destDir, src.getName()), bufSize);
	}

	/**
	 * Moves a file.
	 * <p>
	 * When the destination file is on another file system, do a "copy and
	 * delete".
	 *
	 * @param srcFile
	 *            the file to be moved
	 * @param destFile
	 *            the destination file
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws FileExistsException
	 *             if the destination file exists
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs moving the file
	 * @since 1.4
	 */
	public static void moveFile(File srcFile, File destFile, long bufSize) throws IOException {
		if (srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destFile == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (!srcFile.exists()) {
			throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
		}
		if (srcFile.isDirectory()) {
			throw new IOException("Source '" + srcFile + "' is a directory");
		}
		if (destFile.exists()) {
			throw new AlreadyExistsException("Destination '" + destFile + "' already exists");
		}
		if (destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile + "' is a directory");
		}
		boolean rename = srcFile.renameTo(destFile);
		if (!rename) {
			copyFile(srcFile, destFile, bufSize);
			if (!srcFile.delete()) {
				FileUtils.deleteQuietly(destFile);
				throw new IOException(
						"Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'");
			}
		}
	}

	/**
	 * Moves a file to a directory.
	 *
	 * @param srcFile
	 *            the file to be moved
	 * @param destDir
	 *            the destination file
	 * @param createDestDir
	 *            If <code>true</code> create the destination directory,
	 *            otherwise if <code>false</code> throw an IOException
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws FileExistsException
	 *             if the destination file exists
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs moving the file
	 * @since 1.4
	 */
	public static void moveFileToDirectory(File srcFile, File destDir, boolean createDestDir, long bufSize)
			throws IOException {
		if (srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination directory must not be null");
		}
		if (!destDir.exists() && createDestDir) {
			destDir.mkdirs();
		}
		if (!destDir.exists()) {
			throw new FileNotFoundException(
					"Destination directory '" + destDir + "' does not exist [createDestDir=" + createDestDir + "]");
		}
		if (!destDir.isDirectory()) {
			throw new IOException("Destination '" + destDir + "' is not a directory");
		}
		moveFile(srcFile, new File(destDir, srcFile.getName()), bufSize);
	}

	/**
	 * Moves a file or directory to the destination directory.
	 * <p>
	 * When the destination is on another file system, do a "copy and delete".
	 *
	 * @param src
	 *            the file or directory to be moved
	 * @param destDir
	 *            the destination directory
	 * @param createDestDir
	 *            If <code>true</code> create the destination directory,
	 *            otherwise if <code>false</code> throw an IOException
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws FileExistsException
	 *             if the directory or file exists in the destination directory
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs moving the file
	 * @since 1.4
	 */
	public static void moveToDirectory(File src, File destDir, boolean createDestDir, long bufSize) throws IOException {
		if (src == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (!src.exists()) {
			throw new FileNotFoundException("Source '" + src + "' does not exist");
		}
		if (src.isDirectory()) {
			moveDirectoryToDirectory(src, destDir, createDestDir, bufSize);
		} else {
			moveFileToDirectory(src, destDir, createDestDir, bufSize);
		}
	}

	/**
	 * Determines whether the specified file is a Symbolic Link rather than an
	 * actual file.
	 * <p>
	 * Will not return true if there is a Symbolic Link anywhere in the path,
	 * only if the specific file is.
	 * <p>
	 * <b>Note:</b> the current implementation always returns {@code false} if
	 * the system is detected as Windows using
	 * {@link FilenameUtils#isSystemWindows()}
	 * 
	 * @param file
	 *            the file to check
	 * @return true if the file is a Symbolic Link
	 * @throws IOException
	 *             if an IO error occurs while checking the file
	 * @since 2.0
	 */
	public static boolean isSymlink(File file) throws IOException {
		if (file == null) {
			throw new NullPointerException("File must not be null");
		}
		if (FilenameUtils.isSystemWindows()) {
			return false;
		}
		File fileInCanonicalDir = null;
		if (file.getParent() == null) {
			fileInCanonicalDir = file;
		} else {
			File canonicalDir = file.getParentFile().getCanonicalFile();
			fileInCanonicalDir = new File(canonicalDir, file.getName());
		}

		if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
			return false;
		} else {
			return true;
		}
	}

	// 自己的
	public static void toFile(String pathName, InputStream is) {
		OutputStream os = null;
		try {
			File file = new File(pathName);
			if (file.exists()) {
				file.delete();
			}

			os = new FileOutputStream(file);
			IOUtils.write(os, is, 10 * 1024 * 1024, -1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static String pathToName(String path) {
		int index = path.indexOf("/");
		if (index != -1) {
			index++;
			return path.substring(index);
		}
		return path;
	}

	public static void downUrlFile(String url, String pathName) {
		try {
			URL connUrl = new URL(url);
			URLConnection conn = connUrl.openConnection();
			InputStream is = conn.getInputStream();
			toFile(pathName, is);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean unZip(String zipPath, String toPath) {
		try {
			File zipF = new File(zipPath);
			if (!zipF.exists()) {
				return false;
			}

			File to = new File(toPath);
			if (!to.exists()) {
				to.mkdirs();
			}

			toPath = to.getPath() + File.separator;
			ZipFile zipFile = new ZipFile(zipPath);
			Enumeration<? extends ZipEntry> ens = zipFile.entries();
			ZipEntry zipEntry = null;
			while (ens.hasMoreElements()) {
				zipEntry = ens.nextElement();
				String dirName = zipEntry.getName();
				if (zipEntry.isDirectory()) {
					// dirName = dirName.substring(0, dirName.length() - 1);
					File f = new File(toPath + dirName);
					f.mkdirs();
				} else {
					String strFilePath = toPath + dirName;
					File f = new File(strFilePath);
					/*
					 * if(!f.exists()){ String[] arrFolderName =
					 * dirName.split("/"); StringBuilder sb = new
					 * StringBuilder(); sb.append(toPath + File.separator);
					 * for(int i=0; i<(arrFolderName.length - 1); i++){
					 * sb.append(arrFolderName[i]); sb.append(File.separator); }
					 * 
					 * File tempDir = new File(sb.toString()); tempDir.mkdir();
					 * }
					 */

					f.createNewFile();

					InputStream is = zipFile.getInputStream(zipEntry);
					FileOutputStream fos = new FileOutputStream(f);
					int len;
					byte[] by = new byte[1024];
					while ((len = is.read(by)) != -1) {
						fos.write(by, 0, len);
					}
					fos.flush();
					fos.close();
					is.close();
				}
			}
			zipFile.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 将文件分割符换成与当前操作系统一致
	 * 
	 * @param path
	 * @return
	 */
	public static String replaceSeparator(String path) {
		if (path == null) {
			return path;
		}

		if (File.separator.equals("/")) {
			return path.replaceAll("\\\\", "/");
		} else {
			return path.replaceAll("/", "\\\\");
		}
	}

	/**
	 * 查找文件
	 * 
	 * @param fileName
	 *            要查找的文件名 不包含目录
	 * @param rootPath
	 *            父目录
	 * @return
	 */
	public static String searchFile(String fileName, String rootPath) {
		File file = new File(rootPath);
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				String name = searchFile(fileName, f.getPath());
				if (name != null) {
					return name;
				}
			} else {
				if (f.getName().equals(fileName)) {
					return f.getPath();
				}
			}
		}
		return null;
	}

	public static List<String> getFileList(String rootPath) {
		rootPath = replaceSeparator(rootPath);
		List<String> list = new ArrayList<String>();

		File rootFile = new File(rootPath);
		if (rootFile.exists()) {
			File[] files = rootFile.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					list.add(file.getPath());
				} else {
					list.addAll(getFileList(file.getPath()));
				}
			}
		}
		return list;
	}

	/**
	 * 在目录下递归搜索指定目录
	 * 
	 * @param rootPath
	 * @param directoryName
	 * @return
	 */
	public static File searchDirectory(String rootPath, String directoryName) {
		File rootFile = new File(rootPath);
		if (rootFile.exists()) {
			File[] files = rootFile.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					if (file.getName().equals(directoryName)) {
						return file;
					}

					File f = searchDirectory(file.getPath(), directoryName);
					if (f != null) {
						return f;
					}
				}
			}
		}
		return null;
	}

	public static String toBase64(String filePath, StringBuilder data) {
		FileInputStream fis = null;
		byte[] b = new byte[1024];
		int len = 0;
		if (data == null) {
			data = new StringBuilder();
		}

		try {
			fis = new FileInputStream(filePath);
			while ((len = fis.read(b)) != -1) {
				data.append(Base64.encode(Arrays.copyOf(b, len)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		return data.toString();
	}

	public static String getFileSuffix(String filePath) {
		int sufIndex = filePath.lastIndexOf(".");
		if (sufIndex != -1) {
			return filePath.substring(sufIndex + 1);
		}
		return null;
	}

	public static boolean copyFile(String oldFile, String newFile) {
		Path oldP = Paths.get(oldFile);
		Path newP = Paths.get(newFile);
		if (oldP.toFile().exists()) {
			try {
				Files.copy(oldP, newP, StandardCopyOption.REPLACE_EXISTING);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(source);
			fos = new FileOutputStream(dest);
			inputChannel = fis.getChannel();
			outputChannel = fos.getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			fis.close();
			fos.close();
			inputChannel.close();
			outputChannel.close();
		}
	}

	public static String readerFileContent(File file, String charsetName) {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fileInputStream, Charset.forName(charsetName));
			return IOUtils.read(isr, 256, 0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static <T> T readObject(File file) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return SerializerUtils.DEFAULT_SERIALIZER.deserialize(fis);
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	public static void writeObject(File file, Object obj) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			SerializerUtils.DEFAULT_SERIALIZER.serialize(fos, obj);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	public static List<String> getFileContentLineList(File file, String charsetName) {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis, charsetName);
			br = new BufferedReader(isr);
			return IOUtils.readLineList(br, -1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(br, isr, fis);
		}
	}

	public static void writeFileContent(String filePath, String content, String charsetName) {
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		writeFileContent(file, content, charsetName);
	}

	public static void writeFileContent(File file, String content, String charsetName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(content.getBytes(charsetName));
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.close(fos);
		}
	}
}
