package com.xtremelabs.devicewall.protocol.gamecontrol;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.ProtocolDeserializer;
import com.xtremelabs.devicewall.protocol.gamecontrol.request.ClientStartRequest;
import com.xtremelabs.devicewall.protocol.gamecontrol.response.ServerStartResponse;

public class GameControlDeserializer implements JsonDeserializer<Protocol> {
	
	private ProtocolDeserializer protocolDeserializer = new ProtocolDeserializer();

	@Override
	public Protocol deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		Protocol protocol = protocolDeserializer.deserialize(json, typeOfT, context);
		
		if (protocol == null) {
			return null;
		}
		
		final GameControlMessageType messageType = GameControlMessageType.getModelType(protocol.getType());

		final JsonElement dataJsonElement = json.getAsJsonObject().get(Protocol.DATA);
		
		if (dataJsonElement == null)
			return null;

		Data data = null;
		
		switch (messageType) {
		case CLIENT_START:
			data = context.deserialize(dataJsonElement, ClientStartRequest.class);
			break;
		case SERVER_START:
			data = context.deserialize(dataJsonElement, ServerStartResponse.class);
			break;
		default:
			break;
		}
		
		protocol = new Protocol(protocol.getId(), protocol.getType(), data);

		return protocol;
	}

}
