package scw.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scw.application.Application;
import scw.application.CommonApplication;
import scw.beans.event.BeanEvent;
import scw.beans.event.BeanLifeCycleEvent;
import scw.beans.event.BeanLifeCycleEvent.Step;
import scw.event.EventListener;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.servlet.beans.ServletContextAware;
import scw.servlet.http.HttpServletService;

public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerUtils.getLogger(DispatcherServlet.class);
	private Application application;
	private HttpServletService httpServletService;
	private boolean reference = false;
	private ServletContext servletContext;

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.reference = true;
		this.application = application;
	}

	public HttpServletService getServletService() {
		return httpServletService;
	}

	public void setHttpServletService(HttpServletService httpServletService) {
		this.httpServletService = httpServletService;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (httpServletService == null) {
			// 未初始化或初始化错误
			resp.sendError(500, "Uninitialized or initialization error");
			return;
		}
		getServletService().service(req, resp);
	}

	@Override
	public final void init(ServletConfig servletConfig) throws ServletException {
		this.servletContext = servletConfig.getServletContext();
		logger.info("Servlet context realPath / in {}", servletContext.getRealPath("/"));
		ServletConfigPropertyFactory propertyFactory = new ServletConfigPropertyFactory(servletConfig);
		try {
			if (getApplication() == null) {
				reference = false;
				this.application = new CommonApplication(propertyFactory.getConfigXml());
			}

			getApplication().getBeanFactory().getBeanEventDispatcher().registerListener(new EventListener<BeanEvent>() {

				public void onEvent(BeanEvent event) {
					if (event instanceof BeanLifeCycleEvent) {
						if (((BeanLifeCycleEvent) event).getStep() == Step.BEFORE_INIT) {
							Object source = event.getSource();
							if (source != null && source instanceof ServletContextAware) {
								((ServletContextAware) source).setServletContext(servletContext);
							}
						}
					}
				}
			});
			getApplication().getPropertyFactory().addLastBasePropertyFactory(propertyFactory);

			if (!reference) {
				getApplication().init();
			}

			if (httpServletService == null && getApplication() != null) {
				this.httpServletService = getApplication().getBeanFactory().getInstance(HttpServletService.class);
			}
		} catch (Exception e) {
			logger.error(e, "初始化异常");
		}
	}

	@Override
	public void destroy() {
		if (!reference && getApplication() != null) {
			getApplication().destroy();
		}
		super.destroy();
	}
}
