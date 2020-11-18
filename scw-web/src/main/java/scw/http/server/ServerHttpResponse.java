package scw.http.server;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.PrintWriter;

import scw.http.HttpCookie;
import scw.http.HttpOutputMessage;
import scw.http.HttpStatus;

public interface ServerHttpResponse extends Flushable, Closeable, HttpOutputMessage{
    void addCookie(HttpCookie cookie);
    
    void addCookie(String name, String value);

    void sendError(int sc) throws IOException;
    
    void sendError(int sc, String msg) throws IOException;

    void sendRedirect(String location) throws IOException;

    void setStatusCode(HttpStatus httpStatus);
    
    void setStatus(int sc);

    int getStatus();

	PrintWriter getWriter() throws IOException;
	
	boolean isCommitted();
	
	String getCharacterEncoding();
	
	void setCharacterEncoding(String env);
}
