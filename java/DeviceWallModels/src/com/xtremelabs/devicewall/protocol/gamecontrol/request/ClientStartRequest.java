package com.xtremelabs.devicewall.protocol.gamecontrol.request;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.xtremelabs.devicewall.protocol.Data;

public class ClientStartRequest implements Data {

	public final String APP = "app";

	@SerializedName("app")
	private final String mApp;

	public ClientStartRequest(String app) {
		this.mApp = app;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(APP, mApp);
		return jsonObject;
	}

	public String getApp() {
		return mApp;
	}

}
