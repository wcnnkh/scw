package scw.zookeeper;

public class ZooKeeperException extends RuntimeException {
	private static final long serialVersionUID = 5341163945147654715L;

	public ZooKeeperException(String message) {
		super(message);
	}

	public ZooKeeperException(Throwable e) {
		super(e);
	}

	public ZooKeeperException(String message, Throwable e) {
		super(message, e);
	}
}
