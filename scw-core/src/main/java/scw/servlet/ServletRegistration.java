package scw.servlet;

import java.util.Collection;

import javax.servlet.Servlet;

public interface ServletRegistration {
	static final String ALL = "/";
	
	Servlet getServlet();
	
	Collection<String> getUrlPatterns();
}
