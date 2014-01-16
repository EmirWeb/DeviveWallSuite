package com.xtremelabs.devicewall.protocol.memory;

import com.google.gson.JsonObject;
import com.xtremelabs.devicewall.protocol.Data;

public class MemoryConfirm implements Data {
	private Long id;
	
	public MemoryConfirm(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", id);
		return jsonObject;
	}

}
