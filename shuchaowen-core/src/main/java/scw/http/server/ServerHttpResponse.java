package scw.http.server;

import java.io.Flushable;
import java.io.IOException;
import java.io.PrintWriter;

import scw.http.HttpCookie;
import scw.http.HttpHeaders;
import scw.http.HttpStatus;
import scw.net.message.OutputMessage;

public interface ServerHttpResponse extends Flushable, OutputMessage{
    void addCookie(HttpCookie cookie);
    
    void addCookie(String name, String value);

    void sendError(int sc) throws IOException;
    
    void sendError(int sc, String msg) throws IOException;

    void sendRedirect(String location) throws IOException;

    void setStatusCode(HttpStatus httpStatus);
    
    void setStatus(int sc);

    int getStatus();

    void setContentLength(long length);

    HttpHeaders getHeaders();
    
    String getRawContentType();
	
	void setContentType(String contentType);
	
	String getCharacterEncoding();
	
	void setCharacterEncoding(String charset);

	PrintWriter getWriter() throws IOException;
	
	boolean isCommitted();
}