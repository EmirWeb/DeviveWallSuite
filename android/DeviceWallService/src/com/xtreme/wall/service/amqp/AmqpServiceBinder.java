package com.xtreme.wall.service.amqp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.xtreme.utils.Logger;
import com.xtreme.wall.shared.amqp.AmqpListener;
import com.xtreme.wall.shared.amqp.AmqpPublishListener;
import com.xtreme.wall.shared.amqp.RemoteAmqpServiceBinder;
import com.xtremelabs.devicewall.protocol.Protocol;

class AmqpServiceBinder extends RemoteAmqpServiceBinder.Stub {

	private final Handler mUiThreadHandler = new Handler(Looper.getMainLooper());
	private final List<AmqpListener> mAmqpListeners = new ArrayList<AmqpListener>();
	private volatile boolean mIsConnected = false;
	private final PublishListener mPublishListener;
	private String mQueueName;
	private AmqpService mAmqpService;

	public AmqpServiceBinder(AmqpService amqpService, PublishListener publishListener) {
		mAmqpService = amqpService;
		mPublishListener = publishListener;
	}

	public void setId(final Long id) {
		Logger.debug("id: " + id);
		mAmqpService.setId(id);
	}

	@Override
	public void publishToServer(String messageType, String messageJson, AmqpPublishListener listener) throws RemoteException {
		try {
			mPublishListener.onPublishToServer(formatJson(messageType, messageJson), listener);
		} catch (JSONException e) {
			listener.onFailure("JSONException caught when attempting to format the message.");
		}
	}

	@Override
	public void publishToAll(String messageType, String messageJson, AmqpPublishListener listener) throws RemoteException {
		try {
			mPublishListener.onPublishToAll(formatJson(messageType, messageJson), listener);
		} catch (JSONException e) {
			listener.onFailure("JSONException caught when attempting to format the message.");
		}
	}

	@Override
	public synchronized void registerListener(AmqpListener amqpListener) throws RemoteException {
		mAmqpListeners.add(amqpListener);
		reportStatus(amqpListener);
	}

	@Override
	public synchronized void unregisterListener(AmqpListener amqpListener) throws RemoteException {
		mAmqpListeners.remove(amqpListener);
	}

	synchronized void onConnected(final String queueName) {
		mQueueName = queueName;
		mIsConnected = true;
		reportStatus();
	}

	synchronized void onDisconnected() {
		mIsConnected = false;
		reportStatus();
	}

	private void reportStatus() {
		final boolean isConnected;
		synchronized (this) {
			isConnected = mIsConnected;
		}

		mUiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					if (isConnected) {
						for (AmqpListener listener : mAmqpListeners) {
							listener.onAmqpConnected(mQueueName);
						}
					} else {
						for (AmqpListener listener : mAmqpListeners) {
							listener.onAmqpDisconnected();
						}
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void reportStatus(final AmqpListener amqpListener) {
		Log.d("EMIR", "reportStatus");
		final boolean isConnected;
		synchronized (this) {
			isConnected = mIsConnected;
		}
		Log.d("EMIR", "isConnected" + isConnected);

		mUiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				Log.d("EMIR", "reporting isConnected" + isConnected);
				try {
					if (isConnected) {
						amqpListener.onAmqpConnected(mQueueName);
					} else {
						amqpListener.onAmqpDisconnected();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private String formatJson(String messageType, String messageJson) throws JSONException {

		final JSONObject jsonObject = new JSONObject();
		jsonObject.put(Protocol.ID, mAmqpService.getId());
		jsonObject.put(Protocol.TYPE, messageType);
		jsonObject.put(Protocol.DATA, new JSONObject(messageJson));
		return jsonObject.toString();
	}

	interface PublishListener {
		public void onPublishToServer(String json, AmqpPublishListener listener);

		public void onPublishToAll(String json, AmqpPublishListener listener);
	}

	public void onMessageReceived(final String message) {
		mUiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				List<AmqpListener> listeners;
				synchronized (AmqpServiceBinder.this) {
					listeners = new ArrayList<AmqpListener>(mAmqpListeners);
				}

				try {
					final JSONObject jsonObject;
					jsonObject = new JSONObject(message);
					String type = jsonObject.getString(Protocol.TYPE);
					String message = jsonObject.getJSONObject(Protocol.DATA).toString();

					for (AmqpListener listener : listeners) {
						try {
							listener.onMessageReceived(type, message);
						} catch (RemoteException e) {
						}
					}
				} catch (JSONException e) {
				}

			}
		});
	}

	@Override
	public long getId() throws RemoteException {
		final Long id = mAmqpService.getId();
		if (id == null)
			return -1;
		return id;
	}

	public String getQueueName() {
		return mQueueName;
	}

}
