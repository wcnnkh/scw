package scw.http.server;

import java.io.IOException;

public class ServerHttpResponseCompleteAsyncListener implements ServerHttpAsyncListener{
	private ServerHttpResponse response;
	
	public ServerHttpResponseCompleteAsyncListener(ServerHttpResponse response){
		this.response = response;
	}
	
	public void onComplete(ServerHttpAsyncEvent event) throws IOException {
		response.close();
	}

	public void onTimeout(ServerHttpAsyncEvent event) throws IOException {
	}

	public void onError(ServerHttpAsyncEvent event) throws IOException {
	}

	public void onStartAsync(ServerHttpAsyncEvent event) throws IOException {
	}

}
