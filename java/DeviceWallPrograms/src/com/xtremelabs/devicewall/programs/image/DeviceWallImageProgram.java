package com.xtremelabs.devicewall.programs.image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.xtremelabs.devicewall.programs.amqp.AmqpConnectionManager;
import com.xtremelabs.devicewall.programs.amqp.AmqpListener;
import com.xtremelabs.devicewall.protocol.AmqpConstants;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlMessageType;
import com.xtremelabs.devicewall.protocol.gamecontrol.request.ClientStartRequest;
import com.xtremelabs.devicewall.protocol.gamecontrol.response.ServerStartResponse;
import com.xtremelabs.devicewall.protocol.identifier.IdentifierMessageType;
import com.xtremelabs.devicewall.protocol.identifier.data.MobileServerIdentifierData;
import com.xtremelabs.devicewall.protocol.identifier.response.MapIdentifierResponse;
import com.xtremelabs.devicewall.protocol.image.ImageDeserializer;
import com.xtremelabs.devicewall.protocol.image.ImageMessageType;
import com.xtremelabs.devicewall.protocol.image.request.ClientStartConfirmRequest;

public class DeviceWallImageProgram {

	public static class PhoneInfo {

		public PhoneInfo(Long id, String queue, double xRelative, double yRelative, double widthRelative, double heightRelative) {
			this.id = id;
			this.queue = queue;
			this.xRelative = xRelative;
			this.yRelative = yRelative;
			this.widthRelative = widthRelative;
			this.heightRelative = heightRelative;
		}

		public Long id;
		public String queue;

		public double xRelative;
		public double yRelative;

		public double widthRelative;
		public double heightRelative;
	}

	public static class OCRResponse {
		public double width;
		public double height;
		public PhoneInfo[] screens;
	}

	private static AmqpConnectionManager mAmqpConnectionManager;
	private static Gson sGson;
	static {
		final GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Protocol.class, new ImageDeserializer());
		sGson = builder.create();
	}

	private static String url;
	private static Dimension imageDimensions;
	private static Map<Long, PhoneInfo> map = new HashMap<Long, PhoneInfo>();

	public static void main(final String[] args) throws Exception {
		mAmqpConnectionManager = new AmqpConnectionManager(sAmqpListener);
	}

	private static void sendStartMessage() {
		mAmqpConnectionManager.publishToAll(GameControlMessageType.SERVER_START.toString(), new ServerStartResponse(AmqpConstants.IMAGE_SERVER_APP_NAME).toJson().toString());
	}

	private static void sendMessages(final PhoneInfo info) {
		int x = (int) (info.xRelative * imageDimensions.width);
		int y = (int) (info.yRelative * imageDimensions.height);
		int width = (int) (info.widthRelative * imageDimensions.width);
		int height = (int) (info.heightRelative * imageDimensions.height);
		String json = String.format("{\"image_url\": \"%s\",\"show_rect\": {\"x\": %d,\"y\": %d,\"width\": %d,\"height\": %d}}", url, x, y, width, height);
		System.out.println("queue: " + info.queue + ", json: " + json);
		if (info.id != null) {
			mAmqpConnectionManager.publishToBinding(Long.toString(info.id), "image", json);
		} else {
			mAmqpConnectionManager.publishToBinding(info.queue, "image", json);
		}
	}

	private static OCRResponse populateOCRMap() {
		// get info from OCR
		// String json =
		// "{\"width\": 2359.0, \"height\": 1576.0, \"screens\": [{\"id\": 13, \"xRelative\": 0.03815175922000848, \"yRelative\": 0.7436548223350253, \"widthRelative\": 0.3128444256040695, \"heightRelative\": 0.9873096446700508},{\"id\": 18, \"xRelative\": 0.7236116998728275, \"yRelative\": 0.6605329949238579, \"widthRelative\": 0.9957609156422212, \"heightRelative\": 0.9035532994923858},{\"id\": 12, \"xRelative\": 0.6265366680796948, \"yRelative\": 0.24048223350253808, \"widthRelative\": 0.8944467994913099, \"heightRelative\": 0.4733502538071066},{\"id\": 17, \"xRelative\": 0.003391267486222976, \"yRelative\": 0.05710659898477157, \"widthRelative\": 0.17125900805426028, \"heightRelative\": 0.4403553299492386},{\"id\": 11, \"xRelative\": 0.39084357778719797, \"yRelative\": 0.7493654822335025, \"widthRelative\": 0.662144976685036, \"heightRelative\": 0.9961928934010152},{\"id\": 16, \"xRelative\": 0.29122509537939806, \"yRelative\": 0.3730964467005076, \"widthRelative\": 0.5582874099194574, \"heightRelative\": 0.608502538071066},{\"id\": 15, \"xRelative\": 0.2560406952098347, \"yRelative\": 0.0, \"widthRelative\": 0.5218312844425604, \"heightRelative\": 0.23223350253807107},{\"id\": 14, \"xRelative\": 0.069520983467571, \"yRelative\": 0.5380710659898477, \"widthRelative\": 0.20983467571004663, \"heightRelative\": 0.6649746192893401}]}";
		String json = "{\"width\": 2002.0, \"height\": 1253.0, \"screens\": [{\"id\": 14, \"xRelative\": 0.007992007992007992, \"yRelative\": 0.026336791699920193, \"widthRelative\": 0.3926073926073926, \"heightRelative\": 0.37988826815642457},{\"id\": 13, \"xRelative\": 0.0034965034965034965, \"yRelative\": 0.6296887470071828, \"widthRelative\": 0.39760239760239763, \"heightRelative\": 1.0},{\"id\": 0, \"xRelative\": 0.8296703296703297, \"yRelative\": 0.0, \"widthRelative\": 1.0, \"heightRelative\": 0.1524341580207502}]}";
		File jsonFile = new File("/Users/devfloater56/Development/workspace/FindSquares/squares_data.json");
		if (jsonFile.exists()) {

			FileReader fr = null;

			try {
				fr = new FileReader(jsonFile);
				OCRResponse response = sGson.fromJson(fr, OCRResponse.class);
				for (PhoneInfo phone : response.screens) {
					phone.widthRelative -= phone.xRelative;
					phone.heightRelative -= phone.yRelative;
					map.put(phone.id, phone);
				}

				return response;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				closeStreams(fr);
			}

		}

		return null;
	}

	private static void closeStreams(Closeable... streams) {
		for (Closeable closeable : streams) {
			try {
				if (closeable != null) {
					closeable.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static Dimension getImageDimensions(String url) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response;
		try {
			response = client.execute(get);
			InputStream stream = response.getEntity().getContent();

			JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(stream);

			BufferedImage bufferedImage = decoder.decodeAsBufferedImage();

			int height = bufferedImage.getHeight();
			int width = bufferedImage.getWidth();

			Dimension dimension = new Dimension(width, height);
			return dimension;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void parseServerIdentifierResponse(final MapIdentifierResponse data) {
		Collection<MobileServerIdentifierData> map2 = data.getMap();
		for (MobileServerIdentifierData entry : map2) {
			PhoneInfo phoneInfo = map.get(entry.getId());
			if (phoneInfo != null) {
				phoneInfo.queue = entry.getQueueName();
			}
		}
	}

	private static void handleClientStart() {
		OCRResponse response = populateOCRMap();

		url = "http://" + AmqpConstants.MOBILE_HOST + ":8080/test.jpg";// "http://timenerdworld.files.wordpress.com/2013/01/wpid-photo-jan-14-2013-1117-am.jpg";
		imageDimensions = getImageDimensions(url);

		adjustImageDimensions(response);

		mAmqpConnectionManager.publishToServer(IdentifierMessageType.MAP_SERVER_REQUEST.toString(), "");
	}

	private static void adjustImageDimensions(OCRResponse response) {
		double ocrRatio = response.width / response.height;
		double imageRatio = imageDimensions.getWidth() / imageDimensions.getHeight();

		if (ocrRatio < imageRatio) {
			imageDimensions.width = (int) (imageDimensions.height * ocrRatio);
		} else if (ocrRatio > imageRatio) {
			imageDimensions.height = (int) (imageDimensions.width / ocrRatio);
		}
	}

	private static AmqpListener sAmqpListener = new AmqpListener() {

		@Override
		public void handleDelivery(String body) throws IOException {
			System.out.println("handleDeliver ImageProg: " + body);
			final Protocol protocol = sGson.fromJson(body, Protocol.class);
			if (protocol == null)
				return;

			final Data data = protocol.getData();
			final String typeString = protocol.getType();
			System.out.println("messageType: " + typeString);

			final GameControlMessageType gameType = GameControlMessageType.getModelType(typeString);
			if (gameType == GameControlMessageType.CLIENT_START) {
				final ClientStartRequest clientStartRequest = (ClientStartRequest) protocol.getData();
				if (AmqpConstants.IMAGE_SERVER_APP_NAME.equals(clientStartRequest.getApp()))
					handleClientStart();
				return;
			}

			final IdentifierMessageType identifierType = IdentifierMessageType.getModelType(typeString);
			if (identifierType == IdentifierMessageType.MAP_SERVER_RESPONSE) {
				parseServerIdentifierResponse((MapIdentifierResponse) data);
				sendStartMessage();
				return;
			}

			final ImageMessageType imageMessageType = ImageMessageType.getModelType(typeString);
			System.out.println("imageMessageType: " + imageMessageType);
			if (imageMessageType == ImageMessageType.CONFIRM) {
				final ClientStartConfirmRequest clientStartRequest = (ClientStartConfirmRequest) protocol.getData();
				final Long id = protocol.getId();
				System.out.println("id: " + id);
				System.out.println("map: " + map);
				if (id == null)
					return;

				final PhoneInfo phoneInfo = map.get(id);
				if (phoneInfo == null)
					return;
				sendMessages(phoneInfo);

				return;
			}

		}

		@Override
		public void onConnected() {
		}

		@Override
		public void onDisconnected() {
		}
	};

}
