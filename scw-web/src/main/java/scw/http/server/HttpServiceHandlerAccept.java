package scw.http.server;

import scw.util.Accept;

public interface HttpServiceHandlerAccept extends Accept<ServerHttpRequest>{
	boolean accept(ServerHttpRequest request);
}
