package com.xtreme.wall.shared.amqp.helper;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.xtreme.wall.shared.amqp.AmqpListener;

class AmqpListenerWrapper extends AmqpListener.Stub {

	private final AmqpHelperListener mAmqpHelperListener;

	public AmqpListenerWrapper(final AmqpHelperListener amqpHelperListener) {
		mAmqpHelperListener = amqpHelperListener;
	}

	@Override
	public void onAmqpConnected(String queueName) throws RemoteException {
		Log.d("EMIR", "queueName: " + queueName + " mAmqpHelperListener: " + mAmqpHelperListener);
		if (mAmqpHelperListener == null)
			return;
		mAmqpHelperListener.onAmqpConnected(queueName);
	}

	@Override
	public void onAmqpDisconnected() throws RemoteException {
		if (mAmqpHelperListener == null)
			return;
		mAmqpHelperListener.onAmqpDisconnected();
	}

	@Override
	public void onMessageReceived(final String messageType, final String messageJson) throws RemoteException {
		if (mAmqpHelperListener == null)
			return;
		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (mAmqpHelperListener == null)
					return;
				mAmqpHelperListener.onMessageReceived(messageType, messageJson);
			}
		});

	}

}
