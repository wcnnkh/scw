package scw.util.placeholder.support;

import java.util.Iterator;

import scw.instance.Configurable;
import scw.instance.ConfigurableServices;
import scw.instance.ServiceLoaderFactory;
import scw.util.placeholder.ConfigurablePlaceholderReplacer;
import scw.util.placeholder.PlaceholderReplacer;
import scw.util.placeholder.PlaceholderResolver;

public class DefaultPlaceholderReplacer implements ConfigurablePlaceholderReplacer, Configurable{
	private static final String DEFAULT_PREFIX = "{";
	private static final String DEFAULT_SUFFIX = "}";
	private static final PlaceholderReplacer DEFAULT_SIMPLE_REPLACER = new SimplePlaceholderReplaer(DEFAULT_PREFIX, DEFAULT_SUFFIX, true);
	private static final PlaceholderReplacer DEFAULT_SMART_REPLACER = new SmartPlaceholderReplacer(DEFAULT_PREFIX, DEFAULT_SUFFIX, true);	
	private final ConfigurableServices<PlaceholderReplacer> placeholderReplacers = new ConfigurableServices<>(PlaceholderReplacer.class);
	
	public void addPlaceholderReplacer(PlaceholderReplacer placeholderReplacer){
		placeholderReplacers.addService(placeholderReplacer);
	}
	
	@Override
	public Iterator<PlaceholderReplacer> iterator() {
		return placeholderReplacers.iterator();
	}
	
	public String replacePlaceholders(String value,
			PlaceholderResolver placeholderResolver) {
		String textToUse = value;
		for(PlaceholderReplacer replacer : this){
			textToUse = replacer.replacePlaceholders(textToUse, placeholderResolver);
		}
		
		textToUse = SimplePlaceholderReplaer.NON_STRICT_REPLACER.replacePlaceholders(textToUse, placeholderResolver);
		textToUse = SmartPlaceholderReplacer.NON_STRICT_REPLACER.replacePlaceholders(textToUse, placeholderResolver);
		textToUse = DEFAULT_SIMPLE_REPLACER.replacePlaceholders(textToUse, placeholderResolver);
		textToUse = DEFAULT_SMART_REPLACER.replacePlaceholders(textToUse, placeholderResolver);
		return textToUse;
	}
	
	public String replaceRequiredPlaceholders(String value, PlaceholderResolver placeholderResolver){
		String textToUse = value;
		for(PlaceholderReplacer replacer : this){
			textToUse = replacer.replacePlaceholders(textToUse, new RequiredPlaceholderResolver(textToUse, placeholderResolver));
		}
		
		textToUse = SimplePlaceholderReplaer.NON_STRICT_REPLACER.replacePlaceholders(textToUse, new RequiredPlaceholderResolver(textToUse, placeholderResolver));
		textToUse = SmartPlaceholderReplacer.NON_STRICT_REPLACER.replacePlaceholders(textToUse, new RequiredPlaceholderResolver(textToUse, placeholderResolver));
		textToUse = DEFAULT_SIMPLE_REPLACER.replacePlaceholders(textToUse, new RequiredPlaceholderResolver(textToUse, placeholderResolver));
		textToUse = DEFAULT_SMART_REPLACER.replacePlaceholders(textToUse, new RequiredPlaceholderResolver(textToUse, placeholderResolver));
		return textToUse;
	}

	@Override
	public void configure(ServiceLoaderFactory serviceLoaderFactory) {
		placeholderReplacers.configure(serviceLoaderFactory);
	}
}
