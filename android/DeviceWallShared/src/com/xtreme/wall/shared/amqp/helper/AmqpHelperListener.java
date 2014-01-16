package com.xtreme.wall.shared.amqp.helper;


public interface AmqpHelperListener {
	public void onMessageReceived(final String messageType, final String messageJson);

	public void onAmqpDisconnected();

	public void onAmqpConnected(final String queueName);
}
