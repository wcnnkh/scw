package scw.http.server;

import java.io.IOException;

public interface ServerHttpAsyncListener {

	public void onComplete(ServerHttpAsyncEvent event) throws IOException;

	public void onTimeout(ServerHttpAsyncEvent event) throws IOException;

	public void onError(ServerHttpAsyncEvent event) throws IOException;

	public void onStartAsync(ServerHttpAsyncEvent event) throws IOException;
}
