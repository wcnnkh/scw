package scw.discovery;

import java.net.URI;
import java.util.Map;

/**
 * Represents an instance of a service in a discovery system.
 * @author shuchaowen
 *
 */
public interface ServiceInstance {
	/**
	 * @return The unique instance ID as registered.
	 */
	String getId();
	
	/**
	 * @return The service ID as registered.
	 */
	String getName();
	
	/**
	 * @return The hostname of the registered service instance.
	 */
	String getHost();

	/**
	 * @return The port of the registered service instance.
	 */
	int getPort();

	/**
	 * @return Whether the port of the registered service instance uses HTTPS.
	 */
	boolean isSecure();
	
	/**
	 * @return The service URI address.
	 */
	URI getUri();

	/**
	 * @return The key / value pair metadata associated with the service instance.
	 */
	Map<String, String> getMetadata();
}
