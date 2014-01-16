package com.xtremelabs.devicewall.programs.amqp;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.xtremelabs.devicewall.protocol.AmqpConstants;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.identifier.IdentifierMessageType;
import com.xtremelabs.devicewall.protocol.identifier.request.DesktopServerIdentifierRequest;
import com.xtremelabs.devicewall.protocol.identifier.response.ServerIdentifierResponse;

public class AmqpConnectionManager {

	private Channel mChannel = null;
	private Connection mConnection = null;
	private String mQueueName;
	private Long mId;

	public AmqpConnectionManager(final AmqpListener amqpListener) throws IOException {
		final ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(AmqpConstants.DESKTOP_HOST);
		factory.setPort(AmqpConstants.PORT);

		mConnection = factory.newConnection();
		mChannel = mConnection.createChannel();

		mQueueName = mChannel.queueDeclare("", false, true, false, null).getQueue();
		mChannel.queueBind(mQueueName, AmqpConstants.SERVER_EXCHANGE, AmqpConstants.CLIENT_ROUTING_KEY);
		mChannel.queueBind(mQueueName, AmqpConstants.SERVER_EXCHANGE, AmqpConstants.SERVER_ROUTING_KEY);
		mChannel.queueBind(mQueueName, AmqpConstants.SERVER_EXCHANGE, mQueueName);

		System.out.println("New queue created: " + mQueueName);

		mChannel.basicConsume(mQueueName, false, new DefaultConsumer(mChannel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] bodyData) throws IOException {
				final long deliveryTag = envelope.getDeliveryTag();
				getChannel().basicAck(deliveryTag, true);
				final String message = new String(bodyData);
				System.out.println("AmqpConnectionManager message: " + message);

				final Protocol protocol = AmqpConstants.sIdentifierGson.fromJson(message, Protocol.class);
				if (protocol == null) {
					amqpListener.handleDelivery(new String(bodyData));
					return;
				}

				final IdentifierMessageType messageType = IdentifierMessageType.getModelType(protocol.getType());
				switch (messageType) {
				case SERVER_IDENTIFIER_RESPONSE:
					final ServerIdentifierResponse serverIdentifierResponse = (ServerIdentifierResponse) protocol.getData();
					mId = serverIdentifierResponse.getId();
					amqpListener.onConnected();
					break;
				default:
					amqpListener.handleDelivery(new String(bodyData));
				}

			}

			@Override
			public void handleConsumeOk(String consumerTag) {
				super.handleConsumeOk(consumerTag);
			}
		});

		final DesktopServerIdentifierRequest identifierData = new DesktopServerIdentifierRequest(mQueueName);
		final Protocol protocol = new Protocol(null, IdentifierMessageType.DESKTOP_SERVER_IDENTIFIER_REQUEST.toString(), identifierData);
		mChannel.basicPublish(AmqpConstants.SERVER_EXCHANGE, AmqpConstants.SERVER_ROUTING_KEY, null, protocol.toJson().toString().getBytes());
	}

	public Long getId() {
		return mId;
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

	private JsonObject getJsonObject(final String type, final String message) {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(Protocol.TYPE, type);
		jsonObject.addProperty(Protocol.ID, mId);
		final JsonParser jsonParser = new JsonParser();
		final JsonElement jsonElement = jsonParser.parse(message);
		jsonObject.add(Protocol.DATA, jsonElement);
		return jsonObject;
	}

	private boolean basicPublish(final String exchange, final String routingKey, JsonObject jsonObject) {
		System.out.println("jsonObject: " + jsonObject.toString());
		if (jsonObject == null || routingKey == null || exchange == null)
			return false;
		basicPublish(AmqpConstants.SERVER_EXCHANGE, routingKey, null, jsonObject.toString().getBytes());
		return true;
	}

	public boolean publishToServer(final String type, final String message) {
		return basicPublish(AmqpConstants.SERVER_EXCHANGE, AmqpConstants.SERVER_ROUTING_KEY, getJsonObject(type, message));
	}

	public boolean publishToAll(final String type, final String message) {
		return basicPublish(AmqpConstants.SERVER_EXCHANGE, AmqpConstants.CLIENT_ROUTING_KEY, getJsonObject(type, message));
	}
	
	public boolean publishToBinding(final String binding, final String type, final String message) {
		return basicPublish(AmqpConstants.SERVER_EXCHANGE, binding, getJsonObject(type, message));
	}

	private boolean basicPublish(String serverExchange, String queueName, BasicProperties basicProperties, byte[] bytes) {
		if (mChannel == null)
			return false;
		try {
			mChannel.basicPublish(serverExchange, queueName, basicProperties, bytes);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
