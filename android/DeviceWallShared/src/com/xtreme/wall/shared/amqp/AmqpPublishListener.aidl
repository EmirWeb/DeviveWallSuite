package com.xtreme.wall.shared.amqp;

interface AmqpPublishListener {
	void onPublished();
	void onFailure(String errorMessage);
}