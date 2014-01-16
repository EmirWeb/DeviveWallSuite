package com.xtremelabs.devicewall.protocol.identifier.request;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.xtremelabs.devicewall.protocol.Data;

public class DesktopServerIdentifierRequest implements Data {

	public static final String QUEUE_NAME_KEY = "queueName";
	@SerializedName("queueName")
	private final String mQueueName;

	public DesktopServerIdentifierRequest(final String queueName) {
		mQueueName = queueName;
	}

	public String getQueueName() {
		return mQueueName;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(QUEUE_NAME_KEY, mQueueName);
		return jsonObject;
	}

}
