package com.xtremelabs.devicewall.protocol.image;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.ProtocolDeserializer;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlDeserializer;
import com.xtremelabs.devicewall.protocol.identifier.IdentifierDeserializer;
import com.xtremelabs.devicewall.protocol.image.data.ImageData;

public class ImageDeserializer implements JsonDeserializer<Protocol> {

	private IdentifierDeserializer identifierDeserializer = new IdentifierDeserializer();
	private GameControlDeserializer gameControlDeserializer = new GameControlDeserializer();
	private ProtocolDeserializer protocolDeserializer = new ProtocolDeserializer();

	@Override
	public Protocol deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		Protocol protocol = identifierDeserializer.deserialize(json, typeOfT, context);

		if (protocol == null) {
			return null;
		} else if (protocol.getData() == null) {
			protocol = gameControlDeserializer.deserialize(json, typeOfT, context);
			if (protocol.getData() == null) {

				protocol = protocolDeserializer.deserialize(json, typeOfT, context);

				if (protocol == null) {
					return null;
				}

				final ImageMessageType messageType = ImageMessageType.getModelType(protocol.getType());

				final JsonElement dataJsonElement = json.getAsJsonObject().get(Protocol.DATA);

				if (dataJsonElement == null)
					return null;

				Data data = null;

				switch (messageType) {
				case DATA:
					data = context.deserialize(dataJsonElement, ImageData.class);
					break;
				case CONFIRM:
				default:
				}

				protocol = new Protocol(protocol.getId(), protocol.getType(), data);

			}
		}

		return protocol;
	}

}
