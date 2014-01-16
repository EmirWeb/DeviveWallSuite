package com.xtreme.wall.shared.amqp.helper;

public interface AmqpHelperPublishListener {

	public void onPublished();

	public void onFailure(final String errorMessage);

}
