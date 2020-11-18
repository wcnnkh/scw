package scw.amqp.support;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import scw.amqp.Exchange;
import scw.amqp.ExchangeDeclare;
import scw.amqp.Message;
import scw.amqp.MessageListener;
import scw.amqp.MessageProperties;
import scw.amqp.MethodMessage;
import scw.amqp.QueueDeclare;
import scw.aop.MethodInvoker;
import scw.beans.Init;
import scw.core.utils.StringUtils;
import scw.io.support.LocalLogger.Record;
import scw.io.NoTypeSpecifiedSerializer;
import scw.io.support.SystemLocalLogger;
import scw.json.JSONUtils;
import scw.lang.NestedExceptionUtils;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.transaction.DefaultTransactionDefinition;
import scw.transaction.DefaultTransactionLifeCycle;
import scw.transaction.Transaction;
import scw.transaction.TransactionLifeCycle;
import scw.transaction.TransactionManager;

/**
 * 此实现通过重试来保证消息的可靠消费
 * 
 * @author shuchaowen
 *
 */
public abstract class AbstractExchange implements Exchange, Init {
	protected final Logger logger = LoggerUtils.getLogger(getClass());
	private final NoTypeSpecifiedSerializer serializer;
	private final ExchangeDeclare exchangeDeclare;
	private SystemLocalLogger<MessageLog> systemLocalLogger;
	private final boolean enableLocalRetryPush;

	/**
	 * @param serializer
	 * @param exchangeDeclare
	 * @param enableLocalRetryPush
	 *            是否开启本地重试, 此实现保证消息一定发送成功，但不保证消息多次发送，为保证生产者性能，消息的幂等性需要消费端自行处理
	 */
	public AbstractExchange(NoTypeSpecifiedSerializer serializer, ExchangeDeclare exchangeDeclare,
			boolean enableLocalRetryPush) {
		this.serializer = serializer;
		this.exchangeDeclare = exchangeDeclare;
		this.enableLocalRetryPush = enableLocalRetryPush;
			this.systemLocalLogger = new SystemLocalLogger<AbstractExchange.MessageLog>(
					"scw_rabbitmq_" + exchangeDeclare.getName());
	}

	public final NoTypeSpecifiedSerializer getSerializer() {
		return serializer;
	}

	public final ExchangeDeclare getExchangeDeclare() {
		return exchangeDeclare;
	}

	public void init() throws Exception {
		// 将因意外发送失败的消息补发
		Enumeration<Record<MessageLog>> enumeration = systemLocalLogger.enumeration();
		while (enumeration.hasMoreElements()) {
			Record<MessageLog> record = enumeration.nextElement();
			basicPublish(record.getData());
			systemLocalLogger.getLocalLogger().delete(record.getId());
		}
	}

	public final void bind(String routingKey, QueueDeclare queueDeclare, MethodInvoker invoker) {
		bind(routingKey, queueDeclare, new MethodMessageListener(invoker));
	}

	public final void bind(String routingKey, QueueDeclare queueDeclare, MessageListener messageListener) {
		logger.info("add message listener：{}, routingKey={}, queueDeclare={}", messageListener, routingKey,
				queueDeclare);
		try {
			bindInternal(routingKey, queueDeclare, new MessageListenerInternal(messageListener));
		} catch (IOException e) {
			logger.error(e, "bind error, Try again in 10 seconds");
			try {
				Thread.sleep(10000);
				bind(routingKey, queueDeclare, messageListener);
			} catch (InterruptedException e1) {
			}
		}
	}

	protected abstract void bindInternal(String routingKey, QueueDeclare queueDeclare, MessageListener messageListener)
			throws IOException;

	public final void push(String routingKey, Message message) {
		push(routingKey, message, message.getBody());
	}

	public final void push(String routingKey, MethodMessage methodMessage) {
		byte[] body = serializer.serialize(methodMessage.getArgs());
		push(routingKey, methodMessage, body);
	}

	private final class MethodMessageListener implements MessageListener {
		private MethodInvoker invoker;

		public MethodMessageListener(MethodInvoker invoker) {
			this.invoker = invoker;
		}

		public void onMessage(String exchange, String routingKey, Message message) throws IOException {
			try {
				Object[] args = serializer.deserialize(message.getBody());
				invoker.invoke(args);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String toString() {
			return invoker.toString();
		}
	}

	protected long getDefaultRetryDelay() {
		return 10000;
	}

	/**
	 * 0表示一直重试
	 * 
	 * @return
	 */
	protected int getMaxRetryCount() {
		return 0;
	}

	/**
	 * 转发消息时会调用此方法
	 * 
	 * @param routingKey
	 * @param messageProperties
	 * @param body
	 */
	protected void forwardPush(String routingKey, MessageProperties messageProperties, byte[] body) throws IOException {
		basicPublish(new MessageLog(routingKey, messageProperties, body));
	}

	/**
	 * 失败重试时会调用此方法
	 * 
	 * @param routingKey
	 * @param messageProperties
	 * @param body
	 */
	protected void retryPush(String routingKey, MessageProperties messageProperties, byte[] body) throws IOException {
		basicPublish(new MessageLog(routingKey, messageProperties, body));
	}

	protected class MessageListenerInternal implements MessageListener {
		private final MessageListener messageListener;

		public MessageListenerInternal(MessageListener messageListener) {
			this.messageListener = messageListener;
		}

		public void onMessage(String exchange, String routingKey, Message message) throws IOException {
			String routingKeyToUse = message.getPublishRoutingKey();
			if (StringUtils.isEmpty(routingKeyToUse)) {
				routingKeyToUse = routingKey;
			}

			if (message.getDelay() > 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("delay message forward exchange:{}, routingKey:{}, message:{}", exchange, routingKey,
							JSONUtils.toJSONString(message));
				}

				message.setDelay(0, TimeUnit.SECONDS);
				forwardPush(routingKeyToUse, message, message.getBody());
				return;
			}

			if (messageListener == null) {
				// 不应该到这里, 转为延迟消息重试
				int delay = 1;
				TimeUnit delayTimeUnit = TimeUnit.SECONDS;
				logger.error("retry delay: {}, Unable to consume exchange:{}, routingKey:{}, message:{}", delayTimeUnit.toMillis(delay), exchange, routingKeyToUse,
						JSONUtils.toJSONString(message));
				message.setDelay(delay, delayTimeUnit);
				retryPush(routingKeyToUse, message, message.getBody());
				return ;
			}

			if (logger.isDebugEnabled()) {
				logger.debug("handleDelivery exchange:{}, routingKey:{}, message:{}", exchange, routingKeyToUse,
						JSONUtils.toJSONString(message));
			}

			Transaction transaction = TransactionManager.getTransaction(new DefaultTransactionDefinition());
			try {
				messageListener.onMessage(exchange, routingKeyToUse, message);
				TransactionManager.commit(transaction);
			} catch (Throwable e) {
				TransactionManager.rollback(transaction);
				message.incrRetryCount();
				long retryDelay = message.getRetryDelay();
				if (retryDelay == 0) {
					retryDelay = getDefaultRetryDelay();
				}

				if (retryDelay != 0) {
					double retryDelayMultiple = message.getRetryDelayMultiple();
					if (retryDelayMultiple > 0) {
						retryDelay = (long) (retryDelay * retryDelayMultiple * message.getRetryCount());
					}
				}

				int maxRetryCount = message.getMaxRetryCount();
				if (maxRetryCount == 0) {
					maxRetryCount = getMaxRetryCount();
				}

				if (retryDelay < 0 || maxRetryCount < 0
						|| (maxRetryCount > 0 && message.getRetryCount() > maxRetryCount)) {// 不重试
					logger.error(NestedExceptionUtils.getRootCause(e),
							"Don't try again: exchange={}, routingKey={}, message={}", exchange, routingKeyToUse,
							JSONUtils.toJSONString(message));
				} else {
					logger.error(NestedExceptionUtils.getRootCause(e),
							"retry delay: {}, exchange={}, routingKey={}, message={}", retryDelay, exchange,
							routingKeyToUse, JSONUtils.toJSONString(message));
					message.setDelay(retryDelay, TimeUnit.MILLISECONDS);
					retryPush(routingKeyToUse, message, message.getBody());
				}
			}
		}
	}

	public final void push(String routingKey, MessageProperties messageProperties, byte[] body) {
		messageProperties.setPublishRoutingKey(routingKey);
		final MessageLog log = new MessageLog(routingKey, messageProperties, body);
		if (TransactionManager.hasTransaction()) {
			TransactionLifeCycle transactionLifeCycle;
			// 是否开启本地事务
			Boolean enableLocalRetryPush = messageProperties.isEnableLocalRetryPush();
			if (enableLocalRetryPush == null) {
				enableLocalRetryPush = this.enableLocalRetryPush;
			}

			if (enableLocalRetryPush) {
				final Record<MessageLog> record = systemLocalLogger.create(log);
				transactionLifeCycle = new DefaultTransactionLifeCycle() {
					@Override
					public void afterCommit() {
						if (systemLocalLogger.getLocalLogger().isExist(record.getId())) {
							try {
								basicPublish(log);
							} catch (IOException e) {
								logger.error(e, JSONUtils.toJSONString(log));
							}
						}
						systemLocalLogger.getLocalLogger().delete(record.getId());
					}

					@Override
					public void afterRollback() {
						systemLocalLogger.getLocalLogger().delete(record.getId());
					}
				};
			} else {
				transactionLifeCycle = new DefaultTransactionLifeCycle() {
					public void afterCommit() {
						try {
							basicPublish(log);
						} catch (IOException e) {
							logger.error(e, JSONUtils.toJSONString(log));
						}
					};
				};
			}
			TransactionManager.transactionLifeCycle(transactionLifeCycle);
		} else {
			// 不存在事务，直接发送
			try {
				basicPublish(log);
			} catch (IOException e) {
				throw new RuntimeException(JSONUtils.toJSONString(log), e);
			}
		}
	}

	public abstract void basicPublish(MessageLog messageLog) throws IOException;

	public static final class MessageLog implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String routingKey;
		private final MessageProperties messageProperties;
		private final byte[] body;

		public MessageLog(String routingKey, MessageProperties messageProperties, byte[] body) {
			this.routingKey = routingKey;
			this.messageProperties = messageProperties;
			this.body = body;
		}

		public String getRoutingKey() {
			return routingKey;
		}

		public MessageProperties getMessageProperties() {
			return messageProperties;
		}

		public byte[] getBody() {
			return body;
		}
	}
}
