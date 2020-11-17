package scw.netflix.eureka.server;

public class InstanceRegistryProperties {
	/**
	 * Prefix for Eureka instance registry properties.
	 */
	public static final String PREFIX = "eureka.instance.registry";

	/*
	 * Default number of expected client, defaults to 1. Setting
	 * expectedNumberOfClientsSendingRenews to non-zero to ensure that even an
	 * isolated server can adjust its eviction policy to the number of
	 * registrations (when it's zero, even a successful registration won't reset
	 * the rate threshold in InstanceRegistry.register()).
	 */
	// compatibility
	private int expectedNumberOfClientsSendingRenews = 1;

	/**
	 * Value used in determining when leases are cancelled, default to 1 for
	 * standalone. Should be set to 0 for peer replicated eurekas
	 */
	private int defaultOpenForTrafficCount = 1;

	public int getExpectedNumberOfClientsSendingRenews() {
		return expectedNumberOfClientsSendingRenews;
	}

	public void setExpectedNumberOfClientsSendingRenews(int expectedNumberOfClientsSendingRenews) {
		this.expectedNumberOfClientsSendingRenews = expectedNumberOfClientsSendingRenews;
	}

	public int getExpectedNumberOfRenewsPerMin() {
		return getExpectedNumberOfClientsSendingRenews();
	}

	public void setExpectedNumberOfRenewsPerMin(int expectedNumberOfRenewsPerMin) {
		setExpectedNumberOfClientsSendingRenews(expectedNumberOfRenewsPerMin);
	}

	public int getDefaultOpenForTrafficCount() {
		return defaultOpenForTrafficCount;
	}

	public void setDefaultOpenForTrafficCount(int defaultOpenForTrafficCount) {
		this.defaultOpenForTrafficCount = defaultOpenForTrafficCount;
	}
}
