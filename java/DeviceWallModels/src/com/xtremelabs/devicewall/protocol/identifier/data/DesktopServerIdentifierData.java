package com.xtremelabs.devicewall.protocol.identifier.data;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.identifier.request.DesktopServerIdentifierRequest;

public class DesktopServerIdentifierData implements Data {

	public static final String ID_KEY = "id";
	@SerializedName("id")
	private final Long mId;

	public static final String QUEUE_NAME_KEY = "queueName";
	@SerializedName("queueName")
	private final String mQueueName;

	public DesktopServerIdentifierData(final Long id, final DesktopServerIdentifierRequest desktopServerIdentifierRequest) {
		mId = id;
		mQueueName = desktopServerIdentifierRequest.getQueueName();
	}

	public DesktopServerIdentifierData(final Long id, final String queueName) {
		mId = id;
		mQueueName = queueName;
	}

	public String getQueueName() {
		return mQueueName;
	}

	public Long getId() {
		return mId;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(ID_KEY, mId);
		jsonObject.addProperty(QUEUE_NAME_KEY, mQueueName);
		return jsonObject;
	}

}
