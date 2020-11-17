package scw.netflix.eureka.server.event;

import java.util.Objects;

import scw.application.ApplicationEvent;

public class EurekaInstanceCanceledEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;

	private String appName;

	private String serverId;

	private boolean replication;

	public EurekaInstanceCanceledEvent(Object source, String appName, String serverId, boolean replication) {
		super(source);
		this.appName = appName;
		this.serverId = serverId;
		this.replication = replication;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public boolean isReplication() {
		return replication;
	}

	public void setReplication(boolean replication) {
		this.replication = replication;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EurekaInstanceCanceledEvent that = (EurekaInstanceCanceledEvent) o;
		return Objects.equals(appName, that.appName) && Objects.equals(serverId, that.serverId)
				&& replication == that.replication;
	}

	@Override
	public int hashCode() {
		return Objects.hash(appName, serverId, replication);
	}

	@Override
	public String toString() {
		return new StringBuilder("EurekaInstanceCanceledEvent{").append("appName='").append(appName).append("', ")
				.append("serverId='").append(serverId).append("', ").append("replication=").append(replication)
				.append("}").toString();
	}
}
