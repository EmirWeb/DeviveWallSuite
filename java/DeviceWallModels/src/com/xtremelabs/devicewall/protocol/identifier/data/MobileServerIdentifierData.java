package com.xtremelabs.devicewall.protocol.identifier.data;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.identifier.request.MobileServerIdentifierRequest;

public class MobileServerIdentifierData implements Data {

	public static final String ID_KEY = "id";
	@SerializedName("id")
	private final Long mId;

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
	@SerializedName("height")
	private final Integer mHeight;
	
	public static final String DEVICE_SERIAL = "device_serial";
	@SerializedName("device_serial")
	private final String mDeviceSerial;

	public MobileServerIdentifierData(final Long id, final MobileServerIdentifierRequest mobileServerIdentifierRequest) {
		mId = id;
		mWidth = mobileServerIdentifierRequest.getWidth();
		mHeight = mobileServerIdentifierRequest.getHeight();
		mQueueName = mobileServerIdentifierRequest.getQueueName();
		mIsTablet = mobileServerIdentifierRequest.isTablet();
		mDeviceSerial = mobileServerIdentifierRequest.getDeviceSerial();
	}

	public MobileServerIdentifierData(final Long id, final String queueName, final Boolean isTablet, final Integer width, final Integer height, final String deviceSerial) {
		mId = id;
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

	public Long getId() {
		return mId;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(ID_KEY, mId);
		jsonObject.addProperty(IS_TABLET_KEY, mIsTablet);
		jsonObject.addProperty(DEVICE_SERIAL, mDeviceSerial);
		jsonObject.addProperty(WIDTH_KEY, mWidth);
		jsonObject.addProperty(HEIGHT_KEY, mHeight);
		jsonObject.addProperty(QUEUE_NAME_KEY, mQueueName);
		return jsonObject;
	}

}
