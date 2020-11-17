package scw.discovery.event;

import scw.application.ApplicationEvent;

public class HeartbeatEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private final long count;

	public HeartbeatEvent(Object source, long count) {
		super(source);
		this.count = count;
	}

	public long getCount() {
		return count;
	}
}
