# Device Wall Programs

Authors:

[Emir Hasanbegovic](https://github.com/xtreme-emir-hasanbegovic)

[Peter Iskandar](https://github.com/xtreme-peter-iskandar)

[Devin Fallak](https://github.com/xtreme-devin-fallak)

This is the collection of server clients that run the device wall.

These connect to AMQP just as the phone clients do, but these have a special role. These applications keep track of the state of all devices and send down manage sending and receiving the appropriate commands.

Here is an example of the starter code for a client server:

```java
public class ClientServer {
	private AmqpConnectionManager mAmqpConnectionManager;

	private AmqpListener mAmqpListener = new AmqpListener() {

		@Override
		public void handleDelivery(String body) throws IOException {
			// Message has arrived
		}

		@Override
		public void onConnected() {
			// Connected start sending messages
		}

		@Override
		public void onDisconnected() {
			// Disconnected, try connecting again
		}
	};
	
	public static void main(final String[] args) {
		mAmqpConnectionManager = new AmqpConnectionManager(mAmqpListener);
	}
	
	private void sendMessageToOtherClientServer(final String serverSpecificMessageType messageType, final String message) {
		amqpConnectionManager.publishToServer(messageType, message);
	}
	
	private void sendMessageToAll(final String uniqueMessageType, final String message){
		amqpConnectionManager.publishToAll(uniqueMessageType, message);
	}
	
	private void sendMessageToIndividualClient(final long id, final String uniqueMessageType, final String message){
		amqpConnectionManager.publishToBinding(Long.toString(id),uniqueMessageType, message);
	}

	
}		
```


In this folder you will also find the DeviceWallIdentifier, which needs to be the first application running after AMQP has started and before any of the client services start.

The DeviceWallImageProgram and the Memory program also reside here as examples on how to implement a client server.