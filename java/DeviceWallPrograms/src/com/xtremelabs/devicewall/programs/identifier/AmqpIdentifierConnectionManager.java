package com.xtremelabs.devicewall.programs.identifier;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.xtremelabs.devicewall.programs.amqp.AmqpListener;
import com.xtremelabs.devicewall.protocol.AmqpConstants;

public class AmqpIdentifierConnectionManager {

	private Channel mChannel = null;
	private Connection mConnection = null;

	public AmqpIdentifierConnectionManager(final AmqpListener amqpListener) throws IOException {
		final ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(AmqpConstants.DESKTOP_HOST);
		factory.setPort(AmqpConstants.PORT);

		mConnection = factory.newConnection();
		mChannel = mConnection.createChannel();
		mChannel.exchangeDeclare(AmqpConstants.SERVER_EXCHANGE, "direct", true);

		mChannel.queueDeclare(AmqpConstants.SERVER_QUEUE_NAME, false, false, false, null);
		mChannel.queueBind(AmqpConstants.SERVER_QUEUE_NAME, AmqpConstants.SERVER_EXCHANGE, AmqpConstants.SERVER_ROUTING_KEY);

		mChannel.basicConsume(AmqpConstants.SERVER_QUEUE_NAME, false, new DefaultConsumer(mChannel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] bodyData) throws IOException {
				final long deliveryTag = envelope.getDeliveryTag();
				getChannel().basicAck(deliveryTag, true);
				amqpListener.handleDelivery(new String(bodyData));
			}

			@Override
			public void handleConsumeOk(String consumerTag) {
				super.handleConsumeOk(consumerTag);
			}
		});

	}

	public void destroy() {
		if (mChannel != null) {
			try {
				mChannel.close();
			} catch (final IOException ioException) {
				ioException.printStackTrace();
			}
		}
		if (mConnection != null) {
			try {
				mConnection.close();
			} catch (final IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	public boolean basicPublish(String serverExchange, String binding, BasicProperties basicProperties, byte[] bytes) {
		System.out.println("serverExchange: " + serverExchange + " binding: "+ binding + " basicProperties: " + basicProperties + " bytes: " + new String(bytes));
		System.out.println("mChannel: " + mChannel);
		if (mChannel == null)
			return false;
		try {
			mChannel.basicPublish(serverExchange, binding, basicProperties, bytes);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
