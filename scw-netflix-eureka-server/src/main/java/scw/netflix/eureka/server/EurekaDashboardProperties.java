package scw.netflix.eureka.server;

import scw.core.utils.StringUtils;

public class EurekaDashboardProperties {
	/**
	 * The path to the Eureka dashboard (relative to the servlet path). Defaults
	 * to "/".
	 */
	private String path = "/";

	/**
	 * Flag to enable the Eureka dashboard. Default true.
	 */
	private boolean enabled = true;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EurekaDashboardProperties that = (EurekaDashboardProperties) o;
		return enabled == that.enabled && StringUtils.equals(path, that.path);
	}

	@Override
	public int hashCode() {
		return (enabled ? 0 : 1) + path.hashCode();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("EurekaDashboardProperties{");
		sb.append("path='").append(path).append('\'');
		sb.append(", enabled=").append(enabled);
		sb.append('}');
		return sb.toString();
	}
}
