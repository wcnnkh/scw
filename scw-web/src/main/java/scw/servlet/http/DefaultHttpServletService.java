package scw.servlet.http;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scw.beans.BeanFactory;
import scw.core.Constants;
import scw.core.instance.annotation.Configuration;
import scw.http.server.DefaultHttpService;
import scw.value.property.PropertyFactory;

@Configuration(order = Integer.MIN_VALUE, value = HttpServletService.class)
public class DefaultHttpServletService extends DefaultHttpService implements HttpServletService {

	public DefaultHttpServletService(BeanFactory beanFactory, PropertyFactory propertyFactory) {
		super(beanFactory, propertyFactory);
	}

	public String getCharsetName() {
		return Constants.DEFAULT_CHARSET_NAME;
	}

	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding(getCharsetName());
		response.setCharacterEncoding(getCharsetName());
		ServletServerHttpRequest serverHttpRequest = new ServletServerHttpRequest(request);
		ServletServerHttpResponse serverHttpResponse = new ServletServerHttpResponse(response);
		service(serverHttpRequest, serverHttpResponse);
	}

}
