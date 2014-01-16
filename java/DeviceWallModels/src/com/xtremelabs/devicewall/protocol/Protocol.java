package com.xtremelabs.devicewall.protocol;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Protocol {

	public static final String ID = "id";

	/**
	 * The app id.
	 */
	@SerializedName("id")
	private final Long mId;

	public static final String TYPE = "type";
	@SerializedName("type")
	private final String mMessageType;

	public static final String DATA = "data";
	@SerializedName("data")
	private final Data mData;

	public Protocol(final Long id, final String messageType, final Data data) {
		mId = id;
		mData = data;
		mMessageType = messageType;
	}

	public String getType() {
		return mMessageType;
	}

	public Long getId() {
		return mId;
	}

	public Data getData() {
		return mData;
	}

	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(ID, mId);
		jsonObject.addProperty(TYPE, mMessageType.toString());
		jsonObject.add(DATA, mData.toJson());
		return jsonObject;
	}

}
