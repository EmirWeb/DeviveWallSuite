package com.xtreme.wall.shared.amqp;

interface AmqpListener {
	void onAmqpConnected(String queueName);
	void onAmqpDisconnected();
	void onMessageReceived(String messageType, String messageJson);
}