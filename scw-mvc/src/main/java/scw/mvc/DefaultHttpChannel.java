package scw.mvc;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import scw.beans.BeanFactory;
import scw.beans.Destroy;
import scw.beans.ExtendBeanFactory;
import scw.compatible.CompatibleUtils;
import scw.core.Constants;
import scw.core.ResolvableType;
import scw.core.instance.NoArgsInstanceFactory;
import scw.core.parameter.AbstractParameterFactory;
import scw.core.parameter.ParameterDescriptor;
import scw.core.parameter.ParameterDescriptors;
import scw.core.parameter.ParameterUtils;
import scw.core.parameter.RenameParameterDescriptor;
import scw.core.parameter.annotation.ParameterName;
import scw.core.reflect.ReflectionUtils;
import scw.core.utils.CollectionUtils;
import scw.core.utils.NumberUtils;
import scw.core.utils.StringUtils;
import scw.core.utils.TypeUtils;
import scw.http.HttpMethod;
import scw.http.server.ServerHttpRequest;
import scw.http.server.ServerHttpResponse;
import scw.json.JSONSupport;
import scw.lang.ParameterException;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.mapper.MapperUtils;
import scw.mapper.Mapping;
import scw.mapper.support.AbstractParameterMapping;
import scw.mvc.annotation.Attribute;
import scw.mvc.annotation.BigDecimalMultiply;
import scw.mvc.annotation.DateFormat;
import scw.mvc.annotation.IP;
import scw.mvc.annotation.RequestBean;
import scw.mvc.annotation.RequestBody;
import scw.mvc.parameter.RequestBodyParse;
import scw.mvc.security.UserSessionFactoryAdapter;
import scw.mvc.security.UserSessionResolver;
import scw.net.RestfulParameterMapAware;
import scw.security.session.Session;
import scw.security.session.UserSession;
import scw.security.session.UserSessionFactory;
import scw.util.MultiValueMap;
import scw.util.Target;
import scw.util.XUtils;
import scw.value.AbstractStringValue;
import scw.value.EmptyValue;
import scw.value.StringValue;
import scw.value.Value;
import scw.web.WebUtils;

public class DefaultHttpChannel extends AbstractParameterFactory implements HttpChannel, Destroy, Target {
	private static Logger logger = LoggerUtils.getLogger(DefaultHttpChannel.class);
	private final long createTime;
	private final JSONSupport jsonSupport;
	private boolean completed = false;
	private final ServerHttpRequest request;
	private final ServerHttpResponse response;
	private final ExtendBeanFactory extendBeanFactory;

	public DefaultHttpChannel(BeanFactory beanFactory, JSONSupport jsonSupport, ServerHttpRequest request,
			ServerHttpResponse response) {
		this.createTime = System.currentTimeMillis();
		this.jsonSupport = jsonSupport;
		this.request = request;
		this.response = response;
		this.extendBeanFactory = new ExtendBeanFactory(this, beanFactory);
	}

	public final JSONSupport getJsonSupport() {
		return jsonSupport;
	}

	public boolean isCompleted() {
		return completed;
	}

	public NoArgsInstanceFactory getInstanceFactory() {
		return extendBeanFactory;
	}

	public void destroy() throws Exception {
		if (isCompleted()) {
			return;
		}

		completed = true;
		if (logger.isTraceEnabled()) {
			logger.trace("destroy channel: {}", toString());
		}

		extendBeanFactory.destroy();
	}

	public final long getCreateTime() {
		return createTime;
	}

	public final Value getValue(String name) {
		return getValue(name, EmptyValue.INSTANCE);
	}

	public final Value getValue(String name, Value defaultValue) {
		return new RequestValue(name, defaultValue == null ? EmptyValue.INSTANCE : defaultValue);
	}

	protected Value parseValue(String value) {
		return new StringValue(value);
	}

	@SuppressWarnings("unchecked")
	protected final <E> E[] parseArray(MultiValueMap<String, String> parameterMap, String name,
			Class<? extends E> type) {
		List<String> values = request.getParameterMap().get(name);
		if (CollectionUtils.isEmpty(values)) {
			return (E[]) Array.newInstance(type, 0);
		}

		Object array = Array.newInstance(type, values.size());
		for (int i = 0, len = values.size(); i < len; i++) {
			Value value = parseValue(values.get(i));
			Array.set(array, i, value.getAsObject(type));
		}

		return (E[]) array;
	}

	@SuppressWarnings("unchecked")
	public <E> E[] getArray(String name, Class<? extends E> type) {
		Value[] values = WebUtils.getParameterValues(getRequest(), name);
		Object array = Array.newInstance(type, values.length);
		for (int i = 0, len = values.length; i < len; i++) {
			Array.set(array, i, values[i].getAsObject(type));
		}
		return (E[]) array;
	}

	@Override
	protected boolean isAccept(ParameterDescriptors parameterDescriptors, ParameterDescriptor parameterDescriptor,
			int index) {
		return getParameter(parameterDescriptor) != null;
	}

	@Override
	protected Object getParameter(ParameterDescriptors parameterDescriptors, ParameterDescriptor parameterDescriptor,
			int index) throws Exception {
		return getParameter(parameterDescriptor);
	}

	protected Object getParameterInternal(ParameterDescriptor parameterDescriptor) {
		Value defaultValue = ParameterUtils.getDefaultValue(parameterDescriptor);
		BigDecimalMultiply bigDecimalMultiply = parameterDescriptor.getAnnotatedElement()
				.getAnnotation(BigDecimalMultiply.class);
		if (bigDecimalMultiply != null) {
			return bigDecimalMultiply(parameterDescriptor, bigDecimalMultiply, defaultValue);
		}

		DateFormat dateFormat = parameterDescriptor.getAnnotatedElement().getAnnotation(DateFormat.class);
		if (dateFormat != null) {
			return dateFormat(dateFormat, parameterDescriptor, defaultValue);
		}

		Value value = getValue(parameterDescriptor.getName(), defaultValue);
		return value.getAsObject(parameterDescriptor.getGenericType());
	}

	public <T> T getTarget(Class<T> targetType) {
		T target = XUtils.getTarget(getRequest(), targetType);
		if (target != null) {
			return target;
		}

		target = XUtils.getTarget(getResponse(), targetType);
		if (target != null) {
			return target;
		}
		return null;
	}

	public Session getSession(boolean create) {
		return getRequest().getSession(create);
	}

	public Object getParameter(ParameterDescriptor parameterDescriptor) {
		Object target = XUtils.getTarget(this, parameterDescriptor.getType());
		if (target != null) {
			return target;
		}

		if (parameterDescriptor.getType().isInstance(this)) {
			return this;
		}

		if (UserSession.class == parameterDescriptor.getType()) {
			ResolvableType resolvableType = ResolvableType.forType(parameterDescriptor.getGenericType());
			return getUserSession(resolvableType.getGeneric(0).getRawClass());
		}

		if (Session.class == parameterDescriptor.getType()) {
			return getSession(false);
		}

		if (parameterDescriptor.getAnnotatedElement().getAnnotation(IP.class) != null) {
			return getRequest().getIp();
		}

		Attribute attribute = parameterDescriptor.getAnnotatedElement().getAnnotation(Attribute.class);
		if (attribute != null) {
			return getRequest().getAttribute(attribute.value());
		}

		RequestBody requestBody = parameterDescriptor.getAnnotatedElement().getAnnotation(RequestBody.class);
		if (requestBody != null) {
			RequestBodyParse requestBodyParse = getInstanceFactory().getInstance(requestBody.value());
			try {
				return requestBodyParse.requestBodyParse(this, getJsonSupport(), parameterDescriptor);
			} catch (Exception e) {
				throw ParameterException.createError(parameterDescriptor.getName(), e);
			}
		}

		RequestBean requestBean = parameterDescriptor.getAnnotatedElement().getAnnotation(RequestBean.class);
		if (requestBean != null) {
			return StringUtils.isEmpty(requestBean.value())
					? getInstanceFactory().getInstance(parameterDescriptor.getType().getName())
					: getInstanceFactory().getInstance(requestBean.value());
		}

		ParameterName parameterName = parameterDescriptor.getAnnotatedElement().getAnnotation(ParameterName.class);
		return getParameterInternal(
				(parameterName == null || StringUtils.isEmpty(parameterName.value())) ? parameterDescriptor
						: new RenameParameterDescriptor(parameterDescriptor, parameterDescriptor.getName()));
	}

	private Object dateFormat(DateFormat dateFormat, ParameterDescriptor parameterDescriptor, Value defaultValue) {
		String value = getValue(parameterDescriptor.getName(), defaultValue).getAsString();
		if (TypeUtils.isString(parameterDescriptor.getType())) {
			return StringUtils.isEmpty(value) ? value
					: new SimpleDateFormat(dateFormat.value()).format(StringUtils.parseLong(value));
		}

		long time = 0;
		if (StringUtils.isNotEmpty(value)) {
			SimpleDateFormat format = new SimpleDateFormat(dateFormat.value());
			try {
				time = format.parse(value).getTime();
			} catch (ParseException e) {
				logger.error("{} format error value:{}", dateFormat.value(), value);
			}
		}

		if (Date.class.isAssignableFrom(parameterDescriptor.getType())) {
			return new Date(time);
		} else if (TypeUtils.isLong(parameterDescriptor.getType())) {
			return time;
		} else if (TypeUtils.isInt(parameterDescriptor.getType())) {
			return time / 1000;
		} else if (Calendar.class == parameterDescriptor.getType()) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(time);
			return calendar;
		}
		throw new ParameterException("not support type [" + parameterDescriptor.getType() + "]");
	}

	private Object bigDecimalMultiply(ParameterDescriptor parameterDescriptor, BigDecimalMultiply bigDecimalMultiply,
			Value defaultValue) {
		String value = getValue(parameterDescriptor.getName(), defaultValue).getAsString();
		if (StringUtils.isEmpty(value)) {
			return castBigDecimal(null, parameterDescriptor.getType());
		}

		BigDecimal a = new BigDecimal(value);
		BigDecimal b = new BigDecimal(bigDecimalMultiply.value());
		return castBigDecimal(a.multiply(b), parameterDescriptor.getType());
	}

	private Object castBigDecimal(BigDecimal bigDecimal, Class<?> type) {
		if (type == BigDecimal.class) {
			return bigDecimal;
		}

		if (type == BigInteger.class) {
			return bigDecimal == null ? null : bigDecimal.toBigInteger();
		}

		if (bigDecimal == null) {
			return type.isPrimitive() ? 0 : null;
		}

		return NumberUtils.converPrimitive(bigDecimal, type);
	}

	protected final String decodeGETParameter(String value) {
		if (StringUtils.containsChinese(value)) {
			return value;
		}

		try {
			return new String(CompatibleUtils.getStringOperations().getBytes(value, Constants.ISO_8859_1),
					request.getCharacterEncoding());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return value;
		}
	}

	protected String getStringValue(String name) {
		Value value = WebUtils.getParameter(getRequest(), name);
		String v = value.getAsString();
		if (v == null && request instanceof RestfulParameterMapAware) {
			v = request.getRestfulParameterMap().getFirst(name);
		}

		if (v != null && HttpMethod.GET == request.getMethod()) {
			v = decodeGETParameter(v);
		}
		return v;
	}

	public ServerHttpRequest getRequest() {
		return request;
	}

	public ServerHttpResponse getResponse() {
		return response;
	}

	@Override
	public String toString() {
		return getRequest().toString();
	}

	public UserSessionResolver getUserSessionResolver() {
		return getService(UserSessionResolver.class);
	}

	@SuppressWarnings("unchecked")
	public <T> T getUid(Class<T> type) {
		T uid = (T) getRequest().getAttribute(UID_ATTRIBUTE);
		if (uid != null) {
			return uid;
		}

		UserSessionResolver userSessionResolver = getUserSessionResolver();
		if (userSessionResolver == null) {
			return null;
		}

		uid = userSessionResolver.getUid(this, type);
		if (uid != null) {
			getRequest().setAttribute(UID_ATTRIBUTE, uid);
		}
		return uid;
	}

	public String getSessionId() {
		String sessionId = (String) getRequest().getAttribute(SESSIONID_ATTRIBUTE);
		if (sessionId != null) {
			return sessionId;
		}

		UserSessionResolver userSessionResolver = getUserSessionResolver();
		if (userSessionResolver == null) {
			return null;
		}

		sessionId = userSessionResolver.getSessionId(this);
		if (sessionId != null) {
			getRequest().setAttribute(SESSIONID_ATTRIBUTE, sessionId);
		}
		return sessionId;
	}

	private final class RequestValue extends AbstractStringValue {
		private final String name;

		public RequestValue(String name, Value defaultValue) {
			super(defaultValue);
			this.name = name;
		}

		public String getAsString() {
			return DefaultHttpChannel.this.getStringValue(name);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected <T> T getAsObjectNotSupport(Class<? extends T> type) {
			if (type.isArray()) {
				return (T) getArray(name, type.getComponentType());
			}

			// 不可以被实例化且不存在无参的构造方法
			if (!ReflectionUtils.isInstance(type, true)) {
				return getInstanceFactory().getInstance(type);
			}

			Value value = WebUtils.getParameter(getRequest(), name);
			if (!value.isEmpty()) {
				return value.getAsObject(type);
			}

			Mapping mapping = new AbstractParameterMapping(true, name) {

				@Override
				protected Object getValue(ParameterDescriptor parameterDescriptor) {
					return getParameter(parameterDescriptor);
				}
			};
			try {
				return MapperUtils.getMapper().mapping(type, null, mapping);
			} catch (Exception e) {
				throw new ParameterException("name=" + name + ", type=" + type, e);
			}
		}

		@Override
		protected Object getAsObjectNotSupport(Type type) {
			Value value = WebUtils.getParameter(getRequest(), name);
			if (!value.isEmpty()) {
				return value.getAsObject(type);
			}

			return getAsObjectNotSupport(TypeUtils.toClass(type));
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getService(Class<T> type) {
		T service = (T) getRequest().getAttribute(type.getName());
		if (service != null) {
			return service;
		}

		if (extendBeanFactory.isInstance(type)) {
			service = extendBeanFactory.getInstance(type);
		}

		if (service != null) {
			getRequest().setAttribute(type.getName(), service);
		}
		return service;
	}

	@SuppressWarnings("unchecked")
	public <T> UserSessionFactory<T> getUserSessionFactory(Class<T> type) {
		UserSessionFactory<T> userSessionFactory = (UserSessionFactory<T>) getRequest()
				.getAttribute(UserSessionFactory.class.getName());
		if (userSessionFactory != null) {
			return userSessionFactory;
		}

		UserSessionFactoryAdapter userSessionFactoryAdapter = getService(UserSessionFactoryAdapter.class);
		if (userSessionFactoryAdapter != null) {
			userSessionFactory = userSessionFactoryAdapter.getUserSessionFactory(type);
		}

		if (userSessionFactory == null && extendBeanFactory.isInstance(UserSessionFactory.class)) {
			userSessionFactory = extendBeanFactory.getInstance(UserSessionFactory.class);
		}

		if (userSessionFactory != null) {
			getRequest().setAttribute(UserSessionFactory.class.getName(), userSessionFactory);
		}

		if (userSessionFactory == null) {
			logger.error("Not support user session factory: {}", this.toString());
		}
		return userSessionFactory;
	}

	@SuppressWarnings("unchecked")
	public <T> UserSession<T> getUserSession(Class<T> type) {
		UserSession<T> userSession = (UserSession<T>) getRequest().getAttribute(UserSession.class.getName());
		if (userSession != null) {
			return userSession;
		}

		T uid = getUid(type);
		if (uid == null) {
			return null;
		}

		String sessionId = getSessionId();
		if (StringUtils.isEmpty(sessionId)) {
			return null;
		}

		UserSessionFactory<T> userSessionFactory = getUserSessionFactory(type);
		if (userSessionFactory == null) {
			return null;
		}

		userSession = userSessionFactory.getUserSession(uid, sessionId);
		if (userSession != null) {
			getRequest().setAttribute(UserSession.class.getName(), userSession);
		}
		return userSession;
	}

	public <T> UserSession<T> createUserSession(Class<T> type, T uid, String sessionId) {
		if(uid == null || type == null || StringUtils.isEmpty(sessionId)){
			throw new IllegalArgumentException();
		}
		
		UserSessionFactory<T> userSessionFactory = getUserSessionFactory(type);
		if (userSessionFactory == null) {
			return null;
		}

		UserSession<T> userSession = userSessionFactory.getUserSession(uid, sessionId, true);
		if (userSession != null) {
			getRequest().setAttribute(UID_ATTRIBUTE, uid);
			getRequest().setAttribute(SESSIONID_ATTRIBUTE, sessionId);
			getRequest().setAttribute(UserSession.class.getName(), userSession);
		}
		return userSession;
	}
}
