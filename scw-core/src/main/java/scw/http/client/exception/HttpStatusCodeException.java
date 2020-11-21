package scw.http.client.exception;

import java.nio.charset.Charset;

import scw.http.HttpHeaders;
import scw.http.HttpStatus;

public class HttpStatusCodeException extends HttpClientResponseException {

	private static final long serialVersionUID = 5696801857651587810L;


	private final HttpStatus statusCode;


	/**
	 * Construct a new instance with an {@link HttpStatus}.
	 * @param statusCode the status code
	 */
	protected HttpStatusCodeException(HttpStatus statusCode) {
		this(statusCode, statusCode.name(), null, null, null);
	}

	/**
	 * Construct a new instance with an {@link HttpStatus} and status text.
	 * @param statusCode the status code
	 * @param statusText the status text
	 */
	protected HttpStatusCodeException(HttpStatus statusCode, String statusText) {
		this(statusCode, statusText, null, null, null);
	}

	/**
	 * Construct instance with an {@link HttpStatus}, status text, and content.
	 * @param statusCode the status code
	 * @param statusText the status text
	 * @param responseBody the response body content, may be {@code null}
	 * @param responseCharset the response body charset, may be {@code null}
	 */
	protected HttpStatusCodeException(HttpStatus statusCode, String statusText,
			byte[] responseBody, Charset responseCharset) {

		this(statusCode, statusText, null, responseBody, responseCharset);
	}

	/**
	 * Construct instance with an {@link HttpStatus}, status text, content, and
	 * a response charset.
	 * @param statusCode the status code
	 * @param statusText the status text
	 * @param responseHeaders the response headers, may be {@code null}
	 * @param responseBody the response body content, may be {@code null}
	 * @param responseCharset the response body charset, may be {@code null}
	 */
	protected HttpStatusCodeException(HttpStatus statusCode, String statusText,
			HttpHeaders responseHeaders, byte[] responseBody, Charset responseCharset) {

		super(statusCode.value() + " " + statusText, statusCode.value(), statusText,
				responseHeaders, responseBody, responseCharset);
		this.statusCode = statusCode;
	}


	/**
	 * Return the HTTP status code.
	 */
	public HttpStatus getStatusCode() {
		return this.statusCode;
	}

}
