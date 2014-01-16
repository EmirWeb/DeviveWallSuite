package com.xtreme.wall.service.amqp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

class AmqpAutoConnectUtil {
	private static final int RETRY_PERIOD_IN_MILLIS = 500; // Half a second.
	private final Handler mUiThreadHandler = new Handler(Looper.getMainLooper());
	private final AmqpConnection mConnection;
	private boolean mEnabled = false;

	public AmqpAutoConnectUtil(AmqpConnection connection) {
		mConnection = connection;
	}

	public synchronized void autoConnect() {
		if (!mEnabled && !mConnection.isOpen()) {
			mEnabled = true;
			attemptConnectionInBackground();
		}
	}

	private void scheduleRetry() {
		mUiThreadHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				attemptConnectionInBackground();
			}
		}, RETRY_PERIOD_IN_MILLIS);
	}

	private void attemptConnectionInBackground() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				attemptConnection();
				return null;
			}
		}.execute();
	}

	private synchronized void attemptConnection() {
		if (mEnabled) {
			if (mConnection.isOpen()) {
				mEnabled = false;
				return;
			}

			boolean connected = mConnection.connect();
			if (connected) {
				mEnabled = false;
			} else {
				scheduleRetry();
			}
		}
	}
}
