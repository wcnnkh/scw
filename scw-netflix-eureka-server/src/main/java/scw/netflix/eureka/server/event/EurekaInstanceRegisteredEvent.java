package scw.netflix.eureka.server.event;

import java.util.Objects;

import com.netflix.appinfo.InstanceInfo;

import scw.application.ApplicationEvent;

public class EurekaInstanceRegisteredEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private InstanceInfo instanceInfo;

	private int leaseDuration;

	private boolean replication;

	public EurekaInstanceRegisteredEvent(Object source, InstanceInfo instanceInfo, int leaseDuration,
			boolean replication) {
		super(source);
		this.instanceInfo = instanceInfo;
		this.leaseDuration = leaseDuration;
		this.replication = replication;
	}

	public InstanceInfo getInstanceInfo() {
		return instanceInfo;
	}

	public void setInstanceInfo(InstanceInfo instanceInfo) {
		this.instanceInfo = instanceInfo;
	}

	public int getLeaseDuration() {
		return leaseDuration;
	}

	public void setLeaseDuration(int leaseDuration) {
		this.leaseDuration = leaseDuration;
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
		EurekaInstanceRegisteredEvent that = (EurekaInstanceRegisteredEvent) o;
		return Objects.equals(instanceInfo, that.instanceInfo) && leaseDuration == that.leaseDuration
				&& replication == that.replication;
	}

	@Override
	public int hashCode() {
		return Objects.hash(instanceInfo, leaseDuration, replication);
	}

	@Override
	public String toString() {
		return new StringBuilder("EurekaInstanceRegisteredEvent{").append("instanceInfo=").append(instanceInfo)
				.append(", ").append("leaseDuration=").append(leaseDuration).append(", ").append("replication=")
				.append(replication).append("}").toString();
	}
}
