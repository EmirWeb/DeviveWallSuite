package com.xtremelabs.devicewallmemorygame;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtreme.wall.shared.activities.AmqpActivity;
import com.xtremelabs.devicewall.protocol.AmqpConstants;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlDeserializer;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlMessageType;
import com.xtremelabs.devicewall.protocol.gamecontrol.request.ClientStartRequest;
import com.xtremelabs.devicewall.protocol.gamecontrol.response.ServerStartResponse;
import com.xtremelabs.devicewall.protocol.memory.MemoryAssign;
import com.xtremelabs.devicewall.protocol.memory.MemoryClick;
import com.xtremelabs.devicewall.protocol.memory.MemoryConfirm;
import com.xtremelabs.devicewall.protocol.memory.MemoryDeserializer;
import com.xtremelabs.devicewall.protocol.memory.MemoryFlip;
import com.xtremelabs.devicewall.protocol.memory.MemoryMessageType;
import com.xtremelabs.devicewallmemorygame.AnimationFactory.FlipDirection;

public class MainActivity extends AmqpActivity {

	private static final int cardFaceResourceIDs[] = { R.drawable.i1, R.drawable.i2, R.drawable.i3, R.drawable.i4, R.drawable.i5, R.drawable.i6, R.drawable.i7, R.drawable.i8, R.drawable.i9, R.drawable.i10, R.drawable.i11, R.drawable.i12 };

	private ViewFlipper viewFlipper;
	private ViewFlipper viewFlipper2;
	private ImageView winView;

	private boolean mShowingFace;
	private boolean mShowingFace2;

	private Gson mGson;

	private boolean mIsTablet;
	private static final String TAG = "MemGame";

	private static Gson sGameControlGson;
	static {
		final GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Protocol.class, new GameControlDeserializer());
		sGameControlGson = builder.create();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Protocol.class, new MemoryDeserializer());
		mGson = builder.create();
		init();

	}

	private void init() {
		setContentView(R.layout.activity_main);

		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		viewFlipper2 = (ViewFlipper) findViewById(R.id.viewFlipper2);
		winView = (ImageView) findViewById(R.id.winView);
		winView.setVisibility(View.GONE);

		/**
		 * Bind a click listener to initiate the flip transitions
		 */
		viewFlipper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mShowingFace) {
					// Tell the server we are flipping up a card
					sendClickMessage(0);
				}
			}
		});

		if (viewFlipper2 != null) {
			viewFlipper2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!mShowingFace2) {
						// Tell the server we are flipping up a card
						sendClickMessage(1);
					}
				}
			});
		}

		winView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				viewFlipper.setVisibility(View.VISIBLE);
				flipCardDown(0);
				if (mIsTablet) {
					flipCardDown(1);
					viewFlipper2.setVisibility(View.VISIBLE);
				}
				winView.setVisibility(View.GONE);
				sendStartMessage();
			}
		});
	}

	private void flipCardUp(final int cardId) {
		if (cardId == 0 && !mShowingFace) {
			AnimationFactory.flipTransition(viewFlipper, FlipDirection.LEFT_RIGHT);
			mShowingFace = true;
		}
		if (cardId == 1 && !mShowingFace2) {
			AnimationFactory.flipTransition(viewFlipper2, FlipDirection.LEFT_RIGHT);
			mShowingFace2 = true;
		}
	}

	private void flipCardDown(final int cardId) {
		if (cardId == 0 && mShowingFace) {
			AnimationFactory.flipTransition(viewFlipper, FlipDirection.RIGHT_LEFT);
			mShowingFace = false;
		}
		if (cardId == 1 && mShowingFace2) {
			AnimationFactory.flipTransition(viewFlipper2, FlipDirection.RIGHT_LEFT);
			mShowingFace2 = false;
		}
	}

	private void win() {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {

				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (viewFlipper != null) {
							viewFlipper.setVisibility(View.GONE);
						}

						if (viewFlipper2 != null) {
							viewFlipper2.setVisibility(View.GONE);
						}

						winView.setVisibility(View.VISIBLE);
					}
				});

			}
		}.start();

	}

	/*********************************************************************
	 * AMQP Message handling
	 *********************************************************************/

	@Override
	public void onAmqpConnected(String queueName) {
		Log.d(TAG, "Connected to: " + queueName);
		// sendMemoryGameReadyMessage();
	}

	private void sendMemoryGameReadyMessage() {
		publishToAll(MemoryMessageType.CONFIRM.toString(), new MemoryConfirm(getId()).toJson().toString());
	}

	private void sendClickMessage(final int cardId) {
		publishToAll(MemoryMessageType.CLICK.toString(), new MemoryClick(cardId).toJson().toString());
	}

	private void sendStartMessage() {
		final ClientStartRequest clientStartRequest = new ClientStartRequest(AmqpConstants.MEMORY_SERVER_APP_NAME);
		publishToAll(GameControlMessageType.CLIENT_START.toString(), clientStartRequest.toJson().toString());
	}

	@Override
	public void onMessageReceived(String messageType, String messageJson) {
		Log.d(TAG, "message Type: " + messageType + " messageJson: " + messageJson);

		Protocol protocol = sGameControlGson.fromJson(messageJson, Protocol.class);
		Log.d(TAG, "protocol: " + protocol);
		if (protocol != null) {
			final GameControlMessageType gameControlMesseageType = GameControlMessageType.getModelType(protocol.getType());
			if (gameControlMesseageType == GameControlMessageType.SERVER_START) {
				final ServerStartResponse serverStartResponse = (ServerStartResponse) protocol.getData();
				if (AmqpConstants.MEMORY_SERVER_APP_NAME.equals(serverStartResponse.getApp()))
					sendMemoryGameReadyMessage();
				return;
			}
		}

		MemoryMessageType type = MemoryMessageType.getModelType(messageType);
		Log.d(TAG, "message Type: " + type);

		switch (type) {

		case ASSIGN:
			winView.setVisibility(View.GONE);
			MemoryAssign assignMessage = mGson.fromJson(messageJson, MemoryAssign.class);

			Log.d(TAG, "Recieved Assign");

			if (assignMessage.getId().equals(getId())) {

				mShowingFace = false;
				mShowingFace2 = false;

				ArrayList<Integer> picIDs = assignMessage.getPic_ids();
				ImageView cardFaceImageView = (ImageView) findViewById(R.id.card_face_image_view);
				ImageView cardFaceImageView2 = (ImageView) findViewById(R.id.card_face_image_view2);

				ImageView cardBackImageView = (ImageView) findViewById(R.id.card_back_image_view);
				ImageView cardBackImageView2 = (ImageView) findViewById(R.id.card_back_image_view2);
				cardBackImageView.setVisibility(View.GONE);

				if (cardBackImageView2 != null) {
					cardBackImageView2.setVisibility(View.GONE);
				}

				if (picIDs.isEmpty())
					return;
				Log.d(TAG, "picIDs " + picIDs);
				int index = picIDs.remove(0);
				cardFaceImageView.setImageResource(cardFaceResourceIDs[index]);
				cardBackImageView.setVisibility(View.VISIBLE);
				Log.d(TAG, "viewFlipper " + viewFlipper);
				viewFlipper.setDisplayedChild(0);
				viewFlipper.setVisibility(View.VISIBLE);

				if (picIDs.isEmpty())
					return;

				if (cardBackImageView2 != null) {
					index = picIDs.get(0);
					cardFaceImageView2.setImageResource(cardFaceResourceIDs[index]);
					cardBackImageView2.setVisibility(View.VISIBLE);
					viewFlipper2.setDisplayedChild(0);
					viewFlipper2.setVisibility(View.VISIBLE);
				}

			}
			break;

		case FLIP:
			MemoryFlip flipMessage = mGson.fromJson(messageJson, MemoryFlip.class);

			Log.d(TAG, "Recieved Flip");

			int action = flipMessage.getAction();

			if (action == MemoryFlip.UP) {
				flipCardUp(flipMessage.getCardId());
			} else if (action == MemoryFlip.DOWN) {
				flipCardDown(flipMessage.getCardId());
			} else if (action == MemoryFlip.WIN) { // WIN
				win();
			} else {

			}
			break;

		case CLICK:
		case EMPTY:
		default:
			Log.d(TAG, "Invalid message type");
		}
	}

	@Override
	public void onAmqpDisconnected() {
		Log.d(TAG, "Disconnected!!");
	}
}
