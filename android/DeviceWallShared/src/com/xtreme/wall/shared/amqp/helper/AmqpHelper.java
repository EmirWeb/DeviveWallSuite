package com.xtreme.wall.shared.amqp.helper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.xtreme.wall.shared.amqp.RemoteAmqpServiceBinder;
import com.xtreme.utils.Logger;

public class AmqpHelper {
	private final Activity mActivity;
	private final AmqpListenerWrapper mListenerImplementation;
	private RemoteAmqpServiceBinder mAmqpBinder;

	public AmqpHelper(final Activity activity, final AmqpHelperListener amqpHelperListener) {
		mActivity = activity;
		mListenerImplementation = new AmqpListenerWrapper(amqpHelperListener);

		final Intent intent = new Intent("com.xtreme.wall.service.amqp.AmqpService");
		activity.bindService(intent, mAmqpServiceConnection, Context.BIND_AUTO_CREATE);
	}

	public void publishToServer(final String messageType, final String jsonMessage,
			final AmqpHelperPublishListener amqpHelperPublishListener) {
		if (mAmqpBinder == null)
			return;
		try {
			mAmqpBinder.publishToServer(messageType, jsonMessage, new AmqpPublishListenerWrapper(
					amqpHelperPublishListener));
		} catch (final RemoteException remoteException) {
			remoteException.printStackTrace();
			if (amqpHelperPublishListener == null)
				return;
			amqpHelperPublishListener.onFailure(remoteException.getMessage());
		}
	}

	public void publishToServer(final String messageType, final String jsonMessage) {
		publishToServer(messageType, jsonMessage, null);
	}

	public void publishToAll(final String messageType, final String jsonMessage) {
		publishToAll(messageType, jsonMessage, null);
	}

	public void publishToAll(final String messageType, final String jsonMessage,
			final AmqpHelperPublishListener amqpHelperPublishListener) {
		if (mAmqpBinder == null)
			return;
		try {
			mAmqpBinder.publishToAll(messageType, jsonMessage,
					new AmqpPublishListenerWrapper(amqpHelperPublishListener));
		} catch (RemoteException e) {
			Logger.ex(e);
		}
	}

	public void destroy() {
		mActivity.unbindService(mAmqpServiceConnection);
	}

	private final ServiceConnection mAmqpServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mAmqpBinder = RemoteAmqpServiceBinder.Stub.asInterface(service);
			try {
				mAmqpBinder.registerListener(mListenerImplementation);
			} catch (RemoteException e) {
				Logger.ex(e);
			}
		}
	};

	public Long getId() {
		if (mAmqpBinder == null)
			return null;

		try {
			final long id = mAmqpBinder.getId();
			if (id == -1)
				return null;
			return id;

		} catch (RemoteException e) {
			Logger.ex(e);
		}

		return null;
	}

}
