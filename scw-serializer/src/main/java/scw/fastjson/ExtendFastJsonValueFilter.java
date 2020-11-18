package scw.fastjson;

import com.alibaba.fastjson.serializer.ValueFilter;

import scw.aop.ProxyUtils;
import scw.aop.support.FieldSetterListen;
import scw.aop.support.FieldSetterListenImpl;
import scw.cglib.proxy.Factory;
import scw.json.JsonAware;
import scw.mapper.Copy;

public class ExtendFastJsonValueFilter implements ValueFilter {
	public static final ValueFilter INSTANCE = new ExtendFastJsonValueFilter();

	private ExtendFastJsonValueFilter() {
	};

	public Object process(Object object, String name, Object value) {
		if (value == null) {
			return value;
		}
		
		if(value instanceof JsonAware){
			return ((JsonAware) value).toJsonString();
		}

		if (object instanceof Factory && "callbacks".equals(name)) {
			return null;
		}

		if (object instanceof FieldSetterListen && FieldSetterListenImpl.FIELD_SETTER_MAP_FIELD_NAME.equals(name)) {
			return null;
		}

		if (ProxyUtils.getProxyFactory().isProxy(value.getClass())) {
			return Copy.copy(ProxyUtils.getProxyFactory().getUserClass(value.getClass()), value);
		}
		
		return value;
	}
}
