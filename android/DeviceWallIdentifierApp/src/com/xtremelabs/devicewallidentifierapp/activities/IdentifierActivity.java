package com.xtremelabs.devicewallidentifierapp.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.xtreme.wall.shared.activities.AmqpActivity;
import com.xtremelabs.devicewallidentifierapp.R;

public class IdentifierActivity extends AmqpActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		parseIntent();
		setContentView(R.layout.device_wall_identifier_activity);

		final Window w = getWindow();
		w.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onCreate(savedInstanceState);
	}

	private void parseIntent() {
	}

	@Override
	public void onMessageReceived(String messageType, String messageJson) {
	}

	@Override
	public void onAmqpDisconnected() {
		Log.d("EMIR", "DISC");
	}

	@Override
	public void onAmqpConnected(String queueName) {
		Log.d("EMIR", "CON");
		Long id = getId();
		if (id == null)
			id = (long) -2;
		final TextView ocrIdTextView = (TextView) findViewById(R.id.device_wall_identifier_activity_ocr_id);
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Helvetica.otf");
		ocrIdTextView.setTypeface(typeface);
		ocrIdTextView.setText(id.toString());
	}
}
