package scw.async.beans;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import scw.async.beans.annotation.AsyncComplete;
import scw.core.instance.InstanceFactory;
import scw.core.reflect.SerializableMethodHolder;

public class AsyncInvokeInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private SerializableMethodHolder methodConfig;
	private long delayMillis;
	private TimeUnit timeUnit;
	private Object[] args;
	private String beanName;

	protected AsyncInvokeInfo() {
	};

	public AsyncInvokeInfo(AsyncComplete asyncComplete, Class<?> clz, String beanName, Method method, Object[] args) {
		this.delayMillis = asyncComplete.delayMillis();
		this.methodConfig = new SerializableMethodHolder(clz, method);
		this.timeUnit = asyncComplete.timeUnit();
		this.args = args;
		this.beanName = beanName;
	}

	public SerializableMethodHolder getMethodConfig() {
		return methodConfig;
	}

	public long getDelayMillis() {
		return delayMillis;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public Object invoke(InstanceFactory instanceFactory) throws Throwable {
		AsyncCompleteFilter.setEnable(false);
		Object bean = instanceFactory.getInstance(beanName);
		return methodConfig.invoke(bean, args);
	}
}