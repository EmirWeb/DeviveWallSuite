package com.xl.devicewallprototype.activities;

import android.os.Bundle;
import android.view.View;

import com.xl.devicewallimageapp.OldImageActivity;
import com.xl.devicewallprototype.R;
import com.xtreme.wall.shared.activities.AmqpActivity;
import com.xtremelabs.devicewall.protocol.AmqpConstants;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlMessageType;
import com.xtremelabs.devicewall.protocol.gamecontrol.request.ClientStartRequest;

public class HomeActivity extends AmqpActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_activity);
	}

	public void onImageAppClicked(final View view) {
		final ClientStartRequest clientStartRequest = new ClientStartRequest(AmqpConstants.IMAGE_SERVER_APP_NAME);
		publishToAll(GameControlMessageType.CLIENT_START.toString(), clientStartRequest.toJson().toString());
	}
	@Override
	public void onMessageReceived(String messageType, String messageJson) {
	}

	
	public void onIdentifierAppClicked(final View view) {
		final ClientStartRequest clientStartRequest = new ClientStartRequest(AmqpConstants.IDENTIFIER_SERVER_APP_NAME);
		publishToAll(GameControlMessageType.SERVER_START.toString(), clientStartRequest.toJson().toString());
	}
	
	public void onMemoryButtonClicked(final View view) {
		final ClientStartRequest clientStartRequest = new ClientStartRequest(AmqpConstants.MEMORY_SERVER_APP_NAME);
		publishToAll(GameControlMessageType.CLIENT_START.toString(), clientStartRequest.toJson().toString());
	}

	@Override
	public void onAmqpDisconnected() {
	}

	@Override
	public void onAmqpConnected(String queueName) {
	}
}
