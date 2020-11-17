package scw.discovery;

import java.util.List;

public interface DiscoveryClient {

	/**
	 * Gets all ServiceInstances associated with a particular name.
	 * @param serviceId The serviceId to query.
	 * @return A List of ServiceInstance.
	 */
	List<ServiceInstance> getInstances(String name);

	/**
	 * @return All known service names.
	 */
	List<String> getServices();
}
