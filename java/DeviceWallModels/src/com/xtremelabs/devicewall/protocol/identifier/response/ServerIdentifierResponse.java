package com.xtremelabs.devicewall.protocol.identifier.response;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.xtremelabs.devicewall.protocol.Data;

public class ServerIdentifierResponse implements Data {

	public static final String ID_KEY = "id";
	@SerializedName("id")
	private final Long mId;

	public ServerIdentifierResponse(final Long id) {
		mId= id;
	}

	public Long getId() {
		return mId;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(ID_KEY, mId);
		return jsonObject;
	}

}
