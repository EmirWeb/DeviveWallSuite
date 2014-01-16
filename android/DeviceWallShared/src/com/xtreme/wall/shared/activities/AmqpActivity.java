package com.xtreme.wall.shared.activities;

import android.app.Activity;
import android.os.Bundle;

import com.xtreme.wall.shared.amqp.helper.AmqpHelper;
import com.xtreme.wall.shared.amqp.helper.AmqpHelperListener;
import com.xtreme.wall.shared.amqp.helper.AmqpHelperPublishListener;

public abstract class AmqpActivity extends Activity implements AmqpHelperListener {
	private AmqpHelper mHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mHelper = new AmqpHelper(this, this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHelper.destroy();
	}

	public void publishToServer(final String type, final String message, final AmqpHelperPublishListener amqpHelperPublishListener) {
		mHelper.publishToServer(type, message, amqpHelperPublishListener);
	}
	
	public void publishToServer(final String type, final String message) {
		publishToServer(type, message, null);
	}
	public void publishToAll(final String type, final String message, final AmqpHelperPublishListener amqpHelperPublishListener) {
		mHelper.publishToAll(type, message, amqpHelperPublishListener);
	}
	
	public void publishToAll(final String type, final String message) {
		publishToAll(type, message, null);
	}
	
	/**
	 * 
	 * @return null if no id available
	 */
	public Long getId(){
		return mHelper.getId();
	}

}
