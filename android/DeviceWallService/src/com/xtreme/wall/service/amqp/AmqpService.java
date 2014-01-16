package com.xtreme.wall.service.amqp;

import java.io.IOException;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothClass.Device;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.xtreme.utils.Logger;
import com.xtreme.wall.service.amqp.AmqpConnection.AmqpConnectionListener;
import com.xtreme.wall.service.amqp.AmqpServiceBinder.PublishListener;
import com.xtreme.wall.service.utils.ScreenUtils;
import com.xtreme.wall.shared.amqp.AmqpPublishListener;
import com.xtremelabs.devicewall.protocol.AmqpConstants;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlDeserializer;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlMessageType;
import com.xtremelabs.devicewall.protocol.gamecontrol.response.ServerStartResponse;
import com.xtremelabs.devicewall.protocol.identifier.IdentifierMessageType;
import com.xtremelabs.devicewall.protocol.identifier.request.MobileServerIdentifierRequest;
import com.xtremelabs.devicewall.protocol.identifier.response.ServerIdentifierResponse;

public class AmqpService extends Service {

	private AmqpServiceBinder mAmqpServiceBinder;
	private AmqpConnection mConnection;
	private AmqpAutoConnectUtil mConnectionUtil;
	private Long mId;

	private static Gson sGameControlGson;
	static {
		final GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Protocol.class, new GameControlDeserializer());
		sGameControlGson = builder.create();
	}

	public static enum AmqpListenerType {
		passive, active
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.setup(true, "AmqpService");
		Logger.debug("onCreate");
		final Intent startServiceIntent = new Intent(getApplicationContext(), AmqpService.class);
		startService(startServiceIntent);
		mAmqpServiceBinder = new AmqpServiceBinder(this, new PublishListener() {
			@Override
			public void onPublishToServer(final String json, final AmqpPublishListener listener) {
				mConnection.publish(AmqpConstants.SERVER_EXCHANGE, AmqpConstants.SERVER_ROUTING_KEY, null, json.getBytes(), listener);
			}

			@Override
			public void onPublishToAll(final String json, final AmqpPublishListener listener) {
				mConnection.publish(AmqpConstants.SERVER_EXCHANGE, AmqpConstants.CLIENT_ROUTING_KEY, null, json.getBytes(), listener);
			}
		});

		mConnection = new AmqpConnection(new AmqpConnectionListener() {
			@Override
			public void onAmqpDisconnected() {
				mAmqpServiceBinder.onDisconnected();
				mConnectionUtil.autoConnect();
			}

			@Override
			public void onAmqpConnected() {
				initializeConsumer();
			}
		});

		mConnectionUtil = new AmqpAutoConnectUtil(mConnection);
		mConnectionUtil.autoConnect();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Logger.debug("onBond");
		return mAmqpServiceBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.debug("onStartCommand");
		return Service.START_STICKY;
	}

	private void initializeConsumer() {
		Logger.debug("initializeConsumer");
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				Logger.debug("doInBackground");
				final Channel channel = mConnection.getChannel();
				try {

					String serial = Build.SERIAL;
					if (serial == null || serial.isEmpty()) {
						SharedPreferences sharedPreferences = getSharedPreferences("amqp_service", Context.MODE_PRIVATE);
						serial = sharedPreferences.getString("uuid", UUID.randomUUID().toString());
						sharedPreferences.edit().putString("uuid", serial).commit();
					}
					Logger.debug("serial: " + serial);
					final String queueName = channel.queueDeclare(serial, false, true, false, null).getQueue();
					Logger.debug("queueName: "+ queueName);

					channel.queueBind(queueName, AmqpConstants.SERVER_EXCHANGE, AmqpConstants.CLIENT_ROUTING_KEY);
					Logger.d("bound to: " + AmqpConstants.CLIENT_ROUTING_KEY);
					channel.queueBind(queueName, AmqpConstants.SERVER_EXCHANGE, serial);
					Logger.d("bound to: " + serial);
					if (mId != null) {
						channel.queueBind(queueName, AmqpConstants.SERVER_EXCHANGE, Long.toString(mId));
					}
					channel.basicConsume(queueName, new QueueingConsumer(channel) {

						@Override
						public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
							final String message = new String(body);
							Logger.debug("message: " + message);
							channel.basicAck(envelope.getDeliveryTag(), true);

							Protocol protocol = sGameControlGson.fromJson(message, Protocol.class);
							final GameControlMessageType gameControlMesseageType = GameControlMessageType.getModelType(protocol.getType());
							switch (gameControlMesseageType) {
							case SERVER_START:
								final ServerStartResponse serverStartResponse = (ServerStartResponse) protocol.getData();
								if (AmqpConstants.MEMORY_SERVER_APP_NAME.equals(serverStartResponse.getApp())) {
									final Intent intent = new Intent(Intent.ACTION_MAIN);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.setComponent(new ComponentName("com.xtremelabs.devicewallmemorygame", "com.xtremelabs.devicewallmemorygame.MainActivity"));
									startActivity(intent);
								} else if (AmqpConstants.IMAGE_SERVER_APP_NAME.equals(serverStartResponse.getApp())) {
									Intent intent = new Intent(Intent.ACTION_MAIN);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.setComponent(new ComponentName("com.xtremelabs.devicewallimageapp", "com.xtremelabs.devicewallimageapp.ImageActivity"));
									startActivity(intent);
								} else if (AmqpConstants.IDENTIFIER_SERVER_APP_NAME.equals(serverStartResponse.getApp())) {
									Intent intent = new Intent(Intent.ACTION_MAIN);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.setComponent(new ComponentName("com.xtremelabs.devicewallidentifierapp", "com.xtremelabs.devicewallidentifierapp.activities.IdentifierActivity"));
									startActivity(intent);
								}
							default:
							}

							protocol = AmqpConstants.sIdentifierGson.fromJson(message, Protocol.class);
							if (protocol == null) {
								mAmqpServiceBinder.onMessageReceived(message);
								return;
							}

							final IdentifierMessageType messageType = IdentifierMessageType.getModelType(protocol.getType());
							switch (messageType) {
							case SERVER_IDENTIFIER_RESPONSE:
								final ServerIdentifierResponse serverIdentifierResponse = (ServerIdentifierResponse) protocol.getData();
								final Long id = serverIdentifierResponse.getId();
								if (id != null)
									channel.queueBind(queueName, AmqpConstants.SERVER_EXCHANGE, Long.toString(id));
								mAmqpServiceBinder.setId(id);
								break;
							default:
								mAmqpServiceBinder.onMessageReceived(message);
							}
						}
					});

					final Boolean isTablet = ScreenUtils.isTablet(getApplicationContext());

					final WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
					final Display display = wm.getDefaultDisplay();
					final Point size = new Point();
					// display.getSize(size);
					final int width = size.x;
					final int height = size.y;

					final MobileServerIdentifierRequest mobileServerIdentifierRequest = new MobileServerIdentifierRequest(queueName, isTablet, width, height, serial);
					final Protocol protocol = new Protocol(null, IdentifierMessageType.MOBILE_SERVER_IDENTIFIER_REQUEST.toString(), mobileServerIdentifierRequest);
					Logger.debug("protocol: " + protocol.toJson().toString());
					channel.basicPublish(AmqpConstants.SERVER_EXCHANGE, AmqpConstants.SERVER_ROUTING_KEY, null, protocol.toJson().toString().getBytes());
					return queueName;

				} catch (Exception e) {
					e.printStackTrace();
					mConnection.close();
					mConnectionUtil.autoConnect();
					return null;
				}
			}

			@Override
			protected void onPostExecute(String queueName) {
				if (queueName != null) {
					mAmqpServiceBinder.onConnected(queueName);
				} else {
					mConnectionUtil.autoConnect();
				}
			}
		}.execute();
	}

	public void setId(Long id) {
		mId = id;
	}

	public Long getId() {
		return mId;
	}
}
