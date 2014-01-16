package com.xtreme.wall.service.amqp;

import java.io.IOException;

import android.os.AsyncTask;
import android.os.RemoteException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.xtreme.wall.shared.amqp.AmqpPublishListener;
import com.xtremelabs.devicewall.protocol.AmqpConstants;

class AmqpConnection {

	private final AmqpConnectionListener mConnectionListener;
	private volatile Connection mConnection;
	private volatile Channel mPublisherChannel;

	public AmqpConnection(AmqpConnectionListener listener) {
		mConnectionListener = listener;
	}

	/**
	 * Must not be called from the UI thread.
	 */
	public synchronized boolean connect() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(AmqpConstants.MOBILE_HOST);
		factory.setPort(AmqpConstants.PORT);

		try {
			mConnection = factory.newConnection();
			mConnection.addShutdownListener(new ShutdownListener() {
				@Override
				public void shutdownCompleted(ShutdownSignalException arg0) {
					mConnectionListener.onAmqpDisconnected();
				}
			});
			mPublisherChannel = mConnection.createChannel();
			mConnectionListener.onAmqpConnected();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			if (mConnection != null && mConnection.isOpen()) {
				try {
					mConnection.close();
				} catch (IOException e1) {
				}
			}
			mConnection = null;
			mPublisherChannel = null;
			mConnectionListener.onAmqpDisconnected();
			return false;
		}
	}

	public synchronized boolean isOpen() {
		return mConnection != null && mConnection.isOpen();
	}

	public void publish(final String exchange, final String routingKey, final BasicProperties props, final byte[] body, final AmqpPublishListener listener) {
		(new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					if (mPublisherChannel == null || !mPublisherChannel.isOpen())
						return false;
					mPublisherChannel.basicPublish(exchange, routingKey, props, body);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				try {
					if (result) {
						listener.onPublished();
					} else {
						listener.onFailure("IOException when publishing.");
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}).execute((Void[]) null);

	}

	interface AmqpConnectionListener {
		public void onAmqpConnected();

		public void onAmqpDisconnected();
	}

	public synchronized Channel getChannel() {
		try {
			if (isOpen()) {
				return mConnection.createChannel();
			}
		} catch (IOException e) {
		}
		return null;
	}

	public synchronized void close() {
		if (mConnection != null && mConnection.isOpen()) {
			try {
				mConnection.close();
			} catch (IOException e) {
			}
		}
	}
}
