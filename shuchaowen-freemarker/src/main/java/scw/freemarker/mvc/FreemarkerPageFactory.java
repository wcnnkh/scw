package scw.freemarker.mvc;

import scw.core.utils.StringUtils;
import scw.net.MimeType;
import scw.net.http.MediaType;
import freemarker.template.Configuration;

@scw.core.instance.annotation.Configuration(order = Integer.MIN_VALUE)
public class FreemarkerPageFactory extends AbstractFreemarkerPageFactory {
	private final Configuration configuration;
	private final MimeType mimeType;

	public FreemarkerPageFactory(Configuration configuration) {
		this(configuration, MediaType.TEXT_HTML);
	}

	public FreemarkerPageFactory(Configuration configuration, MimeType mimeType) {
		this.configuration = configuration;
		this.mimeType = mimeType;
	}

	public final Configuration getConfiguration() {
		return configuration;
	}

	public MimeType getMimeType() {
		return mimeType;
	}

	public boolean isSupport(String page) {
		return StringUtils.endsWithIgnoreCase(page, ".ftl")
				|| StringUtils.endsWithIgnoreCase(page, ".html");
	}
}
