package com.xtremelabs.devicewall.protocol.gamecontrol.response;

import com.google.gson.JsonObject;
import com.xtremelabs.devicewall.protocol.Data;

public class ServerStartResponse implements Data {

	private String app;
	
	public ServerStartResponse(String app) {
		this.app = app;
	}
	
	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("app", app);
		return jsonObject;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

}
