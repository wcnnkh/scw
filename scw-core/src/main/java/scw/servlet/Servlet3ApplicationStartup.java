package scw.servlet;

import java.util.Collection;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import scw.application.Application;
import scw.application.ApplicationUtils;
import scw.core.utils.CollectionUtils;
import scw.util.XUtils;

public class Servlet3ApplicationStartup extends DefaultServletApplicationStartup{
	@Override
	protected void afterStarted(Set<Class<?>> classes,
			ServletContext servletContext, Application application)
			throws ServletException {
		int i = 0;
		for (FilterRegistration registration : ApplicationUtils.loadAllService(FilterRegistration.class, application)) {
			Filter filter = registration.getFilter();
			if (filter == null) {
				continue;
			}
			
			String name = XUtils.getName(filter, FilterRegistration.class.getSimpleName() + (i++));
			Dynamic dynamic = servletContext.addFilter(name, filter);
			Collection<String> urlPatterns = registration.getUrlPatterns();
			dynamic.addMappingForUrlPatterns(null, true, CollectionUtils.isEmpty(urlPatterns)? new String[]{FilterRegistration.ALL}:urlPatterns.toArray(new String[0]));
			dynamic.setAsyncSupported(true);
		}
		
		for(scw.servlet.ServletRegistration registration : ApplicationUtils.loadAllService(scw.servlet.ServletRegistration.class, application)){
			Servlet servlet = registration.getServlet();
			if(servlet == null){
				continue;
			}
			
			String name = XUtils.getName(servlet, ServletRegistration.class.getSimpleName() + (i++));
			ServletRegistration.Dynamic dynamic = servletContext.addServlet(name, servlet);
			Collection<String> urlPatterns = registration.getUrlPatterns();
			dynamic.addMapping(CollectionUtils.isEmpty(urlPatterns)? new String[]{scw.servlet.ServletRegistration.ALL}:urlPatterns.toArray(new String[0]));
			dynamic.setAsyncSupported(true);
			dynamic.setLoadOnStartup(1);
		}
		
		for(ServletContextListener listener : ApplicationUtils.loadAllService(ServletContextListener.class, application)){
			servletContext.addListener(listener);
		}
		
		for (ServletContainerInitializer initializer : ApplicationUtils.loadAllService(ServletContainerInitializer.class, application)) {
			initializer.onStartup(classes, servletContext);
		}
		
		super.afterStarted(classes, servletContext, application);
	}
}
