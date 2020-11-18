package scw.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import scw.amqp.ExchangeDeclare;
import scw.amqp.QueueDeclare;
import scw.core.Assert;
import scw.core.utils.StringUtils;
import scw.io.NoTypeSpecifiedSerializer;

public class RabbitmqExchange extends AbstractRabbitmqExchange {
	private static final String DIX_PREFIX = "scw.dix.";
	private static final String DELAY_PREFIX = "scw.delay.";
	private static final String DELAY_EXCHANGE_TYPE = "fanout";

	private final ThreadLocal<Channel> channelThreadLocal = new ThreadLocal<Channel>();
	private final ExchangeDeclare delayExchangeDeclare;
	private final QueueDeclare delayQueueDeclare;
	private final Connection connection;
	private final ExchangeDeclare dixExchangeDeclare;
	private final QueueDeclare dixQueueDeclare;

	public RabbitmqExchange(NoTypeSpecifiedSerializer serializer, Connection connection,
			ExchangeDeclare exchangeDeclare, boolean enableLocalTransaction) {
		super(serializer, exchangeDeclare, enableLocalTransaction);
		this.connection = connection;
		checkName(exchangeDeclare.getName());

		// 延迟消息路由
		this.delayExchangeDeclare = new ExchangeDeclare(DELAY_PREFIX + getExchangeDeclare().getName());
		delayExchangeDeclare.setType(DELAY_EXCHANGE_TYPE);
		
		this.dixExchangeDeclare = new ExchangeDeclare(DIX_PREFIX + getExchangeDeclare().getName());
		this.dixExchangeDeclare.setType(DELAY_EXCHANGE_TYPE);
		this.dixQueueDeclare = new QueueDeclare(dixExchangeDeclare.getName() + ".queue");
		
		this.delayQueueDeclare = new QueueDeclare(delayExchangeDeclare.getName() + ".queue");
		// 设置延迟消息的死信队列(注意:
		// 不要给此队列绑定消费者，这样在消息过期后会进入死信队列，在死信队列中将消息发给指定队列那么就实现了消息的延迟发送)
		this.delayQueueDeclare.setArgument(X_DEAD_LETTER_EXCHANGE, dixExchangeDeclare.getName());
	}

	protected void checkName(String name) {
		Assert.requiredArgument(
				StringUtils.isNotEmpty(name) && !name.startsWith(DIX_ROUTING_KEY) && !name.startsWith(DELAY_ROUTING_KEY) && !name.startsWith(DIX_PREFIX) && !name.startsWith(DELAY_PREFIX), name);
	}

	protected Channel getChannel() throws IOException {
		Channel channel = channelThreadLocal.get();
		if (channel != null && channel.isOpen()) {
			return channel;
		}

		channel = connection.createChannel();
		channelThreadLocal.set(channel);
		return channel;
	}

	@Override
	public ExchangeDeclare getDixExchangeDeclare() {
		return dixExchangeDeclare;
	}

	@Override
	public QueueDeclare getDixQueueDeclare() {
		return dixQueueDeclare;
	}

	@Override
	public ExchangeDeclare getDelayExchangeDeclare() {
		return delayExchangeDeclare;
	}

	@Override
	public QueueDeclare getDelayQueueDeclare() {
		return delayQueueDeclare;
	}
}
