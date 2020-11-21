package scw.http.server;

import scw.core.utils.ObjectUtils;
import scw.http.HttpMethod;
import scw.net.Restful;

public final class HttpControllerDescriptor {
	private final String path;
	private final HttpMethod method;
	private final Restful restful;

	public HttpControllerDescriptor(String path, HttpMethod method) {
		this.restful = new Restful(path);
		this.method = method;
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public Restful getRestful() {
		return restful;
	}

	@Override
	public int hashCode() {
		return path.hashCode() + method.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof HttpControllerDescriptor) {
			HttpControllerDescriptor descriptor = (HttpControllerDescriptor) obj;
			if(!ObjectUtils.nullSafeEquals(descriptor.method, method)){
				return false;
			}
			
			if(restful.isRestful()){//如果当前路径是restful
				return restful.matching(descriptor.getPath()).isSuccess();
			}else if(descriptor.restful.isRestful()){
				return descriptor.restful.matching(path).isSuccess();
			}else{
				return ObjectUtils.nullSafeEquals(path, descriptor.path);
			}
		}
		return false;
	}
}
