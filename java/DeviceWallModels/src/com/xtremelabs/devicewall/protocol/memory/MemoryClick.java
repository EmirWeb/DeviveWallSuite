package com.xtremelabs.devicewall.protocol.memory;

import com.google.gson.JsonObject;
import com.xtremelabs.devicewall.protocol.Data;

public class MemoryClick implements Data {
	
	private Integer cardId;
	
	public MemoryClick(Integer id) {
		this.cardId = id;
	}

	public int getCardId() {
		return cardId;
	}

	public void setCardId(Integer id) {
		this.cardId = id;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("cardId", cardId);
		return jsonObject;
	}
}
