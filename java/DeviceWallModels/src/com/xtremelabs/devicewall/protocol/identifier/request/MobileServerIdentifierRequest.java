package com.xtremelabs.devicewall.protocol.identifier.request;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.xtremelabs.devicewall.protocol.Data;

public class MobileServerIdentifierRequest implements Data {

	public static final String QUEUE_NAME_KEY = "queueName";
	@SerializedName("queueName")
	private final String mQueueName;

	public static final String IS_TABLET_KEY = "isTablet";
	@SerializedName("isTablet")
	private final Boolean mIsTablet;

	public static final String WIDTH_KEY = "width";
	@SerializedName("width")
	private final Integer mWidth;

	public static final String HEIGHT_KEY = "height";
	@SerializedName("iheight")
	private final Integer mHeight;

	public static final String DEVICE_SERIAL = "device_serial";
	@SerializedName("device_serial")
	private final String mDeviceSerial;

	public MobileServerIdentifierRequest(final String queueName, final Boolean isTablet, final Integer width, final Integer height, final String deviceSerial) {
		mWidth = width;
		mHeight = height;
		mQueueName = queueName;
		mIsTablet = isTablet;
		mDeviceSerial = deviceSerial;
	}
	public String getDeviceSerial() {
		return mDeviceSerial;
	}

	public String getQueueName() {
		return mQueueName;
	}

	public boolean isTablet() {
		return mIsTablet != null && mIsTablet;
	}

	public Integer getWidth() {
		return mWidth;
	}

	public Integer getHeight() {
		return mHeight;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(IS_TABLET_KEY, mIsTablet);
		jsonObject.addProperty(WIDTH_KEY, mWidth);
		jsonObject.addProperty(HEIGHT_KEY, mHeight);
		jsonObject.addProperty(QUEUE_NAME_KEY, mQueueName);
		jsonObject.addProperty(DEVICE_SERIAL, mDeviceSerial);
		return jsonObject;
	}

}
