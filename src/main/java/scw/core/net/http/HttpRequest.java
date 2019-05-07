package scw.core.net.http;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import scw.core.net.AbstractUrlRequest;
import scw.core.net.RequestException;
import scw.core.net.http.enums.Method;

public class HttpRequest extends AbstractUrlRequest {
	protected final Method method;
	private Map<String, String> requestProperties;
	private String requestUrl;
	private SSLSocketFactory sslSocketFactory;

	public HttpRequest(Method method, String requestUrl) {
		this.method = method;
		this.requestUrl = requestUrl;
	}

	public String getRequestAddress() {
		return requestUrl;
	}

	@Override
	public void request(URLConnection urlConnection) throws Throwable {
		HttpURLConnection http = (HttpURLConnection) urlConnection;
		if (http instanceof HttpsURLConnection) {
			SSLSocketFactory sslSocketFactory = getSslSocketFactory();
			if(sslSocketFactory != null){
				HttpsURLConnection https = (HttpsURLConnection) urlConnection;
				https.setSSLSocketFactory(sslSocketFactory);
			}
		}

		http.setRequestMethod(method.name());

		urlConnection.setConnectTimeout(10000);
		urlConnection.setReadTimeout(10000);
		if (method != Method.GET) {
			urlConnection.setDoOutput(true);
		}
		urlConnection.setDoInput(true);

		if (requestProperties != null) {
			for (Entry<String, String> entry : requestProperties.entrySet()) {
				urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		super.request(urlConnection);
	}

	@Override
	protected void doOutput(URLConnection urlConnection, OutputStream os) throws Throwable {
	}

	public void setRequestProperties(String key, Object value) {
		if (value == null) {
			return;
		}

		if (requestProperties == null) {
			requestProperties = new HashMap<String, String>();
		}
		requestProperties.put(key, value.toString());
	}

	public Method getMethod() {
		return method;
	}

	public void setContentType(String contentType) {
		setRequestProperties("Content-Type", contentType);
	}

	public void setRequestProperties(Map<String, String> requestProperties) {
		this.requestProperties = requestProperties;
	}
	
	public void setContentType(ContentType contentType){
		setContentType(contentType.asString());
	}

	@Override
	public URL getURL() {
		try {
			return new URL(getRequestAddress());
		} catch (MalformedURLException e) {
			throw new RequestException(getRequestAddress(), e);
		}
	}

	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	@Override
	public Proxy getProxy() {
		return null;
	}

}
