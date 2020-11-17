package scw.value;

import java.util.Map.Entry;
import java.util.Properties;

import scw.core.StringFormat;
import scw.util.FormatUtils;
import scw.util.PropertyPlaceholderHelper;

public class StringValueFactory extends DefaultValueFactory<String> {
	/**
	 * Resolve {@code $ ...} placeholders in the given text, replacing them with
	 * corresponding system property values.
	 * 
	 * @param text
	 *            the String to resolve
	 * @return the resolved String
	 * @see #PLACEHOLDER_PREFIX
	 * @see #PLACEHOLDER_SUFFIX
	 * @throws IllegalArgumentException
	 *             if there is an unresolvable placeholder
	 */
	public String resolvePlaceholders(String text) {
		return resolvePlaceholders(text, false);
	}

	/**
	 * Resolve {@code $ ...} placeholders in the given text, replacing them with
	 * corresponding system property values. Unresolvable placeholders with no
	 * default value are ignored and passed through unchanged if the flag is set
	 * to {@code true}.
	 * 
	 * @param text
	 *            the String to resolve
	 * @param ignoreUnresolvablePlaceholders
	 *            whether unresolved placeholders are to be ignored
	 * @return the resolved String
	 * @see #PLACEHOLDER_PREFIX
	 * @see #PLACEHOLDER_SUFFIX
	 * @throws IllegalArgumentException
	 *             if there is an unresolvable placeholder and the
	 *             "ignoreUnresolvablePlaceholders" flag is {@code false}
	 */
	public String resolvePlaceholders(String text, boolean ignoreUnresolvablePlaceholders) {
		PropertyPlaceholderHelper helper = (ignoreUnresolvablePlaceholders ? PropertyPlaceholderHelper.nonStrictHelper
				: PropertyPlaceholderHelper.strictHelper);
		return helper.replacePlaceholders(text, new PropertyPlaceholderResolver());
	}

	/**
	 * PlaceholderResolver implementation that resolves against system
	 * properties and system environment variables.
	 */
	private final class PropertyPlaceholderResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

		public String resolvePlaceholder(String placeholderName) {
			return getString(placeholderName);
		}
	}

	public String format(String text) {
		return resolvePlaceholders(FormatUtils.format(text, this, true), true);
	}

	public String format(String text, String prefix, String suffix) {
		return StringFormat.format(text, prefix, suffix, this);
	}

	public Properties format(Properties properties) {
		if (properties == null || properties.isEmpty()) {
			return properties;
		}

		Properties props = new Properties();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			Object value = entry.getValue();
			if (value == null) {
				continue;
			}

			if (value instanceof String) {
				props.put(entry.getKey(), format((String) value));
			} else {
				props.put(entry.getKey(), entry.getValue());
			}
		}
		return props;
	}
}
