package shuchaowen.connection.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import shuchaowen.connection.http.enums.Method;

public class HttpPut extends HttpRequestURLConnection{
	public HttpPut(String url) throws MalformedURLException, IOException {
		super(url);
		setRequestMethod(Method.PUT.name());
		setDoOutput(true);
		setDoInput(true);
	}
	
	public HttpPut(HttpURLConnection httpURLConnection) {
		super(httpURLConnection);
	}
}
