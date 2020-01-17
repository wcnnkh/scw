package scw.mvc.rpc.http;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import scw.aop.Filter;
import scw.aop.FilterChain;
import scw.aop.Invoker;
import scw.core.Constants;
import scw.core.PropertyFactory;
import scw.core.annotation.ParameterName;
import scw.core.instance.InstanceFactory;
import scw.core.utils.ArrayUtils;
import scw.core.utils.StringUtils;
import scw.lang.Nullable;
import scw.mvc.rpc.RpcConstants;
import scw.mvc.rpc.support.ObjectRpcMessageConvert;
import scw.net.http.HttpHeaders;
import scw.serializer.Serializer;

public class HttpRestfulRpcProxy implements Filter, RpcConstants {
	private static final String[] DEFAULT_SHARE_HEADERS = new String[] { HttpHeaders.CONTENT_TYPE,
			HttpHeaders.COOKIE, HttpHeaders.X_FORWARDED_FOR };
	private HttpRpcProxy httpRpcProxy;

	public HttpRestfulRpcProxy(InstanceFactory instanceFactory, PropertyFactory propertyFactory,
			@Nullable String host, Serializer serializer,
			@ParameterName(RPC_HTTP_CHARSET_NAME) @Nullable String charsetName,
			@ParameterName(RPC_HTTP_MVC_SHARE_HEADERS) @Nullable String[] shareHeaders,
			@ParameterName(RPC_HTTP_MVC_SHARE_APPEND_HEADERS) @Nullable String[] appendShareHeaders) {
		String cName = StringUtils.isEmpty(charsetName) ? Constants.DEFAULT_CHARSET_NAME : charsetName;

		Set<String> shareHeaderSet = new HashSet<String>();
		if (ArrayUtils.isEmpty(shareHeaders)) {
			shareHeaderSet.addAll(Arrays.asList(DEFAULT_SHARE_HEADERS));
		} else {
			shareHeaderSet.addAll(Arrays.asList(shareHeaders));
		}

		if (!ArrayUtils.isEmpty(appendShareHeaders)) {
			shareHeaderSet.addAll(Arrays.asList(appendShareHeaders));
		}

		HttpRpcRequestFactory httpRpcRequestFactory = new HttpRestfulRpcRequestFactory(propertyFactory, host, cName,
				shareHeaderSet.toArray(new String[0]));
		this.httpRpcProxy = new HttpRpcProxy(propertyFactory, instanceFactory, httpRpcRequestFactory);
		this.httpRpcProxy.add(new ObjectRpcMessageConvert(serializer, cName));
	}

	public Object doFilter(Invoker invoker, Object proxy, Class<?> targetClass, Method method, Object[] args, FilterChain filterChain)
			throws Throwable {
		return httpRpcProxy.doFilter(invoker, proxy, targetClass, method, args, filterChain);
	}
}