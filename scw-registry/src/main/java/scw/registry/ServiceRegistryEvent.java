package scw.registry;

import scw.event.support.BasicTypeEvent;
import scw.event.support.EventType;

public class ServiceRegistryEvent extends BasicTypeEvent {
	private final ServiceRegistryInstance serviceRegistryInstance;

	public ServiceRegistryEvent(EventType eventType, ServiceRegistryInstance serviceRegistryInstance) {
		super(eventType);
		this.serviceRegistryInstance = serviceRegistryInstance;
	}

	public ServiceRegistryInstance getInstanceInfo() {
		return serviceRegistryInstance;
	}
}
