package com.xtremelabs.devicewall.protocol.memory;

import com.google.gson.JsonObject;
import com.xtremelabs.devicewall.protocol.Data;

public class MemoryFlip implements Data {

	public static final String QUEUE_NAME_KEY = "queueName";
	public static final String ACTION_KEY = "action";
	public static final String ID_KEY = "cardId";
	public static final Integer UP = 0;
	public static final Integer DOWN = 1;
	public static final Integer WIN = 2;
	
	private Integer action;
	private Integer cardId;

	public MemoryFlip(final Integer action, final Integer id) {
		this.action = action;
		this.cardId = id;
	}
	
	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(ACTION_KEY, action);
		jsonObject.addProperty(ID_KEY, cardId);
		return jsonObject;
	}

	public Integer getAction() {
		return action;
	}

	public void setAction(Integer action) {
		this.action = action;
	}
	
	public Integer getCardId() {
		return cardId;
	}

	public void setCardId(Integer id) {
		this.cardId = id;
	}
}
