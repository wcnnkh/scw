package scw.http.server;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import scw.core.utils.CollectionUtils;
import scw.http.HttpMethod;
import scw.lang.AlreadyExistsException;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.net.Restful.RestfulMatchingResult;
import scw.util.comparator.CompareUtils;
import scw.web.WebUtils;

public final class HttpServiceHandlerAccessor {
	private static Logger logger = LoggerUtils.getLogger(HttpServiceHandlerAccessor.class);
	private Map<String, EnumMap<HttpMethod, HttpServiceHandlers>> handerMap = new HashMap<String, EnumMap<HttpMethod, HttpServiceHandlers>>();
	private HttpServiceHandlers handlers = new HttpServiceHandlers();

	public void bind(String path, HttpMethod httpMethod, HttpServiceHandler handler) throws AlreadyExistsException {
		if (handler == null) {
			return;
		}

		EnumMap<HttpMethod, HttpServiceHandlers> enumMap = handerMap.get(path);
		if (enumMap == null) {
			enumMap = new EnumMap<HttpMethod, HttpServiceHandlers>(HttpMethod.class);
			handerMap.put(path, enumMap);
		}

		HttpServiceHandlers handlers = enumMap.get(httpMethod);
		if (handlers == null) {
			handlers = new HttpServiceHandlers();
			enumMap.put(httpMethod, handlers);
		}

		if (!handlers.isEmpty() && !(handler instanceof HttpServiceHandlerAccept) && enumMap.containsKey(httpMethod)) {
			throw new AlreadyExistsException(httpMethod + " " + path);
		}

		handlers.add(handler);
	}

	public void bind(HttpControllerDescriptor descriptor, HttpServiceHandler handler) throws AlreadyExistsException {
		bind(descriptor.getPath(), descriptor.getMethod(), handler);
	}

	public void bind(HttpServiceHandler handler) throws AlreadyExistsException {
		if (handler == null) {
			return;
		}

		if (handler instanceof HttpServiceHandlerControllerDesriptor) {
			HttpControllerDescriptor descriptor = ((HttpServiceHandlerControllerDesriptor) handler)
					.getHttpControllerDescriptor();
			if (!descriptor.getRestful().isRestful()) {
				bind(descriptor, handler);
				return;
			}
		}

		handlers.add(handler);
	}

	public void bind(Collection<? extends HttpServiceHandler> handlers) {
		if (CollectionUtils.isEmpty(handlers)) {
			return;
		}

		for (HttpServiceHandler handler : handlers) {
			try {
				bind(handler);
			} catch (AlreadyExistsException e) {
				logger.error(e, "ignore bind");
			}
		}
	}

	public HttpServiceHandler get(ServerHttpRequest request) {
		EnumMap<HttpMethod, HttpServiceHandlers> enumMap = handerMap.get(request.getPath());
		if (enumMap != null) {
			HttpServiceHandlers handlers = enumMap.get(request.getMethod());
			if (handlers != null) {
				HttpServiceHandler handler = handlers.get(request);
				if (handler != null) {
					return handler;
				}
			}
		}

		return handlers.get(request);
	}

	private static int getOrder(HttpServiceHandler handler) {
		if (handler instanceof HttpServiceHandlerControllerDesriptor) {
			return 0;
		}

		if (handler instanceof HttpServiceHandlerAccept) {
			return 1;
		}

		return 2;
	}
	
	private static class HttpServiceHandlers {
		private Set<HttpServiceHandler> handlers = new TreeSet<HttpServiceHandler>(
				new Comparator<HttpServiceHandler>() {
					public int compare(HttpServiceHandler o1, HttpServiceHandler o2) {
						int v = CompareUtils.compare(getOrder(o1), getOrder(o2), false);
						//如果为0在TreeSet中会插入失败
						return v == 0? 1:v;
					};
				});

		public void add(HttpServiceHandler handler) {
			if(!handlers.add(handler)){
				logger.error("add handler error: {}", handler);
			}
		}

		private boolean isAccept(HttpServiceHandler handler, ServerHttpRequest request) {
			if (handler instanceof HttpServiceHandlerAccept) {
				return ((HttpServiceHandlerAccept) handler).accept(request);
			}
			return true;
		}

		public boolean isEmpty() {
			return handlers.isEmpty();
		}

		public HttpServiceHandler get(ServerHttpRequest request) {
 			for (HttpServiceHandler handler : handlers) {
				if(isAccept(handler, request)){
					if(handler instanceof HttpServiceHandlerControllerDesriptor){
						HttpControllerDescriptor descriptor = ((HttpServiceHandlerControllerDesriptor) handler)
								.getHttpControllerDescriptor();
						if (descriptor.getMethod() == null || descriptor.getMethod().equals(request.getMethod())) {
							if (descriptor.getRestful().isRestful()) {
								RestfulMatchingResult result = WebUtils.matching(descriptor.getRestful(), request);
								if (result.isSuccess()) {
									return handler;
								}
							} else {
								if (request.getPath().equals(descriptor.getPath())) {
									return handler;
								}
							}
						}
					}else{
						return handler;
					}
				}
			}
			return null;
		}
	}
}