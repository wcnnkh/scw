package scw.servlet;

import javax.servlet.ServletContext;

import scw.application.Application;

public interface ServletContextInitialization {
	void init(Application application, ServletContext servletContext);
}
