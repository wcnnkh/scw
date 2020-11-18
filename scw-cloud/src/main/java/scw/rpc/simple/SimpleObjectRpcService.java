package scw.rpc.simple;

import java.io.IOException;

import scw.core.instance.InstanceFactory;
import scw.core.instance.annotation.Configuration;
import scw.core.parameter.annotation.ParameterName;
import scw.core.utils.StringUtils;
import scw.core.utils.XTime;
import scw.io.Bytes;
import scw.io.Serializer;
import scw.io.SerializerUtils;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.net.MimeTypeUtils;
import scw.net.message.InputMessage;
import scw.net.message.OutputMessage;
import scw.rpc.RpcConstants;
import scw.rpc.RpcService;
import scw.rpc.RpcServiceException;
import scw.security.SignatureUtils;

@Configuration(order = Integer.MIN_VALUE, value = RpcService.class)
public final class SimpleObjectRpcService implements RpcService, RpcConstants {
	private static Logger logger = LoggerUtils.getLogger(SimpleObjectRpcService.class);
	private final String sign;
	private final InstanceFactory instanceFactory;
	private Serializer serializer = SerializerUtils.DEFAULT_SERIALIZER;

	public SimpleObjectRpcService(InstanceFactory instanceFactory, @ParameterName(RPC_HTTP_SIGN_NAME) String sign) {
		this.instanceFactory = instanceFactory;
		this.sign = sign;
	}

	public Serializer getSerializer() {
		return serializer;
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public Object request(SimpleObjectRequestMessage requestMessage) throws Throwable {
		if (!rpcAuthorize(requestMessage)) {
			throw new RuntimeException("RPC验证失败");
		}

		Object instance = instanceFactory.getInstance(requestMessage.getSourceClass());
		return requestMessage.invoke(instance);
	}

	private void response(OutputMessage outputMessage, SimpleResponseMessage responseMessage)
			throws RpcServiceException {
		outputMessage.setContentType(MimeTypeUtils.APPLICATION_OCTET_STREAM);
		try {
			serializer.serialize(outputMessage.getBody(), responseMessage);
		} catch (IOException e) {
			logger.error(e, "rpc返回失败");
		}
	}

	public void service(InputMessage inputMessage, OutputMessage outputMessage) throws RpcServiceException {
		outputMessage.setContentType(MimeTypeUtils.APPLICATION_OCTET_STREAM);
		SimpleResponseMessage resonseMessage = new SimpleResponseMessage();
		SimpleObjectRequestMessage requestMessage;
		try {
			requestMessage = serializer.deserialize(inputMessage.getBody());
			resonseMessage.setRequestMessage(requestMessage);
		} catch (Exception e1) {
			logger.error(e1, "序列化失败");
			resonseMessage.setError(e1);
			response(outputMessage, resonseMessage);
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug(requestMessage);
		}

		try {
			resonseMessage.setResponse(request(requestMessage));
		} catch (Throwable e) {
			logger.error(e, requestMessage);
			resonseMessage.setError(e);
		}
		response(outputMessage, resonseMessage);
	}

	/**
	 * RPC权限验证
	 * 
	 * @param objectRpcRequestMessage
	 */
	private boolean rpcAuthorize(SimpleObjectRequestMessage objectRpcRequestMessage) {
		if (StringUtils.isEmpty(sign)) {// 不校验签名
			logger.warn("RPC Signature verification not opened(未开启签名验证)");
			return true;
		}

		long t = (Long) objectRpcRequestMessage.getAttribute(RPC_REQUEST_MESSAGE_T);
		if (t < System.currentTimeMillis() - XTime.ONE_MINUTE) {// 如果超过10秒失效
			return false;
		}

		return (SignatureUtils.byte2hex(SignatureUtils.md5(Bytes.string2bytes(t + sign))))
				.equals((String) objectRpcRequestMessage.getAttribute(RPC_REQUEST_MESSAGE_SIGN));
	}
}
