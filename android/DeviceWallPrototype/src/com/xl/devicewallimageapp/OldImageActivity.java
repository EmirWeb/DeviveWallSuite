package com.xl.devicewallimageapp;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.xl.devicewallprototype.R;
import com.xtreme.network.NetworkError;
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkRequestLauncher;
import com.xtreme.network.NetworkRequestListener;
import com.xtreme.network.NetworkResponse;
import com.xtreme.wall.shared.activities.AmqpActivity;
import com.xtremelabs.devicewall.protocol.AmqpConstants;
import com.xtremelabs.devicewall.protocol.Rectangle;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlMessageType;
import com.xtremelabs.devicewall.protocol.gamecontrol.request.ClientStartRequest;
import com.xtremelabs.devicewall.protocol.image.ImageMessageType;
import com.xtremelabs.devicewall.protocol.image.data.ImageData;
import com.xtremelabs.devicewall.protocol.image.request.ClientStartConfirmRequest;

public class OldImageActivity extends AmqpActivity {

	private static final String TAG = OldImageActivity.class.getSimpleName();
	private ImageView imageView;

	public static void newInstance(Activity activity) {
		Intent intent = new Intent(activity, OldImageActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("EMIR", "this: " + this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		
		imageView = (ImageView) findViewById(R.id.image);

	}

	@Override
	public void onMessageReceived(String messageType, String messageJson) {
		Log.d("EMIR", "messageJson: " + messageJson);
		GameControlMessageType gameType = GameControlMessageType.getModelType(messageType);
		if (gameType == GameControlMessageType.CLIENT_START) {
			final ClientStartRequest clientStartRequest = new Gson().fromJson(messageJson, ClientStartRequest.class);
			if (AmqpConstants.IMAGE_SERVER_APP_NAME.equals(clientStartRequest.getApp()))
				sendClientStartConfirmRequest();
			return;
		}
		
		
		if (!"image".equals(messageType))
			return;

		final ImageData imageData = new Gson().fromJson(messageJson, ImageData.class);
		NetworkRequest request = new NetworkRequest(imageData.getImageUrl(), new ImageRequestListener(imageData));
		NetworkRequestLauncher.getInstance().executeRequest(request);
		Log.d("HOLYMARY", "message received with type: " + messageType + ", and data: " + messageJson);
	}

	@Override
	public void onAmqpDisconnected() {
		Log.d("EMIR", "disconnected");
	}

	@Override
	public void onAmqpConnected(final String queueName) {
		Log.d("EMIR", queueName);
		sendClientStartConfirmRequest();
	}

	private void sendClientStartConfirmRequest() {
		final ClientStartConfirmRequest clientStartConfirmRequest = new ClientStartConfirmRequest();
		publishToAll(ImageMessageType.CONFIRM.toString(), clientStartConfirmRequest.toJson().toString());
	}
	private static Rect getAndroidRect(Rectangle rectangle) {
		int x = rectangle.getX();
		int y = rectangle.getY();
		return new Rect(x, y, x + rectangle.getWidth(), y + rectangle.getHeight());
	}

	private static int calculateSampleSizeForDimension(int imageDimension, int boundingDimension) {
		int sampleSize = 1;
		float imageWidthToBoundsWidthRatio = (float) imageDimension / (float) boundingDimension;
		sampleSize = (int) Math.ceil(imageWidthToBoundsWidthRatio);
		return sampleSize;
	}

	private final class ImageRequestListener implements NetworkRequestListener {
		private ImageData mImageData;

		public ImageRequestListener(ImageData imageData) {
			mImageData = imageData;
		}

		@Override
		public void onSuccess(NetworkResponse response) {
			final BitmapRegionDecoder decoder;
			Log.d(TAG, "received image");
			try {
				decoder = BitmapRegionDecoder.newInstance(response.getInputStream(), false);
				Options options = new Options();
				options.inSampleSize = getSampleSize(mImageData);
				final Bitmap bitmap = decoder.decodeRegion(getAndroidRect(mImageData.getShowRect()), options);
				Log.d(TAG, "Bitmap decoded? " + (bitmap == null ? "false" : "true" + " -- Bitmap dimensions: " + bitmap.getWidth() + "," + bitmap.getHeight()));
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "Setting the image bitmap...");

						imageView.setImageBitmap(bitmap);
						Log.d(TAG, "Bitmap set!");
					}
				});
			} catch (IOException e) {
				Log.w(TAG, "BitmapRegionDecoder failed! Message: " + e.getMessage());
			}
		}

		private int getSampleSize(final ImageData imageData) {
			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
			Rectangle rect = imageData.getShowRect();
			int heightSampleSize = calculateSampleSizeForDimension(rect.getHeight(), displayMetrics.heightPixels);
			int widthSampleSize = calculateSampleSizeForDimension(rect.getWidth(), displayMetrics.widthPixels);
			return Math.max(heightSampleSize, widthSampleSize);
		}

		@Override
		public void onFailure(NetworkError error) {
			Log.w(TAG, "The network request failed... " + error.getException().getMessage());
		}
	}
}
