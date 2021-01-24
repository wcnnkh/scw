package scw.rpc.http;

import java.lang.reflect.Modifier;

import scw.aop.MethodInterceptor;
import scw.aop.MethodInterceptorChain;
import scw.beans.BeanFactory;
import scw.core.reflect.MethodInvoker;
import scw.http.client.ClientHttpRequest;
import scw.http.client.ClientHttpResponse;
import scw.lang.NotSupportedException;
import scw.net.InetUtils;
import scw.net.message.converter.MessageConverter;
import scw.net.message.converter.MessageConverterFactory;

public class HttpRpcProxyMethodInterceptor implements MethodInterceptor {
	private final HttpRpcProxyRequestFactory httpRpcProxyRequestFactory;
	private final MessageConverter messageConverter;

	public HttpRpcProxyMethodInterceptor(BeanFactory beanFactory) {
		this.httpRpcProxyRequestFactory = beanFactory.isInstance(HttpRpcProxyRequestFactory.class)
				? beanFactory.getInstance(HttpRpcProxyRequestFactory.class)
				: new RestfulHttpRpcProxyRequestFactory(beanFactory.getEnvironment(), beanFactory.getEnvironment().getCharsetName());
		MessageConverterFactory messageConverter = new MessageConverterFactory();
		messageConverter.getMessageConverters().add(InetUtils.getMessageConverter());
		for(MessageConverter converter : beanFactory.getServiceLoader(MessageConverter.class)){
			messageConverter.getMessageConverters().add(converter);
		}
		this.messageConverter = messageConverter;
	}

	public HttpRpcProxyMethodInterceptor(HttpRpcProxyRequestFactory httpRpcProxyRequestFactory,
			MessageConverter messageConverter) {
		this.httpRpcProxyRequestFactory = httpRpcProxyRequestFactory;
		this.messageConverter = messageConverter;
	}

	public Object intercept(MethodInvoker invoker, Object[] args, MethodInterceptorChain filterChain) throws Throwable {
		if (Modifier.isAbstract(invoker.getMethod().getModifiers())
				|| Modifier.isInterface(invoker.getMethod().getModifiers())) {
			ClientHttpRequest request = httpRpcProxyRequestFactory.getClientHttpRequest(invoker, args);
			if (request != null) {
				ClientHttpResponse response = null;
				try {
					response = request.execute();
					if (!messageConverter.canRead(invoker.getMethod().getGenericReturnType(),
							response.getContentType())) {
						throw new NotSupportedException("type=" + invoker.getMethod().getGenericReturnType().toString()
								+ ", contentType=" + response.getContentType());
					}

					return messageConverter.read(invoker.getMethod().getGenericReturnType(), response);
				} finally {
					if (response != null) {
						response.close();
					}
				}
			}
		}
		return filterChain.intercept(invoker, args);
	}

}
