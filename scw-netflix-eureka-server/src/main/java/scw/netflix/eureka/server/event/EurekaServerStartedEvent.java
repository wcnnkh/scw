package scw.netflix.eureka.server.event;

import com.netflix.eureka.EurekaServerConfig;

import scw.application.ApplicationEvent;

public class EurekaServerStartedEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * @param eurekaServerConfig
	 *            {@link EurekaServerConfig} event source
	 */
	public EurekaServerStartedEvent(EurekaServerConfig eurekaServerConfig) {
		super(eurekaServerConfig);
	}

}
