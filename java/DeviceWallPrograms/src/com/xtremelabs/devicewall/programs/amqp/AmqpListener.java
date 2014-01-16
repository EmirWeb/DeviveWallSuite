package com.xtremelabs.devicewall.programs.amqp;

import java.io.IOException;

public interface AmqpListener {
	public void handleDelivery(String message) throws IOException;
	public void onConnected();
	public void onDisconnected();
}
