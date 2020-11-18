package scw.http.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import scw.http.HttpCookie;
import scw.http.HttpHeaders;
import scw.http.HttpStatus;
import scw.http.MediaType;
import scw.net.MimeType;
import scw.util.Target;
import scw.util.XUtils;

public class ServerHttpResponseWrapper implements ServerHttpResponse, Target {
	private final ServerHttpResponse targetResponse;

	public ServerHttpResponseWrapper(ServerHttpResponse targetResponse) {
		this.targetResponse = targetResponse;
	}

	public ServerHttpResponse getTargetResponse() {
		return targetResponse;
	}
	
	public <T> T getTarget(Class<T> targetType) {
		return XUtils.getTarget(targetResponse, targetType);
	}

	public void setContentType(MimeType contentType) {
		targetResponse.setContentType(contentType);
	}

	public void setContentLength(long contentLength) {
		targetResponse.setContentLength(contentLength);
	}

	public OutputStream getBody() throws IOException {
		return targetResponse.getBody();
	}

	public MediaType getContentType() {
		return targetResponse.getContentType();
	}

	public long getContentLength() {
		return targetResponse.getContentLength();
	}

	public void flush() throws IOException {
		targetResponse.flush();
	}

	public boolean isCommitted() {
		return targetResponse.isCommitted();
	}

	public PrintWriter getWriter() throws IOException {
		return targetResponse.getWriter();
	}

	public void addCookie(HttpCookie cookie) {
		targetResponse.addCookie(cookie);
	}

	public void addCookie(String name, String value) {
		targetResponse.addCookie(name, value);
	}

	public void sendError(int sc) throws IOException {
		targetResponse.sendError(sc);
	}

	public void sendRedirect(String location) throws IOException {
		targetResponse.sendRedirect(location);
	}

	public void sendError(int sc, String msg) throws IOException {
		targetResponse.sendError(sc, msg);
	}

	public void setStatusCode(HttpStatus httpStatus) {
		targetResponse.setStatusCode(httpStatus);
	}

	public void setStatus(int sc) {
		targetResponse.setStatus(sc);
	}

	public int getStatus() {
		return targetResponse.getStatus();
	}

	public HttpHeaders getHeaders() {
		return targetResponse.getHeaders();
	}

	public void setContentType(MediaType contentType) {
		targetResponse.setContentType(contentType);
	}

	public void close() throws IOException {
		targetResponse.close();
	}

	public String getCharacterEncoding() {
		return targetResponse.getCharacterEncoding();
	}

	public void setCharacterEncoding(String env) {
		targetResponse.setCharacterEncoding(env);
	}
}
