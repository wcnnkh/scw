package scw.registry;

public class ServiceRegistryException extends RuntimeException {
	private static final long serialVersionUID = 5341163945147654715L;

	public ServiceRegistryException(String message) {
		super(message);
	}

	public ServiceRegistryException(Throwable e) {
		super(e);
	}

	public ServiceRegistryException(String message, Throwable e) {
		super(message, e);
	}
}