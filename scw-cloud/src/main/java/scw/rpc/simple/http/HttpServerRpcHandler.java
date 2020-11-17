package scw.rpc.simple.http;

import java.io.IOException;

import scw.beans.BeanFactory;
import scw.core.instance.annotation.Configuration;
import scw.core.utils.StringUtils;
import scw.http.HttpMethod;
import scw.http.server.HttpControllerDescriptor;
import scw.http.server.HttpServiceHandler;
import scw.http.server.HttpServiceHandlerAccept;
import scw.http.server.HttpServiceHandlerControllerDesriptor;
import scw.http.server.ServerHttpRequest;
import scw.http.server.ServerHttpResponse;
import scw.rpc.RpcService;
import scw.value.property.PropertyFactory;

@Configuration(order=Integer.MAX_VALUE)
public final class HttpServerRpcHandler implements HttpServiceHandler, HttpServiceHandlerControllerDesriptor, HttpServiceHandlerAccept {
	private final HttpControllerDescriptor controllerDescriptor;
	private final RpcService rpcService;

	public HttpServerRpcHandler(BeanFactory beanFactory, PropertyFactory propertyFactory) {
		String path = propertyFactory.getString("mvc.http.rpc-path");
		this.controllerDescriptor = new HttpControllerDescriptor(StringUtils.isEmpty(path) ? "/rpc" : path, HttpMethod.POST);
		this.rpcService = beanFactory.isInstance(RpcService.class) ? beanFactory.getInstance(RpcService.class) : null;
	}

	public HttpServerRpcHandler(RpcService rpcService, String rpcPath) {
		this.rpcService = rpcService;
		this.controllerDescriptor = new HttpControllerDescriptor(rpcPath, HttpMethod.POST);
	}
	
	public HttpControllerDescriptor getHttpControllerDescriptor() {
		return controllerDescriptor;
	}
	
	public boolean accept(ServerHttpRequest request) {
		return rpcService != null;
	}

	public void doHandle(ServerHttpRequest request, ServerHttpResponse response) throws IOException {
		rpcService.service(request, response);
	}
}
