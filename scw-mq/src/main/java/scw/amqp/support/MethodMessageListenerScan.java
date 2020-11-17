package scw.amqp.support;

import java.lang.reflect.Method;

import scw.amqp.Exchange;
import scw.amqp.QueueDeclare;
import scw.application.Application;
import scw.application.ApplicationInitialization;
import scw.beans.BeanUtils;
import scw.core.annotation.AnnotationUtils;
import scw.core.instance.annotation.Configuration;
import scw.util.ClassScanner;
import scw.value.ValueFactory;

@Configuration(order = Integer.MIN_VALUE)
public final class MethodMessageListenerScan implements ApplicationInitialization {

	@SuppressWarnings("unchecked")
	public void init(Application application) throws Throwable {
		for (Class<?> clazz : ClassScanner.getInstance()
				.getClasses(getScanAnnotationPackageName(application.getPropertyFactory()))) {
			if (scw.amqp.MessageListener.class.isAssignableFrom(clazz)) {
				MessageListener messageListener = clazz.getAnnotation(MessageListener.class);
				if (messageListener != null) {
					Exchange exchange = application.getBeanFactory().getInstance(messageListener.exchange());
					scw.amqp.MessageListener listener = application.getBeanFactory()
							.getInstance((Class<scw.amqp.MessageListener>) clazz);
					exchange.bind(messageListener.routingKey(), createQueueDeclare(messageListener), listener);
				}
			}

			for (Method method : AnnotationUtils.getAnnoationMethods(clazz, true, true, MessageListener.class)) {
				MessageListener messageListener = method.getAnnotation(MessageListener.class);
				Exchange exchange = application.getBeanFactory().getInstance(messageListener.exchange());
				exchange.bind(messageListener.routingKey(), createQueueDeclare(messageListener), application
						.getBeanFactory().getAop().getProxyMethod(application.getBeanFactory(), clazz, method));
			}
		}
	}

	private QueueDeclare createQueueDeclare(MessageListener messageListener) {
		QueueDeclare queueDeclare = new QueueDeclare(messageListener.queueName());
		queueDeclare.setDurable(messageListener.durable());
		queueDeclare.setExclusive(messageListener.exclusive());
		queueDeclare.setAutoDelete(messageListener.autoDelete());
		return queueDeclare;
	}

	public String getScanAnnotationPackageName(ValueFactory<String> propertyFactory) {
		return BeanUtils.getScanAnnotationPackageName(propertyFactory);
	}
}
