package scw.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import scw.core.Assert;
import scw.lang.ParameterException;
import scw.util.FormatUtils;
import scw.util.KeyValuePair;

public final class StringUtils {
	private static final String FOLDER_SEPARATOR = "/";

	private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

	private static final String TOP_PATH = "..";

	private static final String CURRENT_PATH = ".";

	private static final char EXTENSION_SEPARATOR = '.';

	private static final Set<String> BOOLEANS;
	static {
		BOOLEANS = new HashSet<String>();
		BOOLEANS.add("1");
		BOOLEANS.add("y");
		BOOLEANS.add("t");
		BOOLEANS.add("ok");
		BOOLEANS.add("yes");
		BOOLEANS.add("right");
		BOOLEANS.add("true");
		BOOLEANS.add("success");
		BOOLEANS.add("successful");
	}

	private StringUtils() {
	};

	/**
	 * spring拷贝过来的
	 */

	// ---------------------------------------------------------------------
	// General convenience methods for working with Strings
	// ---------------------------------------------------------------------

	/**
	 * Check that the given CharSequence is neither {@code null} nor of length
	 * 0. Note: Will return {@code true} for a CharSequence that purely consists
	 * of whitespace.
	 * <p>
	 * 
	 * <pre>
	 * StringUtils.hasLength(null) = false
	 * StringUtils.hasLength("") = false
	 * StringUtils.hasLength(" ") = true
	 * StringUtils.hasLength("Hello") = true
	 * </pre>
	 * 
	 * @param str
	 *            the CharSequence to check (may be {@code null})
	 * @return {@code true} if the CharSequence is not null and has length
	 * @see #hasText(String)
	 */
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}

	/**
	 * Check that the given String is neither {@code null} nor of length 0.
	 * Note: Will return {@code true} for a String that purely consists of
	 * whitespace.
	 * 
	 * @param str
	 *            the String to check (may be {@code null})
	 * @return {@code true} if the String is not null and has length
	 * @see #hasLength(CharSequence)
	 */
	public static boolean hasLength(String str) {
		return hasLength((CharSequence) str);
	}

	/**
	 * Check whether the given CharSequence has actual text. More specifically,
	 * returns {@code true} if the string not {@code null}, its length is
	 * greater than 0, and it contains at least one non-whitespace character.
	 * <p>
	 * 
	 * <pre>
	 * StringUtils.hasText(null) = false
	 * StringUtils.hasText("") = false
	 * StringUtils.hasText(" ") = false
	 * StringUtils.hasText("12345") = true
	 * StringUtils.hasText(" 12345 ") = true
	 * </pre>
	 * 
	 * @param str
	 *            the CharSequence to check (may be {@code null})
	 * @return {@code true} if the CharSequence is not {@code null}, its length
	 *         is greater than 0, and it does not contain whitespace only
	 * @see Character#isWhitespace
	 */
	public static boolean hasText(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given String has actual text. More specifically,
	 * returns {@code true} if the string not {@code null}, its length is
	 * greater than 0, and it contains at least one non-whitespace character.
	 * 
	 * @param str
	 *            the String to check (may be {@code null})
	 * @return {@code true} if the String is not {@code null}, its length is
	 *         greater than 0, and it does not contain whitespace only
	 * @see #hasText(CharSequence)
	 */
	public static boolean hasText(String str) {
		return hasText((CharSequence) str);
	}
	
	public static boolean hasText(String text, int fromIndex, int endIndex){
		if(fromIndex == endIndex){
			return false;
		}
		
		if (!hasLength(text)) {
			return false;
		}
		
		for (int i = fromIndex, end = Math.min(text.length(), endIndex); i < end; i++) {
			if (!Character.isWhitespace(text.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given CharSequence contains any whitespace characters.
	 * 
	 * @param str
	 *            the CharSequence to check (may be {@code null})
	 * @return {@code true} if the CharSequence is not empty and contains at
	 *         least 1 whitespace character
	 * @see Character#isWhitespace
	 */
	public static boolean containsWhitespace(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given String contains any whitespace characters.
	 * 
	 * @param str
	 *            the String to check (may be {@code null})
	 * @return {@code true} if the String is not empty and contains at least 1
	 *         whitespace character
	 * @see #containsWhitespace(CharSequence)
	 */
	public static boolean containsWhitespace(String str) {
		return containsWhitespace((CharSequence) str);
	}

	/**
	 * Trim leading and trailing whitespace from the given String.
	 * 
	 * @param str
	 *            the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Trim <i>all</i> whitespace from the given String: leading, trailing, and
	 * inbetween characters.
	 * 
	 * @param str
	 *            the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimAllWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		int index = 0;
		while (sb.length() > index) {
			if (Character.isWhitespace(sb.charAt(index))) {
				sb.deleteCharAt(index);
			} else {
				index++;
			}
		}
		return sb.toString();
	}

	/**
	 * Trim leading whitespace from the given String.
	 * 
	 * @param str
	 *            the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimLeadingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	/**
	 * Trim trailing whitespace from the given String.
	 * 
	 * @param str
	 *            the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimTrailingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Trim all occurences of the supplied leading character from the given
	 * String.
	 * 
	 * @param str
	 *            the String to check
	 * @param leadingCharacter
	 *            the leading character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimLeadingCharacter(String str, char leadingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	/**
	 * Trim all occurences of the supplied trailing character from the given
	 * String.
	 * 
	 * @param str
	 *            the String to check
	 * @param trailingCharacter
	 *            the trailing character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimTrailingCharacter(String str, char trailingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Test if the given String starts with the specified prefix, ignoring
	 * upper/lower case.
	 * 
	 * @param str
	 *            the String to check
	 * @param prefix
	 *            the prefix to look for
	 * @see java.lang.String#startsWith
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		if (str.startsWith(prefix)) {
			return true;
		}
		if (str.length() < prefix.length()) {
			return false;
		}
		String lcStr = str.substring(0, prefix.length()).toLowerCase();
		String lcPrefix = prefix.toLowerCase();
		return lcStr.equals(lcPrefix);
	}

	/**
	 * Test if the given String ends with the specified suffix, ignoring
	 * upper/lower case.
	 * 
	 * @param str
	 *            the String to check
	 * @param suffix
	 *            the suffix to look for
	 * @see java.lang.String#endsWith
	 */
	public static boolean endsWithIgnoreCase(String str, String suffix) {
		if (str == null || suffix == null) {
			return false;
		}
		if (str.endsWith(suffix)) {
			return true;
		}
		if (str.length() < suffix.length()) {
			return false;
		}

		String lcStr = str.substring(str.length() - suffix.length()).toLowerCase();
		String lcSuffix = suffix.toLowerCase();
		return lcStr.equals(lcSuffix);
	}

	/**
	 * Test whether the given string matches the given substring at the given
	 * index.
	 * 
	 * @param str
	 *            the original string (or StringBuilder)
	 * @param index
	 *            the index in the original string to start matching against
	 * @param substring
	 *            the substring to match at the given index
	 */
	public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
		for (int j = 0; j < substring.length(); j++) {
			int i = index + j;
			if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Count the occurrences of the substring in string s.
	 * 
	 * @param str
	 *            string to search in. Return 0 if this is null.
	 * @param sub
	 *            string to search for. Return 0 if this is null.
	 */
	public static int countOccurrencesOf(String str, String sub) {
		if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
			return 0;
		}
		int count = 0;
		int pos = 0;
		int idx;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}

	/**
	 * Replace all occurences of a substring within a string with another
	 * string.
	 * 
	 * @param inString
	 *            String to examine
	 * @param oldPattern
	 *            String to replace
	 * @param newPattern
	 *            String to insert
	 * @return a String with the replacements
	 */
	public static String replace(String inString, String oldPattern, String newPattern) {
		if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0; // our position in the old string
		int index = inString.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(inString.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}
		sb.append(inString.substring(pos));
		// remember to append any characters to the right of a match
		return sb.toString();
	}

	/**
	 * Delete all occurrences of the given substring.
	 * 
	 * @param inString
	 *            the original String
	 * @param pattern
	 *            the pattern to delete all occurrences of
	 * @return the resulting String
	 */
	public static String delete(String inString, String pattern) {
		return replace(inString, pattern, "");
	}

	/**
	 * Delete any character in a given String.
	 * 
	 * @param inString
	 *            the original String
	 * @param charsToDelete
	 *            a set of characters to delete. E.g. "az\n" will delete 'a's,
	 *            'z's and new lines.
	 * @return the resulting String
	 */
	public static String deleteAny(String inString, String charsToDelete) {
		if (!hasLength(inString) || !hasLength(charsToDelete)) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inString.length(); i++) {
			char c = inString.charAt(i);
			if (charsToDelete.indexOf(c) == -1) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	// ---------------------------------------------------------------------
	// Convenience methods for working with formatted Strings
	// ---------------------------------------------------------------------

	/**
	 * Quote the given String with single quotes.
	 * 
	 * @param str
	 *            the input String (e.g. "myString")
	 * @return the quoted String (e.g. "'myString'"), or {@code null} if the
	 *         input was {@code null}
	 */
	public static String quote(String str) {
		return (str != null ? "'" + str + "'" : null);
	}

	/**
	 * Turn the given Object into a String with single quotes if it is a String;
	 * keeping the Object as-is else.
	 * 
	 * @param obj
	 *            the input Object (e.g. "myString")
	 * @return the quoted String (e.g. "'myString'"), or the input object as-is
	 *         if not a String
	 */
	public static Object quoteIfString(Object obj) {
		return (obj instanceof String ? quote((String) obj) : obj);
	}

	/**
	 * Unqualify a string qualified by a '.' dot character. For example,
	 * "this.name.is.qualified", returns "qualified".
	 * 
	 * @param qualifiedName
	 *            the qualified name
	 */
	public static String unqualify(String qualifiedName) {
		return unqualify(qualifiedName, '.');
	}

	/**
	 * Unqualify a string qualified by a separator character. For example,
	 * "this:name:is:qualified" returns "qualified" if using a ':' separator.
	 * 
	 * @param qualifiedName
	 *            the qualified name
	 * @param separator
	 *            the separator
	 */
	public static String unqualify(String qualifiedName, char separator) {
		return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
	}

	/**
	 * 合并多个路径<br/>
	 * 注意：该方法使用原始的字符串拼接方式(a + b)，不推荐被动态的多次调用，否则可能成为性能瓶颈
	 * 
	 * @param paths
	 * @return
	 */
	public static String mergePath(String... paths) {
		if (ArrayUtils.isEmpty(paths)) {
			return null;
		}

		if (paths.length == 1) {
			String path = paths[0];
			return path == null ? null : replacePath(path);
		}

		String p = addPath(paths[0], paths[1]);
		for (int i = 2; i < paths.length; i++) {
			p = addPath(p, paths[i]);
		}
		return p;
	}

	private static String replacePath(String path) {
		return path.replaceAll("\\\\", FOLDER_SEPARATOR);
	}

	private static String addPath(String path1, String path2) {
		String p1 = path1 == null ? "" : path1;
		String p2 = path2 == null ? "" : path2;
		p1 = replacePath(p1);
		p2 = replacePath(p2);

		if (!StringUtils.isEmpty(p2)) {
			if (!p1.endsWith(FOLDER_SEPARATOR)) {
				p1 = p1 + FOLDER_SEPARATOR;
			}

			if (p2.startsWith(FOLDER_SEPARATOR)) {
				p2 = p2.substring(1);
			}
		}
		return p1 + p2;
	}

	/**
	 * Extract the filename from the given path, e.g. "mypath/myfile.txt" ->
	 * "myfile.txt".
	 * 
	 * @param path
	 *            the file path (may be {@code null})
	 * @return the extracted filename, or {@code null} if none
	 */
	public static String getFilename(String path) {
		if (path == null) {
			return null;
		}
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
	}

	/**
	 * Extract the filename extension from the given path, e.g.
	 * "mypath/myfile.txt" -> "txt".
	 * 
	 * @param path
	 *            the file path (may be {@code null})
	 * @return the extracted filename extension, or {@code null} if none
	 */
	public static String getFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return null;
		}
		int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (folderIndex > extIndex) {
			return null;
		}
		return path.substring(extIndex + 1);
	}

	/**
	 * Strip the filename extension from the given path, e.g.
	 * "mypath/myfile.txt" -> "mypath/myfile".
	 * 
	 * @param path
	 *            the file path (may be {@code null})
	 * @return the path with stripped filename extension, or {@code null} if
	 *         none
	 */
	public static String stripFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return path;
		}
		int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (folderIndex > extIndex) {
			return path;
		}
		return path.substring(0, extIndex);
	}

	/**
	 * Apply the given relative path to the given path, assuming standard Java
	 * folder separation (i.e. "/" separators).
	 * 
	 * @param path
	 *            the path to start from (usually a full file path)
	 * @param relativePath
	 *            the relative path to apply (relative to the full file path
	 *            above)
	 * @return the full file path that results from applying the relative path
	 */
	public static String applyRelativePath(String path, String relativePath) {
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (separatorIndex != -1) {
			String newPath = path.substring(0, separatorIndex);
			if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
				newPath += FOLDER_SEPARATOR;
			}
			return newPath + relativePath;
		} else {
			return relativePath;
		}
	}

	/**
	 * Normalize the path by suppressing sequences like "path/.." and inner
	 * simple dots.
	 * <p>
	 * The result is convenient for path comparison. For other uses, notice that
	 * Windows separators ("\") are replaced by simple slashes.
	 * 
	 * @param path
	 *            the original path
	 * @return the normalized path
	 */
	public static String cleanPath(String path) {
		if (path == null) {
			return null;
		}
		String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

		// Strip prefix from path to analyze, to not treat it as part of the
		// first path element. This is necessary to correctly parse paths like
		// "file:core/../core/io/Resource.class", where the ".." should just
		// strip the first "core" directory while keeping the "file:" prefix.
		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			pathToUse = pathToUse.substring(prefixIndex + 1);
		}
		if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
			prefix = prefix + FOLDER_SEPARATOR;
			pathToUse = pathToUse.substring(1);
		}

		String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
		List<String> pathElements = new LinkedList<String>();
		int tops = 0;

		for (int i = pathArray.length - 1; i >= 0; i--) {
			String element = pathArray[i];
			if (CURRENT_PATH.equals(element)) {
				// Points to current directory - drop it.
			} else if (TOP_PATH.equals(element)) {
				// Registering top path found.
				tops++;
			} else {
				if (tops > 0) {
					// Merging path element with element corresponding to top
					// path.
					tops--;
				} else {
					// Normal path element found.
					pathElements.add(0, element);
				}
			}
		}

		// Remaining top paths need to be retained.
		for (int i = 0; i < tops; i++) {
			pathElements.add(0, TOP_PATH);
		}

		return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
	}

	/**
	 * Compare two paths after normalization of them.
	 * 
	 * @param path1
	 *            first path for comparison
	 * @param path2
	 *            second path for comparison
	 * @return whether the two paths are equivalent after normalization
	 */
	public static boolean pathEquals(String path1, String path2) {
		return cleanPath(path1).equals(cleanPath(path2));
	}

	/**
	 * Parse the given {@code localeString} value into a {@link Locale}.
	 * <p>
	 * This is the inverse operation of {@link Locale#toString Locale's
	 * toString}.
	 * 
	 * @param localeString
	 *            the locale string, following {@code Locale's}
	 *            {@code toString()} format ("en", "en_UK", etc); also accepts
	 *            spaces as separators, as an alternative to underscores
	 * @return a corresponding {@code Locale} instance
	 */
	public static Locale parseLocaleString(String localeString) {
		String[] parts = tokenizeToStringArray(localeString, "_ ", false, false);
		String language = (parts.length > 0 ? parts[0] : "");
		String country = (parts.length > 1 ? parts[1] : "");
		validateLocalePart(language);
		validateLocalePart(country);
		String variant = "";
		if (parts.length >= 2) {
			// There is definitely a variant, and it is everything after the
			// country
			// code sans the separator between the country code and the variant.
			int endIndexOfCountryCode = localeString.lastIndexOf(country) + country.length();
			// Strip off any leading '_' and whitespace, what's left is the
			// variant.
			variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
			if (variant.startsWith("_")) {
				variant = trimLeadingCharacter(variant, '_');
			}
		}
		return (language.length() > 0 ? new Locale(language, country, variant) : null);
	}

	private static void validateLocalePart(String localePart) {
		for (int i = 0; i < localePart.length(); i++) {
			char ch = localePart.charAt(i);
			if (ch != '_' && ch != ' ' && !Character.isLetterOrDigit(ch)) {
				throw new IllegalArgumentException("Locale part \"" + localePart + "\" contains invalid characters");
			}
		}
	}

	/**
	 * Determine the RFC 3066 compliant language tag, as used for the HTTP
	 * "Accept-Language" header.
	 * 
	 * @param locale
	 *            the Locale to transform to a language tag
	 * @return the RFC 3066 compliant language tag as String
	 */
	public static String toLanguageTag(Locale locale) {
		return locale.getLanguage() + (hasText(locale.getCountry()) ? "-" + locale.getCountry() : "");
	}

	// ---------------------------------------------------------------------
	// Convenience methods for working with String arrays
	// ---------------------------------------------------------------------

	/**
	 * Append the given String to the given String array, returning a new array
	 * consisting of the input array contents plus the given String.
	 * 
	 * @param array
	 *            the array to append to (can be {@code null})
	 * @param str
	 *            the String to append
	 * @return the new array (never {@code null})
	 */
	public static String[] addStringToArray(String[] array, String str) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[] { str };
		}
		String[] newArr = new String[array.length + 1];
		System.arraycopy(array, 0, newArr, 0, array.length);
		newArr[array.length] = str;
		return newArr;
	}

	/**
	 * Concatenate the given String arrays into one, with overlapping array
	 * elements included twice.
	 * <p>
	 * The order of elements in the original arrays is preserved.
	 * 
	 * @param array1
	 *            the first array (can be {@code null})
	 * @param array2
	 *            the second array (can be {@code null})
	 * @return the new array ({@code null} if both given arrays were
	 *         {@code null})
	 */
	public static String[] concatenateStringArrays(String[] array1, String[] array2) {
		if (ObjectUtils.isEmpty(array1)) {
			return array2;
		}
		if (ObjectUtils.isEmpty(array2)) {
			return array1;
		}
		String[] newArr = new String[array1.length + array2.length];
		System.arraycopy(array1, 0, newArr, 0, array1.length);
		System.arraycopy(array2, 0, newArr, array1.length, array2.length);
		return newArr;
	}

	/**
	 * Merge the given String arrays into one, with overlapping array elements
	 * only included once.
	 * <p>
	 * The order of elements in the original arrays is preserved (with the
	 * exception of overlapping elements, which are only included on their first
	 * occurrence).
	 * 
	 * @param array1
	 *            the first array (can be {@code null})
	 * @param array2
	 *            the second array (can be {@code null})
	 * @return the new array ({@code null} if both given arrays were
	 *         {@code null})
	 */
	public static String[] mergeStringArrays(String[] array1, String[] array2) {
		if (ObjectUtils.isEmpty(array1)) {
			return array2;
		}
		if (ObjectUtils.isEmpty(array2)) {
			return array1;
		}
		List<String> result = new ArrayList<String>();
		result.addAll(Arrays.asList(array1));
		for (String str : array2) {
			if (!result.contains(str)) {
				result.add(str);
			}
		}
		return toStringArray(result);
	}

	/**
	 * Turn given source String array into sorted array.
	 * 
	 * @param array
	 *            the source array
	 * @return the sorted array (never {@code null})
	 */
	public static String[] sortStringArray(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[0];
		}
		Arrays.sort(array);
		return array;
	}

	/**
	 * Copy the given Collection into a String array. The Collection must
	 * contain String elements only.
	 * 
	 * @param collection
	 *            the Collection to copy
	 * @return the String array ({@code null} if the passed-in Collection was
	 *         {@code null})
	 */
	public static String[] toStringArray(Collection<String> collection) {
		if (collection == null) {
			return null;
		}
		return collection.toArray(new String[collection.size()]);
	}

	/**
	 * Copy the given Enumeration into a String array. The Enumeration must
	 * contain String elements only.
	 * 
	 * @param enumeration
	 *            the Enumeration to copy
	 * @return the String array ({@code null} if the passed-in Enumeration was
	 *         {@code null})
	 */
	public static String[] toStringArray(Enumeration<String> enumeration) {
		if (enumeration == null) {
			return null;
		}
		List<String> list = Collections.list(enumeration);
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Trim the elements of the given String array, calling
	 * {@code String.trim()} on each of them.
	 * 
	 * @param array
	 *            the original String array
	 * @return the resulting array (of the same size) with trimmed elements
	 */
	public static String[] trimArrayElements(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[0];
		}
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			String element = array[i];
			result[i] = (element != null ? element.trim() : null);
		}
		return result;
	}

	/**
	 * Remove duplicate Strings from the given array. Also sorts the array, as
	 * it uses a TreeSet.
	 * 
	 * @param array
	 *            the String array
	 * @return an array without duplicates, in natural sort order
	 */
	public static String[] removeDuplicateStrings(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return array;
		}
		Set<String> set = new TreeSet<String>();
		for (String element : array) {
			set.add(element);
		}
		return toStringArray(set);
	}

	/**
	 * Take an array Strings and split each element based on the given
	 * delimiter. A {@code Properties} instance is then generated, with the left
	 * of the delimiter providing the key, and the right of the delimiter
	 * providing the value.
	 * <p>
	 * Will trim both the key and value before adding them to the
	 * {@code Properties} instance.
	 * 
	 * @param array
	 *            the array to process
	 * @param delimiter
	 *            to split each element using (typically the equals symbol)
	 * @return a {@code Properties} instance representing the array contents, or
	 *         {@code null} if the array to process was null or empty
	 */
	public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter) {
		return splitArrayElementsIntoProperties(array, delimiter, null);
	}

	/**
	 * Take an array Strings and split each element based on the given
	 * delimiter. A {@code Properties} instance is then generated, with the left
	 * of the delimiter providing the key, and the right of the delimiter
	 * providing the value.
	 * <p>
	 * Will trim both the key and value before adding them to the
	 * {@code Properties} instance.
	 * 
	 * @param array
	 *            the array to process
	 * @param delimiter
	 *            to split each element using (typically the equals symbol)
	 * @param charsToDelete
	 *            one or more characters to remove from each element prior to
	 *            attempting the split operation (typically the quotation mark
	 *            symbol), or {@code null} if no removal should occur
	 * @return a {@code Properties} instance representing the array contents, or
	 *         {@code null} if the array to process was {@code null} or empty
	 */
	public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter, String charsToDelete) {

		if (ObjectUtils.isEmpty(array)) {
			return null;
		}
		Properties result = new Properties();
		for (String element : array) {
			if (charsToDelete != null) {
				element = deleteAny(element, charsToDelete);
			}
			String[] splittedElement = split(element, delimiter);
			if (splittedElement == null) {
				continue;
			}
			result.setProperty(splittedElement[0].trim(), splittedElement[1].trim());
		}
		return result;
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * Trims tokens and omits empty tokens.
	 * <p>
	 * The given delimiters string is supposed to consist of any number of
	 * delimiter characters. Each of those characters can be used to separate
	 * tokens. A delimiter is always a single character; for multi-character
	 * delimiters, consider using {@code delimitedListToStringArray}
	 * 
	 * @param str
	 *            the String to tokenize
	 * @param delimiters
	 *            the delimiter characters, assembled as String (each of those
	 *            characters is individually considered as delimiter).
	 * @return an array of the tokens
	 * @see java.util.StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * <p>
	 * The given delimiters string is supposed to consist of any number of
	 * delimiter characters. Each of those characters can be used to separate
	 * tokens. A delimiter is always a single character; for multi-character
	 * delimiters, consider using {@code delimitedListToStringArray}
	 * 
	 * @param str
	 *            the String to tokenize
	 * @param delimiters
	 *            the delimiter characters, assembled as String (each of those
	 *            characters is individually considered as delimiter)
	 * @param trimTokens
	 *            trim the tokens via String's {@code trim}
	 * @param ignoreEmptyTokens
	 *            omit empty tokens from the result array (only applies to
	 *            tokens that are empty after trimming; StringTokenizer will not
	 *            consider subsequent delimiters as token in the first place).
	 * @return an array of the tokens ({@code null} if the input String was
	 *         {@code null})
	 * @see java.util.StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
			boolean ignoreEmptyTokens) {

		if (str == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return toStringArray(tokens);
	}

	/**
	 * Take a String which is a delimited list and convert it to a String array.
	 * <p>
	 * A single delimiter can consists of more than one character: It will still
	 * be considered as single delimiter string, rather than as bunch of
	 * potential delimiter characters - in contrast to
	 * {@code tokenizeToStringArray}.
	 * 
	 * @param str
	 *            the input String
	 * @param delimiter
	 *            the delimiter between elements (this is a single delimiter,
	 *            rather than a bunch individual delimiter characters)
	 * @return an array of the tokens in the list
	 * @see #tokenizeToStringArray
	 */
	public static String[] delimitedListToStringArray(String str, String delimiter) {
		return delimitedListToStringArray(str, delimiter, null);
	}

	/**
	 * Take a String which is a delimited list and convert it to a String array.
	 * <p>
	 * A single delimiter can consists of more than one character: It will still
	 * be considered as single delimiter string, rather than as bunch of
	 * potential delimiter characters - in contrast to
	 * {@code tokenizeToStringArray}.
	 * 
	 * @param str
	 *            the input String
	 * @param delimiter
	 *            the delimiter between elements (this is a single delimiter,
	 *            rather than a bunch individual delimiter characters)
	 * @param charsToDelete
	 *            a set of characters to delete. Useful for deleting unwanted
	 *            line breaks: e.g. "\r\n\f" will delete all new lines and line
	 *            feeds in a String.
	 * @return an array of the tokens in the list
	 * @see #tokenizeToStringArray
	 */
	public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] { str };
		}
		List<String> result = new ArrayList<String>();
		if ("".equals(delimiter)) {
			for (int i = 0; i < str.length(); i++) {
				result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
			}
		} else {
			int pos = 0;
			int delPos;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(deleteAny(str.substring(pos), charsToDelete));
			}
		}
		return toStringArray(result);
	}

	/**
	 * Convert a CSV list into an array of Strings.
	 * 
	 * @param str
	 *            the input String
	 * @return an array of Strings, or the empty array in case of empty input
	 */
	public static String[] commaDelimitedListToStringArray(String str) {
		return delimitedListToStringArray(str, ",");
	}

	/**
	 * Convenience method to convert a CSV string list to a set. Note that this
	 * will suppress duplicates.
	 * 
	 * @param str
	 *            the input String
	 * @return a Set of String entries in the list
	 */
	public static Set<String> commaDelimitedListToSet(String str) {
		Set<String> set = new TreeSet<String>();
		String[] tokens = commaDelimitedListToStringArray(str);
		for (String token : tokens) {
			set.add(token);
		}
		return set;
	}

	/**
	 * Convenience method to return a Collection as a delimited (e.g. CSV)
	 * String. E.g. useful for {@code toString()} implementations.
	 * 
	 * @param coll
	 *            the Collection to display
	 * @param delim
	 *            the delimiter to use (probably a ",")
	 * @param prefix
	 *            the String to start each element with
	 * @param suffix
	 *            the String to end each element with
	 * @return the delimited String
	 */
	public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
		if (CollectionUtils.isEmpty(coll)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = coll.iterator();
		while (it.hasNext()) {
			sb.append(prefix).append(it.next()).append(suffix);
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}

	/**
	 * Convenience method to return a Collection as a delimited (e.g. CSV)
	 * String. E.g. useful for {@code toString()} implementations.
	 * 
	 * @param coll
	 *            the Collection to display
	 * @param delim
	 *            the delimiter to use (probably a ",")
	 * @return the delimited String
	 */
	public static String collectionToDelimitedString(Collection<?> coll, String delim) {
		return collectionToDelimitedString(coll, delim, "", "");
	}

	/**
	 * Convenience method to return a Collection as a CSV String. E.g. useful
	 * for {@code toString()} implementations.
	 * 
	 * @param coll
	 *            the Collection to display
	 * @return the delimited String
	 */
	public static String collectionToCommaDelimitedString(Collection<?> coll) {
		return collectionToDelimitedString(coll, ",");
	}

	/**
	 * Convenience method to return a String array as a delimited (e.g. CSV)
	 * String. E.g. useful for {@code toString()} implementations.
	 * 
	 * @param arr
	 *            the array to display
	 * @param delim
	 *            the delimiter to use (probably a ",")
	 * @return the delimited String
	 */
	public static String arrayToDelimitedString(Object[] arr, String delim) {
		if (ObjectUtils.isEmpty(arr)) {
			return "";
		}
		if (arr.length == 1) {
			return ObjectUtils.nullSafeToString(arr[0]);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(arr[i]);
		}
		return sb.toString();
	}

	/**
	 * Convenience method to return a String array as a CSV String. E.g. useful
	 * for {@code toString()} implementations.
	 * 
	 * @param arr
	 *            the array to display
	 * @return the delimited String
	 */
	public static String arrayToCommaDelimitedString(Object[] arr) {
		return arrayToDelimitedString(arr, ",");
	}

	/** ------------------传说中的分割线----------------------- **/
	public static final char[] DEFAULT_SPLIT_CHARS = new char[] { ' ', ',', ';', '、' };

	public static boolean isNotEmpty(CharSequence text) {
		return !isEmpty(text);
	}

	public static boolean isEmpty(CharSequence text) {
		return text == null || text.length() == 0;
	}

	public static boolean isNotEmpty(CharSequence... text) {
		return !isEmpty(text);
	}

	public static boolean isEmpty(CharSequence... text) {
		for (CharSequence s : text) {
			if (isEmpty(s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean equals(String a, String b, boolean ignoreCase) {
		if (a == null || b == null) {
			return a == b;
		}

		if (a.length() == 0 || b.length() == 0) {
			return a.length() == b.length();
		}

		return ignoreCase ? a.equalsIgnoreCase(b) : a.equals(b);
	}

	public static boolean equals(String a, String b) {
		return equals(a, b, false);
	}

	public static String[] commonSplit(String str) {
		return split(str, DEFAULT_SPLIT_CHARS);
	}

	public static <T> List<T> splitList(Class<T> type, String strs, String filter) {
		return splitList(type, strs, filter, true);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> splitList(Class<T> type, String strs, String filter, boolean isTrim) {
		Assert.notNull(type);
		Assert.notNull(filter);

		if (strs == null) {
			return Collections.EMPTY_LIST;
		}

		String[] arr = split(strs, isTrim, filter);
		if (ArrayUtils.isEmpty(arr)) {
			return Collections.EMPTY_LIST;
		}

		List<T> list = new ArrayList<T>(arr.length);
		if (String.class.isAssignableFrom(type)) {
			for (String str : arr) {
				if (str.length() == 0) {
					continue;
				}

				if (isTrim) {
					str = str.trim();
					if (str.length() == 0) {
						continue;
					}
				}

				T t = (T) str;
				list.add(t);
			}
		} else if (Integer.class.isAssignableFrom(type)) {
			for (String str : arr) {
				if (str.length() == 0) {
					continue;
				}

				if (isTrim) {
					str = str.trim();
					if (str.length() == 0) {
						continue;
					}
				}

				T t = (T) Integer.valueOf(str);
				list.add(t);
			}
		} else if (Short.class.isAssignableFrom(type)) {
			for (String str : arr) {
				if (str.length() == 0) {
					continue;
				}

				if (isTrim) {
					str = str.trim();
					if (str.length() == 0) {
						continue;
					}
				}

				T t = (T) Short.valueOf(str);
				list.add(t);
			}
		} else if (Long.class.isAssignableFrom(type)) {
			for (String str : arr) {
				if (str.length() == 0) {
					continue;
				}

				if (isTrim) {
					str = str.trim();
					if (str.length() == 0) {
						continue;
					}
				}

				T t = (T) Long.valueOf(str);
				list.add(t);
			}
		} else if (Float.class.isAssignableFrom(type)) {
			for (String str : arr) {
				if (str.length() == 0) {
					continue;
				}

				if (isTrim) {
					str = str.trim();
					if (str.length() == 0) {
						continue;
					}
				}

				T t = (T) Float.valueOf(str);
				list.add(t);
			}
		} else if (Double.class.isAssignableFrom(type)) {
			for (String str : arr) {
				if (str.length() == 0) {
					continue;
				}

				if (isTrim) {
					str = str.trim();
					if (str.length() == 0) {
						continue;
					}
				}

				T t = (T) Double.valueOf(str);
				list.add(t);
			}
		}
		return (List<T>) list;
	}

	public static List<String> toStrList(String strs, boolean isTrim) {
		if (isEmpty(strs)) {
			return null;
		}

		List<String> list = new ArrayList<String>();
		String[] strList = strs.split(",");
		for (String str : strList) {
			if (isEmpty(str)) {
				continue;
			}

			if (isTrim) {
				str = str.trim();
			}

			if (isEmpty(str)) {
				continue;
			}
			list.add(str);
		}
		return list;
	}

	public static String addStr(String str, String addStr, int beginIndex) {
		if (addStr != null && addStr.length() != 0) {
			String str1 = str.substring(0, beginIndex);
			String str2 = str.substring(beginIndex);
			return str1 + addStr + str2;
		}
		return str;
	}

	/**
	 * 1M = 1024K
	 * 
	 * @param size
	 * @param toSuffix
	 * @return
	 */
	public static double parseDiskSize(String size, String toSuffix) {
		int len = size.length();
		double oldSize;
		if (size.endsWith("GB") || size.endsWith("G")) {
			oldSize = Double.parseDouble(size.substring(0, len - 2)) * 1024 * 1024 * 1024;
		} else if (size.endsWith("MB") || size.endsWith("M")) {
			oldSize = Double.parseDouble(size.substring(0, len - 2)) * 1024 * 1024;
		} else if (size.endsWith("KB") || size.endsWith("K")) {
			oldSize = Double.parseDouble(size.substring(0, len - 2)) * 1024;
		} else if (size.endsWith("B")) {
			oldSize = Double.parseDouble(size.substring(0, len - 1));
		} else {
			oldSize = Double.parseDouble(size);
		}

		if ("GB".equals(toSuffix) || "G".equals(toSuffix)) {
			return oldSize / (1024 * 1024 * 1024);
		} else if ("MB".equals(toSuffix) || "M".equals(toSuffix)) {
			return oldSize / (1024 * 1024);
		} else if ("KB".equals(toSuffix) || "K".equals(toSuffix)) {
			return oldSize / (1024);
		} else if ("B".equals(toSuffix)) {
			return oldSize;
		} else {
			return oldSize;
		}
	}

	/**
	 * 将字符串的走出指定长度的部分截取，向后面添加指定字符串
	 * 
	 * @param len
	 * @param repStr
	 */
	public static String sub(String str, int len, String repStr) {
		if (str.length() > len) {
			return str.substring(0, len) + repStr;
		}
		return str;
	}

	/**
	 * 半角转全角
	 * 
	 * @param input
	 *            String.
	 * @return 全角字符串.
	 */
	public static String toSBC(String input) {
		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '\u3000';
			} else if (c[i] < '\177') {
				c[i] = (char) (c[i] + 65248);

			}
		}
		return new String(c);
	}

	/**
	 * 全角转半角
	 * 
	 * @param input
	 *            String.
	 * @return 半角字符串
	 */
	public static String toDBC(String input) {
		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '\u3000') {
				c[i] = ' ';
			} else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
				c[i] = (char) (c[i] - 65248);

			}
		}
		String returnString = new String(c);
		return returnString;
	}

	/**
	 * 判断是否数字(小数和负数也数字)
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(CharSequence str) {
		if (isEmpty(str)) {
			return false;
		}

		boolean findPoint = false;
		int effectiveCount = 0;
		for (int i = 0, len = str.length(); i < len; i++) {
			char chr = str.charAt(i);
			if (chr == '-' || chr == '+') {
				if (effectiveCount == 0) {
					effectiveCount++;
					continue;
				}

				return false;
			}
			
			if (chr == '.') {
				if (findPoint) {
					return false;
				}

				findPoint = true;
				effectiveCount++;
				continue;
			}

			if (!Character.isDigit(chr)) {
				return false;
			}

			effectiveCount++;
		}
		return true;
	}

	public static String toUpperCase(String text, int begin, int end) {
		char[] chars = text.toCharArray();
		for (int i = begin; i < end; i++) {
			chars[i] = Character.toUpperCase(chars[i]);
		}
		return new String(chars);
	}

	public static String toLowerCase(String text, int begin, int end) {
		char[] chars = text.toCharArray();
		for (int i = begin; i < end; i++) {
			chars[i] = Character.toLowerCase(chars[i]);
		}
		return new String(chars);
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

		if (File.separator.equals(FOLDER_SEPARATOR)) {
			return path.replaceAll("\\\\", FOLDER_SEPARATOR);
		} else {
			return path.replaceAll(FOLDER_SEPARATOR, "\\\\");
		}
	}

	/**
	 * 把不足的地方用指定字符填充
	 * 
	 * @param str
	 * @param complemented
	 * @param length
	 * @return
	 */
	public static String complemented(String str, char complemented, int length) {
		if (length < str.length()) {
			throw new ParameterException("length error [" + str + "]");
		}

		if (length == str.length()) {
			return str;
		} else {
			CharBuffer charBuffer = CharBuffer.allocate(length);
			for (int i = 0; i < length - str.length(); i++) {
				charBuffer.put(complemented);
			}
			charBuffer.put(str);
			return new String(charBuffer.array());
		}
	}

	/**
	 * 颠倒字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String reversed(String str) {
		if (isEmpty(str)) {
			return str;
		}

		return new String(reversedCharArray(str.toCharArray()));
	}

	// 根据Unicode编码判断中文汉字和符号
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串中是否存在中文
	 * 
	 * @param charSequence
	 * @return
	 */
	public static boolean containsChinese(CharSequence charSequence) {
		if (charSequence == null || charSequence.length() == 0) {
			return false;
		}

		for (int i = 0; i < charSequence.length(); i++) {
			if (isChinese(charSequence.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static char[] mergeCharArray(char[]... chars) {
		StringBuilder sb = new StringBuilder();
		for (char[] cs : chars) {
			sb.append(cs);
		}
		return sb.toString().toCharArray();
	}

	public static char[] reversedCharArray(char[] array) {
		if (array == null) {
			return array;
		}

		char[] newArray = new char[array.length];
		int index = 0;
		for (int i = newArray.length - 1; i >= 0; i--, index++) {
			newArray[index] = array[i];
		}
		return newArray;
	}

	/**
	 * 把钱保留两位小数
	 * 
	 * @param price
	 *            单位:分
	 * @return
	 */
	public static String formatNothingToYuan(long price) {
		return formatNumberPrecision((double) price / 100, 2);
	}

	/**
	 * 保留小数点精度
	 * 
	 * @param number
	 * @param len
	 *            保留多少位
	 * @return
	 */
	public static String formatNumberPrecision(double number, int len) {
		if (len < 0) {
			throw new IllegalStateException("len < 0");
		}

		if (len == 0) {
			return ((long) number) + "";
		}

		if (number == 0) {
			CharBuffer charBuffer = CharBuffer.allocate(len + 2);
			charBuffer.put('0');
			charBuffer.put('.');
			for (int i = 0; i < len; i++) {
				charBuffer.put('0');
			}
			return new String(charBuffer.array());
		}

		CharBuffer charBuffer = CharBuffer.allocate(len + 3);
		charBuffer.put("#0.");
		for (int i = 0; i < len; i++) {
			charBuffer.put("0");
		}
		DecimalFormat decimalFormat = new DecimalFormat(new String(charBuffer.array()));
		return decimalFormat.format(number);
	}

	public static String[] split(String str, char... filters) {
		if (isEmpty(str)) {
			return new String[0];
		}

		return split(str, true, filters);
	}

	public static String[] split(String str, String... filters) {
		if (isEmpty(str)) {
			return new String[0];
		}

		return split(str, true, filters);
	}

	public static String[] split(String str, boolean ignoreNull, char... filters) {
		if (isEmpty(str)) {
			return new String[0];
		}

		List<String> list = new ArrayList<String>();
		int begin = 0;
		int size = str.length();
		for (int i = 0; i < size; i++) {
			char c = str.charAt(i);
			boolean find = false;
			for (char s : filters) {
				if (c == s) {
					find = true;
					break;
				}
			}

			if (find) {
				if (ignoreNull && i == begin) {
					begin++;
					continue;
				}

				list.add(str.substring(begin, i));
				begin = i + 1;
			} else if (i == size - 1) {
				if (begin == 0) {
					list.add(str);
				} else {
					list.add(str.substring(begin));
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}

	public static String[] split(String str, boolean ignoreNull, String... filters) {
		if (isEmpty(str)) {
			return new String[0];
		}

		int begin = 0;
		int index = -1;
		String v = null;
		for (String f : filters) {
			index = str.indexOf(f, begin);
			if (index != -1) {
				v = f;
				break;
			}
		}

		if (index == -1) {
			return new String[] { str };
		}

		List<String> list = new ArrayList<String>();
		while (index != -1 && v != null) {
			if (ignoreNull && begin == index) {
				begin++;
				for (String f : filters) {
					index = str.indexOf(f, begin);
					if (index != -1) {
						v = f;
						break;
					}
				}
				continue;
			}

			list.add(str.substring(begin, index));
			begin = index + v.length();

			for (String f : filters) {
				index = str.indexOf(f, begin);
				if (index != -1) {
					v = f;
					break;
				}
			}
		}

		if (begin < str.length()) {
			list.add(str.substring(begin));
		}

		return list.toArray(new String[list.size()]);
	}

	/**
	 * 可以解决1,234这种问题
	 * 
	 * @param text
	 * @return
	 */
	public static String formatNumberText(String text) {
		if (isEmpty(text)) {
			return null;
		}

		char[] chars = new char[text.length()];
		int pos = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isWhitespace(c) || c == ',') {
				continue;
			}
			chars[pos++] = c;
		}

		return pos == 0 ? null : new String(chars, 0, pos);
	}

	public static Boolean parseBoolean(String text, Boolean defaultValue) {
		if (!hasText(text)) {
			return defaultValue;
		}

		return parseBooleanValue(text);
	}

	private static boolean parseBooleanValue(String text) {
		return BOOLEANS.contains(text) || BOOLEANS.contains(text.toLowerCase());
	}

	public static boolean parseBoolean(Object text, Boolean defaultValue) {
		if (text == null) {
			return defaultValue;
		}

		return parseBoolean(String.valueOf(text), defaultValue);
	}

	public static boolean parseBoolean(Object text) {
		return parseBoolean(text, false);
	}

	public static boolean parseBoolean(String text) {
		return parseBoolean(text, false);
	}

	public static Byte parseByte(String text, int radix, Byte defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}
		return Byte.parseByte(text, radix);
	}

	public static byte parseByte(String text, int radix, byte defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}
		return Byte.parseByte(text, radix);
	}

	public static byte parseByte(String text) {
		return parseByte(text, 10, (byte) 0);
	}

	public static Byte parseByte(String text, Byte defaultValue) {
		return parseByte(text, 10, defaultValue);
	}

	public static short parseShort(String text) {
		return parseShort(text, 10, (short) 0);
	}

	public static Short parseShort(String text, Short defaultValue) {
		return parseShort(text, 10, defaultValue);
	}

	public static Short parseShort(String text, int radix, Short defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}
		return Short.parseShort(text, radix);
	}

	public static short parseShort(String text, int radix, short defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}
		return Short.parseShort(text, radix);
	}

	public static Integer parseInt(String text, int radix, Integer defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}
		return Integer.parseInt(text, radix);
	}

	public static int parseInt(String text, int radix, int defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}
		return Integer.parseInt(text, radix);
	}

	public static int parseInt(String text, int defaultValue) {
		return parseInt(text, 10, defaultValue);
	}

	public static int parseInt(String text) {
		return parseInt(text, 0);
	}

	public static Integer parseInt(String text, Integer defaultValue) {
		return parseInt(text, 10, defaultValue);
	}

	public static Long parseLong(String text, int radix, Long defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}

		return Long.parseLong(v, radix);
	}

	public static Long parseLong(String text, Long defaultValue) {
		return parseLong(text, defaultValue);
	}

	public static long parseLong(String text, long defaultValue) {
		return parseLong(text, 10, defaultValue);
	}

	public static long parseLong(String text, int radix, long defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}

		return Long.parseLong(v, radix);
	}

	public static long parseLong(String text) {
		return parseLong(text, 10, 0L);
	}

	public static float parseFloat(String text) {
		return parseFloat(text, 0f);
	}

	public static Float parseFloat(String text, Float defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}
		return Float.parseFloat(v);
	}

	public static float parseFloat(String text, float defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}
		return Float.parseFloat(v);
	}

	public static double parseDouble(String text) {
		return parseDouble(text, 0);
	}

	public static Double parseDouble(String text, Double defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}

		return Double.parseDouble(v);
	}

	public static double parseDouble(String text, double defaultValue) {
		String v = formatNumberText(text);
		if (isEmpty(v)) {
			return defaultValue;
		}

		return Double.parseDouble(v);
	}

	public static KeyValuePair<String, String> parseKV(String text, String separator) {
		int index = text.indexOf(separator);
		if (index == -1) {
			return null;
		}

		return new KeyValuePair<String, String>(text.substring(0, index), text.substring(index + separator.length()));
	}

	public static String toString(Object value, Object defaultValue) {
		return toString(value, defaultValue, true);
	}

	public static String toString(Object value, Object defaultValue, boolean checkLength) {
		if (value == null) {
			return defaultValue == null ? null : defaultValue.toString();
		}

		String v = (value instanceof String) ? (String) value : value.toString();
		if (checkLength && !StringUtils.hasLength(v)) {
			return defaultValue.toString();
		}
		return v;
	}

	public static char parseChar(String text, char defaultValue) {
		if (isEmpty(text)) {
			return defaultValue;
		}
		return text.charAt(0);
	}

	/**
	 * 把unicode 转成中文
	 * 
	 * @return
	 */
	public static String convertUnicode(String ori) {
		char aChar;
		int len = ori.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = ori.charAt(x++);
			if (aChar == '\\') {
				aChar = ori.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = ori.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);

		}
		return outBuffer.toString();
	}

	public static void replace(char[] chars, char replace, char newChar) {
		if (ArrayUtils.isEmpty(chars)) {
			return;
		}

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == replace) {
				chars[i] = newChar;
			}
		}
	}

	public static String replace(String text, char replace, char newChar) {
		if (StringUtils.isEmpty(text)) {
			return text;
		}

		char[] chars = text.toCharArray();
		replace(chars, replace, newChar);
		return new String(chars);
	}

	public static String format(String text, final Map<String, ?> valueMap) {
		return FormatUtils.format(text, valueMap);
	}

	public static String format(String text, Object... args) {
		return FormatUtils.formatPlaceholder(text, null, args);
	}

	public static boolean startsWith(String text, String prefix, boolean ignoreCase) {
		return startsWith(text, prefix, 0, ignoreCase);
	}

	public static boolean startsWith(String text, String prefix, int toOffset, boolean ignoreCase) {
		if (ignoreCase) {
			int to = toOffset;
			int po = 0;
			int pc = prefix.length();
			// Note: toffset might be near -1>>>1.
			if ((toOffset < 0) || (toOffset > text.length() - pc)) {
				return false;
			}
			while (--pc >= 0) {
				if (Character.toLowerCase(text.charAt(to++)) != Character.toLowerCase(prefix.charAt(po++))) {
					return false;
				}
			}
			return true;
		} else {
			return text.startsWith(prefix);
		}
	}

	/**
	 * 字符串的压缩
	 *
	 * @param str
	 *            待压缩的字符串
	 * @return 返回压缩后的字符串
	 * @throws IOException
	 */
	public static String compress(String str, String charsetName) throws IOException {
		if (null == str || str.length() <= 0) {
			return str;
		}
		// 创建一个新的 byte 数组输出流
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// 使用默认缓冲区大小创建新的输出流
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		// 将 b.length 个字节写入此输出流
		gzip.write(str.getBytes(charsetName));
		gzip.close();
		// 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
		return out.toString(charsetName);
	}

	/**
	 * 字符串的解压
	 *
	 * @param str
	 *            对字符串解压
	 * @return 返回解压缩后的字符串
	 * @throws IOException
	 */
	public static String unCompress(String str, String charsetName) throws IOException {
		if (null == str || str.length() <= 0) {
			return str;
		}
		// 创建一个新的 byte 数组输出流
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
		ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes(charsetName));
		// 使用默认缓冲区大小创建新的输入流
		GZIPInputStream gzip = new GZIPInputStream(in);
		byte[] buffer = new byte[256];
		int n = 0;
		while ((n = gzip.read(buffer)) >= 0) {// 将未压缩数据读入字节数组
			// 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte数组输出流
			out.write(buffer, 0, n);
		}
		// 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
		return out.toString(charsetName);
	}

	public static boolean contains(String text, String index, boolean ignoreCase) {
		if (text == null || index == null) {
			return text == index;
		}

		if (ignoreCase) {
			return text.toLowerCase().contains(index.toLowerCase());
		} else {
			return text.contains(index);
		}
	}

	/**
	 * 对字符串进行转义
	 * 
	 * @param text
	 * @param chars
	 *            要转义的字符
	 * @return
	 */
	public static String transferredMeaning(String text, char... chars) {
		if (isEmpty(text)) {
			return text;
		}

		int len = text.length();
		char[] values = new char[len * 2];
		int vIndex = 0;
		for (int index = 0; index < len; index++) {
			char v = text.charAt(index);
			for (char c : chars) {
				if (c == v) {
					values[vIndex++] = '\\';
					break;
				}
			}
			values[vIndex++] = v;
		}
		return new String(values, 0, vIndex);
	}

	/**
	 * 驼峰命名大写替换
	 * 
	 * @param humpNaming
	 * @param replacement
	 * @return
	 */
	public static String humpNamingReplacement(String humpNaming, String replacement) {
		int len = humpNaming.length();
		StringBuilder sb = new StringBuilder(len * 2);
		for (int i = 0; i < len; i++) {
			char c = humpNaming.charAt(i);
			if (Character.isUpperCase(c)) {// 如果是大写的
				if (i != 0) {
					sb.append(replacement);
				}
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Parse the given {@code timeZoneString} value into a {@link TimeZone}.
	 * 
	 * @param timeZoneString
	 *            the time zone {@code String}, following
	 *            {@link TimeZone#getTimeZone(String)} but throwing
	 *            {@link IllegalArgumentException} in case of an invalid time
	 *            zone specification
	 * @return a corresponding {@link TimeZone} instance
	 * @throws IllegalArgumentException
	 *             in case of an invalid time zone specification
	 */
	public static TimeZone parseTimeZoneString(String timeZoneString) {
		TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
		if ("GMT".equals(timeZone.getID()) && !timeZoneString.startsWith("GMT")) {
			// We don't want that GMT fallback...
			throw new IllegalArgumentException("Invalid time zone specification '" + timeZoneString + "'");
		}
		return timeZone;
	}

	public static String removeChar(String text, char remove) {
		int len = text.length();
		if (len == 0) {
			return text;
		}

		char[] cs = new char[len];
		int index = 0;
		for (int i = 0; i < len; i++) {
			char v = text.charAt(i);
			if (v == remove) {
				continue;
			}

			cs[index++] = v;
		}
		return new String(cs, 0, index);
	}

	public static int indexOf(String text, String index, int fromIndex, int endIndex) {
		return indexOf(text.toCharArray(), index.toCharArray(), fromIndex, endIndex);
	}

	public static int indexOf(char[] source, char[] target, int fromIndex, int endIndex) {
		int index = indexOf(source, fromIndex, endIndex - fromIndex, target, 0, target.length, 0);
		return index == -1 ? -1 : index + fromIndex;
	}

	/**
	 * Code shared by String and StringBuffer to do searches. The source is the
	 * character array being searched, and the target is the string being
	 * searched for.
	 *
	 * @param source
	 *            the characters being searched.
	 * @param sourceOffset
	 *            offset of the source string.
	 * @param sourceCount
	 *            count of the source string.
	 * @param target
	 *            the characters being searched for.
	 * @param targetOffset
	 *            offset of the target string.
	 * @param targetCount
	 *            count of the target string.
	 * @param fromIndex
	 *            the index to begin searching from.
	 */
	public static int indexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset,
			int targetCount, int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		char first = target[targetOffset];
		int max = sourceOffset + (sourceCount - targetCount);

		for (int i = sourceOffset + fromIndex; i <= max; i++) {
			/* Look for first character. */
			if (source[i] != first) {
				while (++i <= max && source[i] != first)
					;
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++)
					;

				if (j == end) {
					/* Found whole string. */
					return i - sourceOffset;
				}
			}
		}
		return -1;
	}

	/**
	 * 从左边开始获取字符串组合第一次出现的位置, prefix和suffix必须是成对出现的，允许嵌套
	 * 
	 * @param text
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	public static KeyValuePair<Integer, Integer> indexOf(String text, String prefix, String suffix) {
		return indexOf(text, prefix, suffix, 0);
	}
	
	public static KeyValuePair<Integer, Integer> indexOf(String text, String prefix, String suffix, int fromIndex) {
		return indexOf(text.toCharArray(), prefix.toCharArray(), suffix.toCharArray(), fromIndex, text.length());
	}

	/**
	 * 从左边开始获取组合第一次出现的位置, prefix和suffix必须是成对出现的，允许嵌套
	 * 
	 * @param source
	 * @param prefix
	 * @param suffix
	 * @param fromIndex
	 * @param endIndex
	 * @return
	 */
	public static KeyValuePair<Integer, Integer> indexOf(char[] source, char[] prefix, char[] suffix, int fromIndex,
			int endIndex) {
		int begin = indexOf(source, prefix, 0, endIndex);
		if (begin == -1) {
			return null;
		}

		int end = indexOf(source, suffix, begin + prefix.length, endIndex);
		if (end == -1) {
			return null;
		}

		int tempBegin = begin;
		int tempEnd = end;
		while (true) {// 重复查找是否存在嵌套,直到找到最外层的{suffix}
			int nestingLevel = 0;// 嵌套了多少层
			while (true) {
				int index = indexOf(source, prefix, tempBegin + prefix.length, tempEnd);
				if (index == -1) {
					break;
				}
				nestingLevel++;
				tempBegin = index;
			}

			if (nestingLevel == 0) {
				break;
			}

			// prefix嵌套了多少层就将suffix向外移多少层
			for (int i = 0; i < nestingLevel; i++) {
				tempEnd = indexOf(source, suffix, tempEnd + suffix.length, endIndex);
				if (tempEnd == -1) {// 两边的符号嵌套层级不一至
					return null;
				}
			}
		}
		return new KeyValuePair<Integer, Integer>(begin, tempEnd);
	}

	public static int lastIndexOf(String text, String index, int beginIndex, int endIndex) {
		return lastIndexOf(text.toCharArray(), index.toCharArray(), beginIndex, endIndex);
	}

	public static int lastIndexOf(char[] source, char[] target, int fromIndex, int endIndex) {
		int sourceCount = Math.min(fromIndex, source.length) - endIndex;
		int index = lastIndexOf(source, endIndex, sourceCount, target, 0, target.length, sourceCount);
		return index == -1 ? -1 : index + endIndex;
	}

	/**
	 * Code shared by String and StringBuffer to do searches. The source is the
	 * character array being searched, and the target is the string being
	 * searched for.
	 *
	 * @param source
	 *            the characters being searched.
	 * @param sourceOffset
	 *            offset of the source string.
	 * @param sourceCount
	 *            count of the source string.
	 * @param target
	 *            the characters being searched for.
	 * @param targetOffset
	 *            offset of the target string.
	 * @param targetCount
	 *            count of the target string.
	 * @param fromIndex
	 *            the index to begin searching from.
	 */
	public static int lastIndexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset,
			int targetCount, int fromIndex) {
		/*
		 * Check arguments; return immediately where possible. For consistency,
		 * don't check for null str.
		 */
		int rightIndex = sourceCount - targetCount;
		if (fromIndex < 0) {
			return -1;
		}
		if (fromIndex > rightIndex) {
			fromIndex = rightIndex;
		}
		/* Empty string always matches. */
		if (targetCount == 0) {
			return fromIndex;
		}

		int strLastIndex = targetOffset + targetCount - 1;
		char strLastChar = target[strLastIndex];
		int min = sourceOffset + targetCount - 1;
		int i = min + fromIndex;

		startSearchForLastChar: while (true) {
			while (i >= min && source[i] != strLastChar) {
				i--;
			}
			if (i < min) {
				return -1;
			}
			int j = i - 1;
			int start = j - (targetCount - 1);
			int k = strLastIndex - 1;

			while (j > start) {
				if (source[j--] != target[k--]) {
					i--;
					continue startSearchForLastChar;
				}
			}
			return start - sourceOffset + 1;
		}
	}
}
