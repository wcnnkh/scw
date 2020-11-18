package scw.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.Channel;

import scw.amqp.ExchangeDeclare;
import scw.amqp.MessageListener;
import scw.amqp.QueueDeclare;
import scw.amqp.support.AbstractExchange;
import scw.compatible.CompatibleUtils;
import scw.core.Constants;
import scw.io.NoTypeSpecifiedSerializer;
import scw.json.JSONUtils;

public abstract class AbstractRabbitmqExchange extends AbstractExchange {
	static final String DIX_ROUTING_KEY = "scw.dix.routingKey";
	static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	static final String DELAY_ROUTING_KEY = "scw.delay.routingKey";

	public AbstractRabbitmqExchange(NoTypeSpecifiedSerializer serializer, ExchangeDeclare exchangeDeclare,
			boolean enableLocalTransaction) {
		super(serializer, exchangeDeclare, enableLocalTransaction);
		checkName(exchangeDeclare.getName());
	}

	protected abstract void checkName(String name);

	public abstract ExchangeDeclare getDixExchangeDeclare();

	public abstract QueueDeclare getDixQueueDeclare();

	public abstract ExchangeDeclare getDelayExchangeDeclare();

	public abstract QueueDeclare getDelayQueueDeclare();

	@Override
	public void init() throws Exception {
		// 声明路由
		declare(getExchangeDeclare(), null);

		// 声明死信路由和队列
		declare(getDixExchangeDeclare(), getDixQueueDeclare());
		queueBind(getDixExchangeDeclare(), getDixQueueDeclare(), DIX_ROUTING_KEY, new MessageListenerInternal(null));

		// 延迟消息队列
		QueueDeclare delayQueueDeclare = getDelayQueueDeclare();
		// 设置延迟消息的死信队列(注意:
		// 不要给此队列绑定消费者，这样在消息过期后会进入死信队列，在死信队列中将消息发给指定队列那么就实现了消息的延迟发送)
		delayQueueDeclare.setArgument(X_DEAD_LETTER_EXCHANGE, getDixExchangeDeclare().getName());

		// 声明延迟消息路由和队列
		declare(getDelayExchangeDeclare(), delayQueueDeclare);
		queueBind(getDelayExchangeDeclare(), delayQueueDeclare, DELAY_ROUTING_KEY, null);
		super.init();
	}

	protected abstract Channel getChannel() throws IOException;

	private final void declare(ExchangeDeclare exchangeDeclare, QueueDeclare queueDeclare) throws IOException {
		if (exchangeDeclare != null) {
			getChannel().exchangeDeclare(exchangeDeclare.getName(), exchangeDeclare.getType(),
					exchangeDeclare.isDurable(), exchangeDeclare.isAutoDelete(), exchangeDeclare.isInternal(),
					exchangeDeclare.getArguments());
		}
		if (queueDeclare != null) {
			getChannel().queueDeclare(queueDeclare.getName(), queueDeclare.isDurable(), queueDeclare.isExclusive(),
					queueDeclare.isAutoDelete(), queueDeclare.getArguments());
		}
	}

	private final void queueBind(ExchangeDeclare exchangeDeclare, QueueDeclare queueDeclare, String routingKey,
			MessageListener messageListener) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("bind exchangeDeclare:{}, queueDeclare:{}, routingKey:{}", exchangeDeclare, queueDeclare,
					routingKey);
		}

		getChannel().queueBind(queueDeclare.getName(), exchangeDeclare.getName(), routingKey);
		if (messageListener != null) {
			getChannel().basicConsume(queueDeclare.getName(), false,
					new RabbitmqMessageListener(getChannel(), messageListener, isMultiple()));
		}
	}

	@Override
	protected void bindInternal(String routingKey, QueueDeclare queueDeclare, MessageListener messageListener)
			throws IOException {
		checkName(queueDeclare.getName());
		declare(null, queueDeclare);
		queueBind(getExchangeDeclare(), queueDeclare, routingKey, messageListener);
	}

	@Override
	public void basicPublish(MessageLog message) throws IOException {
		ExchangeDeclare exchangeDeclare = message.getMessageProperties().getDelay() > 0 ? getDelayExchangeDeclare()
				: getExchangeDeclare();
		if (logger.isDebugEnabled()) {
			logger.debug("push exchange={}, routingKey={}, properties={}, body={}", exchangeDeclare.getName(),
					message.getRoutingKey(), JSONUtils.toJSONString(message.getMessageProperties()),
					CompatibleUtils.getStringOperations().createString(message.getBody(), Constants.DEFAULT_CHARSET));
		}

		if (message.getMessageProperties().getDeliveryMode() == null) {
			message.getMessageProperties().setDeliveryMode(2);// 消息持久化
		}

		Channel channel = getChannel();
		channel.basicPublish(exchangeDeclare.getName(), message.getRoutingKey(),
				RabbitmqUitls.toBasicProperties(message.getMessageProperties()), message.getBody());
	}

	protected boolean isMultiple() {
		return false;
	}
}
