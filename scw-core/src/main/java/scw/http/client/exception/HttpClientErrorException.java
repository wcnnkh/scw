package scw.http.client.exception;

import java.nio.charset.Charset;

import scw.http.HttpHeaders;
import scw.http.HttpStatus;

public class HttpClientErrorException extends HttpStatusCodeException {

	private static final long serialVersionUID = 5177019431887513952L;


	/**
	 * Construct a new instance of {@code HttpClientErrorException} based on
	 * an {@link HttpStatus}.
	 * @param statusCode the status code
	 */
	public HttpClientErrorException(HttpStatus statusCode) {
		super(statusCode);
	}

	/**
	 * Construct a new instance of {@code HttpClientErrorException} based on
	 * an {@link HttpStatus} and status text.
	 * @param statusCode the status code
	 * @param statusText the status text
	 */
	public HttpClientErrorException(HttpStatus statusCode, String statusText) {
		super(statusCode, statusText);
	}

	/**
	 * Construct a new instance of {@code HttpClientErrorException} based on
	 * an {@link HttpStatus}, status text, and response body content.
	 * @param statusCode the status code
	 * @param statusText the status text
	 * @param responseBody the response body content (may be {@code null})
	 * @param responseCharset the response body charset (may be {@code null})
	 */
	public HttpClientErrorException(HttpStatus statusCode, String statusText,
			byte[] responseBody, Charset responseCharset) {

		super(statusCode, statusText, responseBody, responseCharset);
	}

	/**
	 * Construct a new instance of {@code HttpClientErrorException} based on
	 * an {@link HttpStatus}, status text, and response body content.
	 * @param statusCode the status code
	 * @param statusText the status text
	 * @param responseHeaders the response headers (may be {@code null})
	 * @param responseBody the response body content (may be {@code null})
	 * @param responseCharset the response body charset (may be {@code null})
	 */
	public HttpClientErrorException(HttpStatus statusCode, String statusText,
			HttpHeaders responseHeaders, byte[] responseBody, Charset responseCharset) {

		super(statusCode, statusText, responseHeaders, responseBody, responseCharset);
	}

}
