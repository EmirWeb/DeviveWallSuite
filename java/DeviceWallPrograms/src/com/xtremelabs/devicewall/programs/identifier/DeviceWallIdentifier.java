package com.xtremelabs.devicewall.programs.identifier;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.xtremelabs.devicewall.programs.amqp.AmqpListener;
import com.xtremelabs.devicewall.protocol.AmqpConstants;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.identifier.IdentifierDeserializer;
import com.xtremelabs.devicewall.protocol.identifier.IdentifierMessageType;
import com.xtremelabs.devicewall.protocol.identifier.data.DesktopServerIdentifierData;
import com.xtremelabs.devicewall.protocol.identifier.data.MobileServerIdentifierData;
import com.xtremelabs.devicewall.protocol.identifier.request.DesktopServerIdentifierRequest;
import com.xtremelabs.devicewall.protocol.identifier.request.MobileServerIdentifierRequest;
import com.xtremelabs.devicewall.protocol.identifier.response.MapIdentifierResponse;
import com.xtremelabs.devicewall.protocol.identifier.response.ServerIdentifierResponse;
import com.xtremelabs.devicewall.utils.IdCreator;

public class DeviceWallIdentifier {

	private static final ConcurrentHashMap<Long, MobileServerIdentifierData> MOBILE_SERVER_IDENTIFIERS = new ConcurrentHashMap<Long, MobileServerIdentifierData>();
	private static final ConcurrentHashMap<Long, DesktopServerIdentifierData> DESKTOP_SERVER_IDENTIFIERS = new ConcurrentHashMap<Long, DesktopServerIdentifierData>();
	private static final ConcurrentHashMap<String, Long> MOBILE_DEVICE_SERIALS = new ConcurrentHashMap<String, Long>();

	private static final IdCreator ID_CREATOR = new IdCreator();
	private static final Long ID = ID_CREATOR.getNewId();

	private static AmqpIdentifierConnectionManager sAmqpConnectionManager;
	private static Gson sGson;
	static {
		final GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Protocol.class, new IdentifierDeserializer());
		sGson = builder.create();
	}

	public static void main(final String[] args) {
		try {
			sAmqpConnectionManager = new AmqpIdentifierConnectionManager(sAmqpListener);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static AmqpListener sAmqpListener = new AmqpListener() {

		@Override
		public void handleDelivery(String body) throws IOException {
			System.out.println("Identifier received a message: " + body);
			final Protocol protocol = sGson.fromJson(body, Protocol.class);
			System.out.println("protocol: " + protocol);
			if (protocol == null)
				return;

			final Data data = protocol.getData();
			final IdentifierMessageType messageType = IdentifierMessageType.getModelType(protocol.getType());
			final Long id = protocol.getId();
			System.out.println("messageType: " + messageType);
			switch (messageType) {
			case MOBILE_SERVER_IDENTIFIER_REQUEST:
				handleMobileServerIdentifierData((MobileServerIdentifierRequest) data);
				break;
			case DESKTOP_SERVER_IDENTIFIER_REQUEST:
				handleDesktopServerIdentifierData((DesktopServerIdentifierRequest) data);
				break;
			case MAP_SERVER_REQUEST:
				handleMapRequest(id);
				break;
			default:
				break;
			}

		}

		@Override
		public void onConnected() {
		}

		@Override
		public void onDisconnected() {
		}
	};

	private static synchronized void handleMobileServerIdentifierData(final MobileServerIdentifierRequest mobileServerIdentifierRequest) {
		System.out.println("handleMobileServerIdentifierData:");
		System.out.println("mobileServerIdentifierRequest: " + mobileServerIdentifierRequest);
		if (mobileServerIdentifierRequest == null)
			return;

		final String deviceSerial = mobileServerIdentifierRequest.getDeviceSerial();
		System.out.println("deviceSerial: " + deviceSerial);
		if (deviceSerial == null)
			return;

		Long id = null;
		if (MOBILE_DEVICE_SERIALS.containsKey(deviceSerial)) {
			id = MOBILE_DEVICE_SERIALS.get(deviceSerial);
			System.out.println("has id: " + id);
		} else {
			id = ID_CREATOR.getNewId();
			MOBILE_DEVICE_SERIALS.put(deviceSerial, id);
			System.out.println("added id: " + id);
		}

		final ServerIdentifierResponse serverIdentifierResponse = new ServerIdentifierResponse(id);
		final Protocol clientIdentifierProtocol = new Protocol(ID, IdentifierMessageType.SERVER_IDENTIFIER_RESPONSE.toString(), serverIdentifierResponse);
		final JsonObject jsonObject = clientIdentifierProtocol.toJson();
		System.out.println("jsonObject: " + jsonObject);
		if (jsonObject == null)
			return;

		final boolean success = sAmqpConnectionManager.basicPublish(AmqpConstants.SERVER_EXCHANGE, deviceSerial, null, clientIdentifierProtocol.toJson().toString().getBytes());
		System.out.println("success: " + success);
		if (success)
			MOBILE_SERVER_IDENTIFIERS.put(id, new MobileServerIdentifierData(id, mobileServerIdentifierRequest));
	}

	private synchronized static void handleDesktopServerIdentifierData(final DesktopServerIdentifierRequest desktopServerIdentifierRequest) {
		System.out.println("handleDesktopServerIdentifierData: ");
		System.out.println("desktopServerIdentifierRequest: " + desktopServerIdentifierRequest);
		if (desktopServerIdentifierRequest == null)
			return;

		final String queueName = desktopServerIdentifierRequest.getQueueName();
		System.out.println("queueName: " + queueName);
		if (queueName == null)
			return;

		final Long id = ID_CREATOR.getNewId();
		final ServerIdentifierResponse serverIdentifierResponse = new ServerIdentifierResponse(id);
		final Protocol clientIdentifierProtocol = new Protocol(ID, IdentifierMessageType.SERVER_IDENTIFIER_RESPONSE.toString(), serverIdentifierResponse);
		final JsonObject jsonObject = clientIdentifierProtocol.toJson();
		System.out.println("jsonObject: " + jsonObject);
		if (jsonObject == null)
			return;

		final boolean success = sAmqpConnectionManager.basicPublish(AmqpConstants.SERVER_EXCHANGE, queueName, null, clientIdentifierProtocol.toJson().toString().getBytes());
		System.out.println("success: " + success);
		if (success)
			DESKTOP_SERVER_IDENTIFIERS.put(id, new DesktopServerIdentifierData(id, desktopServerIdentifierRequest));
	}

	private static void handleMapRequest(final Long id) {
		System.out.println("handleMapRequest: " + id);
		if (id == null)
			return;

		final DesktopServerIdentifierData desktopServerIdentifierData = DESKTOP_SERVER_IDENTIFIERS.get(id);
		if (desktopServerIdentifierData == null || desktopServerIdentifierData.getQueueName() == null)
			return;
		final MapIdentifierResponse identifierMapResponse = new MapIdentifierResponse(MOBILE_SERVER_IDENTIFIERS.values());
		final Protocol protocol = new Protocol(ID, IdentifierMessageType.MAP_SERVER_RESPONSE.toString(), identifierMapResponse);

		final JsonObject jsonObject = protocol.toJson();
		if (jsonObject == null)
			return;
		System.out.println("send request");
		final boolean success = sAmqpConnectionManager.basicPublish(AmqpConstants.SERVER_EXCHANGE, desktopServerIdentifierData.getQueueName(), null, protocol.toJson().toString().getBytes());
		System.out.println("sent request: " + success);
	}

}
