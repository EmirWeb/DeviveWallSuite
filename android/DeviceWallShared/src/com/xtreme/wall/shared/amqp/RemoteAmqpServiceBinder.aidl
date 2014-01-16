package com.xtreme.wall.shared.amqp;

import com.xtreme.wall.shared.amqp.AmqpListener;
import com.xtreme.wall.shared.amqp.AmqpPublishListener;

interface RemoteAmqpServiceBinder {
	void publishToServer(String messageType, String jsonMessage, AmqpPublishListener listener);
	void publishToAll(String messageType, String jsonMessage, AmqpPublishListener listener);
	void registerListener(AmqpListener amqpListener);
	void unregisterListener(AmqpListener amqpListener);
	long getId();
}
