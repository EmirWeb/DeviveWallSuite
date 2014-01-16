package com.xtremelabs.devicewall.protocol;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ProtocolDeserializer implements JsonDeserializer<Protocol> {

	@Override
	public Protocol deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		if (json == null)
			return null;

		final JsonObject jsonObject = json.getAsJsonObject();
		if (jsonObject == null)
			return null;

		final JsonElement typeJsonElement = jsonObject.get(Protocol.TYPE);
		if (typeJsonElement == null)
			return null;

		final JsonElement idJsonElement = jsonObject.get(Protocol.ID);
		Long id = null;
		if (idJsonElement != null && !idJsonElement.isJsonNull())
			id = idJsonElement.getAsLong();

		final String typeString = typeJsonElement.getAsString();
		
		return new Protocol(id, typeString, null);
	}

}
