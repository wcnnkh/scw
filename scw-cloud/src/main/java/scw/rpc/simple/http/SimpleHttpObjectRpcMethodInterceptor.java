package scw.rpc.simple.http;

import java.lang.reflect.Modifier;

import scw.aop.MethodInterceptor;
import scw.aop.MethodInterceptorChain;
import scw.aop.MethodInvoker;
import scw.http.HttpUtils;
import scw.http.MediaType;
import scw.io.Bytes;
import scw.io.Serializer;
import scw.rpc.simple.SimpleObjectRequestMessage;
import scw.rpc.simple.SimpleResponseMessage;
import scw.security.SignatureUtils;

public class SimpleHttpObjectRpcMethodInterceptor implements MethodInterceptor {
	private boolean responseThrowable;
	private Serializer serializer;
	private String sign;
	private String host;

	public SimpleHttpObjectRpcMethodInterceptor(Serializer serializer, String sign, boolean responseThrowable,
			String host) {
		this.responseThrowable = responseThrowable;
		this.serializer = serializer;
		this.sign = sign;
		this.host = host;
	}

	public Object intercept(MethodInvoker invoker, Object[] args, MethodInterceptorChain filterChain) throws Throwable {
		if (!Modifier.isAbstract(invoker.getMethod().getModifiers())) {
			return filterChain.intercept(invoker, args);
		}

		long cts = System.currentTimeMillis();
		final SimpleObjectRequestMessage requestMessage = new SimpleObjectRequestMessage(invoker.getSourceClass(),
				invoker.getMethod(), args);
		requestMessage.setAttribute("t", cts);
		requestMessage.setAttribute("sign",
				(SignatureUtils.byte2hex(SignatureUtils.md5(Bytes.string2bytes(cts + sign)))));
		requestMessage.setAttribute("responseThrowable", responseThrowable);
		byte[] body = serializer.serialize(requestMessage);
		byte[] response = HttpUtils.getHttpClient().post(byte[].class, host, body, MediaType.APPLICATION_OCTET_STREAM).getBody();
		Object obj = serializer.deserialize(response);
		if (obj instanceof SimpleResponseMessage) {
			if (((SimpleResponseMessage) obj).getError() != null) {
				throw ((SimpleResponseMessage) obj).getError();
			}
			return ((SimpleResponseMessage) obj).getResponse();
		} else {
			return obj;
		}
	}

}
