package com.xtremelabs.devicewall.protocol.memory;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xtremelabs.devicewall.protocol.Data;

public class MemoryAssign implements Data {

	private Long id;

	private ArrayList<Integer> pic_ids;

	public MemoryAssign(Long id) {
		this.id = id;
		this.pic_ids = new ArrayList<Integer>();
	}
	
	public MemoryAssign(Long id, ArrayList<Integer> pic_ids) {
		this.id = id;
		this.pic_ids = pic_ids;
	}

	@Override
	public JsonObject toJson() {

		final JsonArray jsonArray = new JsonArray();
		for (final Integer picId : pic_ids) {
			final JsonElement jsonElement = new JsonPrimitive(picId);
			jsonArray.add(jsonElement);
		}

		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", id);
		jsonObject.add("pic_ids", jsonArray);

		return jsonObject;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ArrayList<Integer> getPic_ids() {
		return pic_ids;
	}

	public void setPic_ids(ArrayList<Integer> pic_ids) {
		this.pic_ids = pic_ids;
	}
	public void setPic_idsFromCard(ArrayList<Card> cards) {
		for (final Card card : cards)
			pic_ids.add(card.getPicId().ordinal());
	}

}
