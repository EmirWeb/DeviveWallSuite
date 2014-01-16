package com.xtremelabs.devicewall.protocol.memory;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlDeserializer;
import com.xtremelabs.devicewall.protocol.identifier.IdentifierDeserializer;

public class MemoryDeserializer implements JsonDeserializer<Protocol> {

	private IdentifierDeserializer identifierDeserializer = new IdentifierDeserializer();
	private GameControlDeserializer gameControlDeserializer = new GameControlDeserializer();
	
	@Override
	public Protocol deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		Protocol protocol = identifierDeserializer.deserialize(json, typeOfT, context);
		
		if (protocol == null) {
			return null;
		} else if (protocol.getData() == null) {
			protocol = gameControlDeserializer.deserialize(json, typeOfT, context);
			
			if (protocol.getData() == null) {
				final JsonObject jsonObject = json.getAsJsonObject();
				final JsonElement dataJsonElement = jsonObject.get(Protocol.DATA);
				
				Data data = null;
				
				MemoryMessageType messageType = MemoryMessageType.getModelType(protocol.getType());
				
				switch (messageType) {
				case FLIP:
					data = context.deserialize(dataJsonElement, MemoryFlip.class);
					break;
				case CLICK:
					data = context.deserialize(dataJsonElement, MemoryClick.class);
					break;
				case CONFIRM:
					data = context.deserialize(dataJsonElement, MemoryConfirm.class);
					break;
				case ASSIGN:
					data = context.deserialize(dataJsonElement, MemoryAssign.class);
					break;
				default:
					break;
				}
				
				protocol = new Protocol(protocol.getId(), protocol.getType(), data);
			}
		}
		
		return protocol;
	}

}
