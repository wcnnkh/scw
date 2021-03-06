package scw.util.placeholder.support;

import scw.util.placeholder.ConfigurablePropertyResolver;
import scw.util.placeholder.PlaceholderResolver;

public class DefaultPropertyResolver extends DefaultPlaceholderReplacer implements ConfigurablePropertyResolver{
	private PlaceholderResolver placeholderResolver;
	
	public DefaultPropertyResolver(PlaceholderResolver placeholderResolver){
		this.placeholderResolver = placeholderResolver;
	}

	public String resolvePlaceholders(String text) {
		return replacePlaceholders(text, placeholderResolver);
	}
	
	public String resolveRequiredPlaceholders(String text)
			throws IllegalArgumentException {
		return replaceRequiredPlaceholders(text, placeholderResolver);
	}
}
