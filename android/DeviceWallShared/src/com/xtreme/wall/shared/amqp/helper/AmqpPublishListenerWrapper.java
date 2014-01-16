package com.xtreme.wall.shared.amqp.helper;

import android.os.RemoteException;

import com.xtreme.wall.shared.amqp.AmqpPublishListener;

class AmqpPublishListenerWrapper extends AmqpPublishListener.Stub{

	final AmqpHelperPublishListener mAmqpHelperPublishListener;
	
	public AmqpPublishListenerWrapper (final AmqpHelperPublishListener amqpHelperPublishListener){
		mAmqpHelperPublishListener = amqpHelperPublishListener;
	}
	
	@Override
	public void onPublished() throws RemoteException {
		if (mAmqpHelperPublishListener == null)
			return;
		mAmqpHelperPublishListener.onPublished();
		
	}

	@Override
	public void onFailure(final String errorMessage) throws RemoteException {
		if (mAmqpHelperPublishListener == null)
			return;
		mAmqpHelperPublishListener.onFailure(errorMessage);
		
	}

}
