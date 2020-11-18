package scw.servlet;

import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import scw.application.Application;
import scw.application.ApplicationUtils;
import scw.beans.BeanLifeCycleEvent;
import scw.beans.BeanLifeCycleEvent.Step;
import scw.core.instance.InstanceUtils;
import scw.event.EventListener;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.logger.SplitLineAppend;
import scw.servlet.beans.ServletContextAware;

public class DefaultServletApplicationStartup implements ServletApplicationStartup{
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public StartUp start(ServletContext servletContext) throws ServletException {
		StartUp startUp = getStartup(servletContext);
		start(getClasses(startUp.getApplication()), servletContext, startUp.getApplication());
		return startUp;
	}
	
	protected StartUp getStartup(ServletContext servletContext) throws ServletException{
		Application application = ServletUtils.getApplication(servletContext);
		StartUp startUp;
		if(application == null){
			logger.info("Start servlet context[{}] realPath / in {}", servletContext.getContextPath(), servletContext.getRealPath("/"));
			application = new ServletApplication(servletContext);
			addServletContextPropertyFactory(servletContext, application);
			application.init();
			ServletUtils.setApplication(servletContext, application);
			startUp = new StartUp(application, true);
		}else{
			addServletContextPropertyFactory(servletContext, application);
			startUp = new StartUp(application, false);
		}
		return startUp;
	}
	
	private void addServletContextPropertyFactory(ServletContext servletContext, Application application) throws ServletException{
		if(servletContext.getAttribute(ServletContextPropertyFactory.class.getName()) != null){
			return ;
		}
		
		ServletContextPropertyFactory servletContextPropertyFactory = new ServletContextPropertyFactory(servletContext);
		servletContext.setAttribute(ServletContextPropertyFactory.class.getName(), servletContextPropertyFactory);
		application.getPropertyFactory().addLastBasePropertyFactory(servletContextPropertyFactory);
	}
	
	protected Set<Class<?>> getClasses(Application application){
		return InstanceUtils.getClasses(application.getPropertyFactory());
	}
	
	public StartUp start(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
		StartUp startUp = getStartup(servletContext);
		start(classes, servletContext, startUp.getApplication());
		return startUp;
	}

	public final boolean start(Set<Class<?>> classes, final ServletContext servletContext,
			Application application) throws ServletException {
		String nameToUse = ServletApplicationStartup.class.getName();
		if (servletContext.getAttribute(nameToUse) != null) {
			return false;
		}

		servletContext.setAttribute(nameToUse, true);
		
		application.getBeanFactory().getBeanLifeCycleEventDispatcher().registerListener(new EventListener<BeanLifeCycleEvent>() {

			public void onEvent(BeanLifeCycleEvent event) {
				if (event.getStep() == Step.BEFORE_INIT) {
					Object source = event.getSource();
					if (source != null && source instanceof ServletContextAware) {
						((ServletContextAware) source).setServletContext(servletContext);
					}
				}
			}
		});
		
		afterStarted(classes, servletContext, application);
		logger.info(new SplitLineAppend("Servlet context[{}] initialized"), servletContext.getContextPath());
		return true;
	}
	
	protected void afterStarted(Set<Class<?>> classes, ServletContext servletContext,
			Application application) throws ServletException{
		for (ServletContextInitialization initializer : ApplicationUtils
				.loadAllService(ServletContextInitialization.class, application)) {
			initializer.init(application, servletContext);
		}
	}
}
