package scw.result;

import scw.aop.annotation.AopEnable;
import scw.core.GlobalPropertyFactory;
import scw.event.support.DynamicValue;

@AopEnable(false)
public class ResultFactory {
	static final DynamicValue<String> DEFAULT_ERROR_MESSAGE = GlobalPropertyFactory.getInstance()
			.getDynamicValue("result.error.msg", String.class, "系统错误");
	static final DynamicValue<String> AUTHORIZATION_FAILURE_MESSAGE = GlobalPropertyFactory.getInstance()
			.getDynamicValue("result.authorization.failure.msg", String.class, "登录状态已过期");
	static final DynamicValue<String> PARAMETER_ERROR_MESSAGE = GlobalPropertyFactory.getInstance()
			.getDynamicValue("result.parameter.error.msg", String.class, "参数错误");

	private final ResultMessageFactory resultMessageFactory;
	private final int defaultErrorCode;
	private final int successCode;
	private final int authorizationFailureCode;
	private final int parameterErrorCode;

	public ResultFactory() {
		this(null);
	}

	public ResultFactory(ResultMessageFactory resultMessageFactory) {
		this(resultMessageFactory, 1, 0, -1, 2);
	}

	public ResultFactory(ResultMessageFactory resultMessageFactory, int defaultErrorCode, int successCode,
			int authorizationFailureCode, int parameterErrorCode) {
		this.resultMessageFactory = resultMessageFactory;
		this.defaultErrorCode = defaultErrorCode;
		this.successCode = successCode;
		this.authorizationFailureCode = authorizationFailureCode;
		this.parameterErrorCode = parameterErrorCode;
	}

	public final <T> DataResult<T> success() {
		return success(null);
	}

	public final <T> DataResult<T> error(long code) {
		String msg = getMsg(code);
		return error(code, msg == null ? DEFAULT_ERROR_MESSAGE.getValue() : msg);
	}

	public final <T> DataResult<T> error(String msg) {
		return error(getDefaultErrorCode(), msg);
	}

	public final <T> DataResult<T> error() {
		long code = getDefaultErrorCode();
		String msg = getMsg(code);
		return error(code, msg == null ? DEFAULT_ERROR_MESSAGE.getValue() : msg);
	}

	public final <T> DataResult<T> authorizationFailure() {
		long code = getAuthorizationFailureCode();
		String msg = getMsg(code);
		return error(code, msg == null ? AUTHORIZATION_FAILURE_MESSAGE.getValue() : msg);
	}

	public final <T> DataResult<T> parameterError() {
		long code = getParameterErrorCode();
		String msg = getMsg(code);
		return error(code, msg == null ? PARAMETER_ERROR_MESSAGE.getValue() : msg);
	}

	public final <T> DataResult<T> error(Result result) {
		return error(result.getCode(), result.getMsg());
	}

	public <T> DataResult<T> success(T data) {
		long code = getSuccessCode();
		DataResult<T> dataResult = new DataResult<T>(true, code);
		dataResult.setData(data);
		dataResult.setMsg(getMsg(code));
		return dataResult;
	}

	public final <T> DataResult<T> error(long code, String msg) {
		if (code == getSuccessCode()) {
			throw new IllegalArgumentException("Error code cannot be " + getSuccessCode());
		}
		return error(code, msg, null);
	}

	public <T> DataResult<T> error(long code, String msg, Object data) {
		DataResult<T> dataResult = new DataResult<T>(false, code);
		dataResult.setMsg(msg);
		dataResult.setData(data);
		return dataResult;
	}

	public final <T> DataResult<T> error(String msg, Object data) {
		return error(getDefaultErrorCode(), msg, data);
	}

	public final long getDefaultErrorCode() {
		return defaultErrorCode;
	}

	public final long getSuccessCode() {
		return successCode;
	}

	public final long getAuthorizationFailureCode() {
		return authorizationFailureCode;
	}

	public final long getParameterErrorCode() {
		return parameterErrorCode;
	}

	public final ResultMessageFactory getResultMessageFactory() {
		return resultMessageFactory;
	}

	protected String getMsg(long code) {
		return resultMessageFactory == null ? null : resultMessageFactory.getMessage(code);
	}
}
