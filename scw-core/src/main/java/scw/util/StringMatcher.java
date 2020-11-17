package scw.util;

/**
 * 字符串匹配
 * 
 * @author shuchaowen
 *
 */
public interface StringMatcher {
	
	/**
	 * 前缀匹配
	 */
	static final StringMatcher PREFIX_MATCHER = new StringMatcher() {
		
		public boolean match(String pattern, String text) {
			return text.startsWith(pattern);
		}
		
		public boolean isPattern(String text) {
			return true;
		}
	};
	
	/**
	 * Does the given {@code text} represent a pattern that can be matched by an
	 * implementation of this interface?
	 * <p>
	 * If the return value is {@code false}, then the {@link #match} method does
	 * not have to be used because direct equality comparisons on the static
	 * path Strings will lead to the same result.
	 * 
	 * @param path
	 *            the path String to check
	 * @return {@code true} if the given {@code text} represents a pattern
	 */
	boolean isPattern(String text);

	/**
	 * Match the given {@code path} against the given {@code pattern}, according
	 * to this StringMatcher's matching strategy.
	 * 
	 * @param pattern
	 *            the pattern to match against
	 * @param path
	 *            the path String to test
	 * @return {@code true} if the supplied {@code text} matched, {@code false}
	 *         if it didn't
	 */
	boolean match(String pattern, String text);
}
