package scw.netflix.eureka.server;

import javax.servlet.ServletContext;

import scw.application.Application;
import scw.beans.Destroy;
import scw.beans.annotation.Autowired;
import scw.core.instance.annotation.Configuration;
import scw.servlet.ServletContextInitialization;

/**
 * 自动启动eureka服务端
 * 
 * @author shuchaowen
 *
 */
@Configuration(order = Integer.MIN_VALUE)
public class EurekaServerInitializer implements ServletContextInitialization, Destroy {
	@Autowired
	private EurekaServerBootstrap eurekaServerBootstrap;
	private ServletContext servletContext;

	@Override
	public void init(Application application, ServletContext servletContext) {
		eurekaServerBootstrap.contextInitialized(servletContext);
		this.servletContext = servletContext;
	}

	@Override
	public void destroy() throws Throwable {
		if (servletContext == null) {
			return;
		}

		eurekaServerBootstrap.contextDestroyed(servletContext);
	}
}
