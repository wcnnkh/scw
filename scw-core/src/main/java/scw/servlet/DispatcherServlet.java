package scw.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scw.aop.annotation.AopEnable;
import scw.application.Application;
import scw.application.ApplicationAware;
import scw.core.instance.annotation.Configuration;
import scw.http.HttpStatus;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.servlet.ServletApplicationStartup.StartUp;
import scw.servlet.http.HttpServletService;

@Configuration(order = Integer.MIN_VALUE)
@AopEnable(false)
public class DispatcherServlet extends HttpServlet implements ApplicationAware {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerUtils.getLogger(DispatcherServlet.class);
	private Application application;
	private HttpServletService httpServletService;
	private boolean reference = true;
	private volatile boolean initialized = false;

	public void setApplication(Application application) {
		reference = true;
		this.application = application;
		application.getInitializationLatch().countUp();
	}

	public Application getApplication() {
		return application;
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
			//服务未初始化或初始化失败
			resp.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Uninitialized or initialization error");
			return;
		}
		getServletService().service(req, resp);
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		if (initialized) {
			return;
		}

		synchronized (this) {
			if (initialized) {
				return;
			}

			initialized = true;
			try {
				if(application == null){
					StartUp startUp = ServletUtils.getServletApplicationStartup().start(servletConfig.getServletContext());
					this.application = startUp.getApplication();
					if(startUp.isNew()){
						reference = false;
					}
				}

				if (httpServletService == null && application != null) {
					this.httpServletService = application.getBeanFactory().getInstance(HttpServletService.class);
				}
				
				if(reference){
					application.getInitializationLatch().countDown();
				}
			} catch (Throwable e) {
				logger.error(e, "Initialization error");
			}
		}
	}

	@Override
	public void destroy() {
		if (!initialized) {
			return;
		}

		synchronized (this) {
			if (!initialized) {
				return;
			}

			initialized = false;
			if (application != null && !reference) {
				application.destroy();
			}
			super.destroy();
		}
	}
}
