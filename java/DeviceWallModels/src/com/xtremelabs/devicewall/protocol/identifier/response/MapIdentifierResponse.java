package com.xtremelabs.devicewall.protocol.identifier.response;

import java.util.Collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.identifier.data.MobileServerIdentifierData;

public class MapIdentifierResponse implements Data {

	public static final String MAP_KEY = "map";
	@SerializedName("map")
	private final Collection<MobileServerIdentifierData> mIdentifierDatas;


	public MapIdentifierResponse(final Collection<MobileServerIdentifierData> collection) {
		mIdentifierDatas = collection;
	}

	public Collection<MobileServerIdentifierData> getMap() {
		return mIdentifierDatas;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		if (mIdentifierDatas != null) {
			final JsonArray jsonArray = new JsonArray();
			for (final MobileServerIdentifierData identifierData : mIdentifierDatas)
				jsonArray.add(identifierData.toJson());
			jsonObject.add(MAP_KEY, jsonArray);
		}
		return jsonObject;
	}
}
