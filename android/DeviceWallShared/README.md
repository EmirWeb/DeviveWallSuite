# Device Wall Shared

Authors:

[Emir Hasanbegovic](https://github.com/xtreme-emir-hasanbegovic)

[Peter Iskandar](https://github.com/xtreme-peter-iskandar)


This project allows your application to connect to an AMQP server and easily interact with it.

Here is an exmample of what an acitivity would look like:

```java
public class MainActivity extends AmqpActivity { 
	@Override
	public void onAmqpConnected(final String queueName) {
		// Start sending messages
	}
	@Override
	public void onMessageReceived(final String messageType, final String messageJson) {
		// Handle message delivery
	}
	
	/**
	 * Send a message to all connected clients
	 */
	private void sendMessagetoAll(final String uniqueMessageType, final String message){
		publishToAll(uniqueMessageType, message, new AmqpHelperPublishListener() {
			
			@Override
			public void onPublished() {
				// Message was received
			}
			
			@Override
			public void onFailure(String errorMessage) {
				// Message was not received, please try again
			}
		});
	}
	
	/**
	 * Send a message only to server type clients
	 */
	private void sendMessagetoServers(final String uniqueMessageType, final String message){
		publishToServer(uniqueMessageType, message, new AmqpHelperPublishListener() {
			
			@Override
			public void onPublished() {
				// Message was received
			}
			
			@Override
			public void onFailure(String errorMessage) {
				// Message was not received, please try again
			}
		});
	}
}
```

