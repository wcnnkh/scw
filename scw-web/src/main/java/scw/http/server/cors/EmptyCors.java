package scw.http.server.cors;

import scw.http.HttpHeaders;
import scw.http.server.ServerHttpRequest;

class EmptyCors extends Cors {
	@Override
	public void write(ServerHttpRequest request, HttpHeaders headers) {
		// ignore
	}
}