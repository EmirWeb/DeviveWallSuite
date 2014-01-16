package com.xtremelabs.devicewall.protocol.image.request;

import com.google.gson.JsonObject;
import com.xtremelabs.devicewall.protocol.Data;

public class ClientStartConfirmRequest implements Data {
	

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		return jsonObject;
	}

}
