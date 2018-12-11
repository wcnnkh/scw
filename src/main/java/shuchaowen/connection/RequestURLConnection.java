package shuchaowen.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;

import shuchaowen.common.ByteArray;
import shuchaowen.common.io.Decoder;
import shuchaowen.common.io.decoder.ByteArrayDecoder;

public class RequestURLConnection implements Request{
	private static final ByteArrayDecoder BYTE_ARRAY_DECODER = new ByteArrayDecoder();
	
	private final URLConnection urlConnection;
	
	public RequestURLConnection(URLConnection urlConnection){
		this.urlConnection = urlConnection;
	}
	
	public void connect() throws IOException {
		urlConnection.connect();
	}

	public void setConnectTimeout(int timeout) {
		urlConnection.setConnectTimeout(timeout);
	}

	public int getConnectTimeout() {
		return urlConnection.getConnectTimeout();
	}

	public void setReadTimeout(int timeout) {
		urlConnection.setReadTimeout(timeout);
	}

	public int getReadTimeout() {
		return urlConnection.getReadTimeout();
	}

	public URL getURL() {
		return urlConnection.getURL();
	}

	public int getContentLength() {
		return urlConnection.getContentLength();
	}

	public long getContentLengthLong() {
		return urlConnection.getContentLengthLong();
	}

	public String getContentType() {
		return urlConnection.getContentType();
	}

	public String getContentEncoding() {
		return urlConnection.getContentEncoding();
	}

	public long getExpiration() {
		return urlConnection.getExpiration();
	}

	public long getDate() {
		return urlConnection.getDate();
	}

	public long getLastModified() {
		return urlConnection.getLastModified();
	}

	public String getHeaderField(String name) {
		return urlConnection.getHeaderField(name);
	}

	public Map<String, List<String>> getHeaderFields() {
		return urlConnection.getHeaderFields();
	}

	public int getHeaderFieldInt(String name, int Default) {
		return urlConnection.getHeaderFieldInt(name, Default);
	}

	public long getHeaderFieldLong(String name, long Default) {
		return urlConnection.getHeaderFieldLong(name, Default);
	}

	public long getHeaderFieldDate(String name, long Default) {
		return urlConnection.getHeaderFieldDate(name, Default);
	}

	public String getHeaderFieldKey(int n) {
		return urlConnection.getHeaderFieldKey(n);
	}

	public String getHeaderField(int n) {
		return urlConnection.getHeaderField(n);
	}

	public Object getContent() throws IOException {
		return urlConnection.getContent();
	}

	@SuppressWarnings("rawtypes")
	public Object getContent(Class[] classes) throws IOException {
		return urlConnection.getContent(classes);
	}

	public Permission getPermission() throws IOException {
		return urlConnection.getPermission();
	}

	public InputStream getInputStream() throws IOException {
		return urlConnection.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return urlConnection.getOutputStream();
	}

	public void setDoInput(boolean doinput) {
		urlConnection.setDoInput(doinput);
	}

	public boolean getDoInput() {
		return urlConnection.getDoInput();
	}

	public void setDoOutput(boolean dooutput) {
		urlConnection.setDoOutput(dooutput);
	}

	public boolean getDoOutput() {
		return urlConnection.getDoOutput();
	}

	public void setAllowUserInteraction(boolean allowuserinteraction) {
		urlConnection.setAllowUserInteraction(allowuserinteraction);
	}

	public boolean getAllowUserInteraction() {
		return urlConnection.getAllowUserInteraction();
	}

	public void setUseCaches(boolean usecaches) {
		urlConnection.setUseCaches(usecaches);
	}

	public boolean getUseCaches() {
		return urlConnection.getUseCaches();
	}

	public void setIfModifiedSince(long ifmodifiedsince) {
		urlConnection.setIfModifiedSince(ifmodifiedsince);
	}

	public long getIfModifiedSince() {
		return urlConnection.getIfModifiedSince();
	}

	public boolean getDefaultUseCaches() {
		return urlConnection.getDefaultUseCaches();
	}

	public void setDefaultUseCaches(boolean defaultusecaches) {
		urlConnection.setDefaultUseCaches(defaultusecaches);
	}

	public void setRequestProperty(String key, String value) {
		urlConnection.setRequestProperty(key, value);
	}

	public void addRequestProperty(String key, String value) {
		urlConnection.addRequestProperty(key, value);
	}

	public String getRequestProperty(String key) {
		return urlConnection.getRequestProperty(key);
	}

	public Map<String, List<String>> getRequestProperties() {
		return urlConnection.getRequestProperties();
	}

	public ByteArray getResponse() throws IOException{
		return BYTE_ARRAY_DECODER.decode(getInputStream());
	}

	public <T> T execute(Decoder<T> decoder) throws IOException {
		return decoder.decode(getInputStream());
	}

	public void setRequestEntity(RequestEntity entity) throws IOException {
		if(entity != null){
			entity.write(this);
		}
	}
}
