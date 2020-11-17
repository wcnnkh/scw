package scw.servlet;

import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import scw.application.Application;

public interface ServletApplicationStartup {
	StartUp start(ServletContext servletContext) throws ServletException;
	
	StartUp start(Set<Class<?>> classes, ServletContext servletContext) throws ServletException;
	
	boolean start(Set<Class<?>> classes, ServletContext servletContext, Application application) throws ServletException;
	
	class StartUp{
		private final Application application;
		private final boolean isNew;
		
		public StartUp(Application application, boolean isNew){
			this.application = application;
			this.isNew = isNew;
		}

		public Application getApplication() {
			return application;
		}

		public boolean isNew() {
			return isNew;
		}
	}
}
