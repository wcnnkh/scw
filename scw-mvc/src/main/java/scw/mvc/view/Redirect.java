package scw.mvc.view;

import java.io.IOException;

import scw.core.utils.StringUtils;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.mvc.HttpChannel;

public class Redirect implements View {
	private static Logger logger = LoggerUtils.getLogger(Redirect.class);
	private static final String ROOT_PATH = "/";

	private String url;

	public Redirect(String url) {
		this.url = url;
	}

	public void render(HttpChannel httpChannel) throws IOException {
		String redirect = url;
		if (StringUtils.isEmpty(redirect) || ROOT_PATH.equals(url)) {
			redirect = httpChannel.getRequest().getContextPath();
		} else if (redirect.startsWith(ROOT_PATH) && !redirect.startsWith(httpChannel.getRequest().getContextPath())) {
			redirect = httpChannel.getRequest().getContextPath() + redirect;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("redirect:{}", redirect);
		}
		httpChannel.getResponse().sendRedirect(redirect);
	}

}
