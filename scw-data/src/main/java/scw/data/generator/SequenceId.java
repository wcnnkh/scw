package scw.data.generator;

import java.io.Serializable;

public final class SequenceId implements Serializable {
	private static final long serialVersionUID = 1L;
	private long timestamp;
	private String id;

	protected SequenceId() {
	}

	public SequenceId(long timestamp, String id) {
		this.timestamp = timestamp;
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getId() {
		return id;
	}
}
