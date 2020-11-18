package scw.http.server;

public class ServerHttpAsyncEvent {
	private final ServerHttpAsyncControl serverHttpAsyncControl;
	private final Throwable throwable;

	public ServerHttpAsyncEvent(ServerHttpAsyncControl serverHttpAsyncControl, Throwable throwable) {
		this.serverHttpAsyncControl = serverHttpAsyncControl;
		this.throwable = throwable;
	}

	public ServerHttpAsyncControl getAsyncControl() {
		return serverHttpAsyncControl;
	}

	public Throwable getThrowable() {
		return throwable;
	}
}
