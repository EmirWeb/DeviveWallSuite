package com.xtremelabs.devicewall.protocol.identifier;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.ProtocolDeserializer;
import com.xtremelabs.devicewall.protocol.identifier.request.DesktopServerIdentifierRequest;
import com.xtremelabs.devicewall.protocol.identifier.request.MobileServerIdentifierRequest;
import com.xtremelabs.devicewall.protocol.identifier.response.MapIdentifierResponse;
import com.xtremelabs.devicewall.protocol.identifier.response.ServerIdentifierResponse;

public class IdentifierDeserializer implements JsonDeserializer<Protocol> {
	ProtocolDeserializer protocolDeserializer = new ProtocolDeserializer();

	@Override
	public Protocol deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		
		Protocol protocol = protocolDeserializer.deserialize(json, typeOfT, context);
		
		if (protocol == null) {
			return null;
		}
		
		final IdentifierMessageType messageType = IdentifierMessageType.getModelType(protocol.getType());

		final JsonElement dataJsonElement = json.getAsJsonObject().get(Protocol.DATA);
		
		if (dataJsonElement == null)
			return null;

		Data data = null;
		
		switch (messageType) {
		case MOBILE_SERVER_IDENTIFIER_REQUEST:
			data = context.deserialize(dataJsonElement, MobileServerIdentifierRequest.class);
			break;
		case DESKTOP_SERVER_IDENTIFIER_REQUEST:
			data = context.deserialize(dataJsonElement, DesktopServerIdentifierRequest.class);
			break;
		case SERVER_IDENTIFIER_RESPONSE:
			data = context.deserialize(dataJsonElement, ServerIdentifierResponse.class);
			break;
		case MAP_SERVER_RESPONSE:
			data = context.deserialize(dataJsonElement, MapIdentifierResponse.class);
			break;
		case MAP_SERVER_REQUEST:
		default:
			break;
		}
		
		protocol = new Protocol(protocol.getId(), protocol.getType(), data);

		return protocol;
	}

}
