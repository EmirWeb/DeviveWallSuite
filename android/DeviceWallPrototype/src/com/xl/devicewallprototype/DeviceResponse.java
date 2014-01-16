package com.xl.devicewallprototype;

import com.google.gson.annotations.SerializedName;

public class DeviceResponse {

	@SerializedName("id")
	private int id;
	
	@SerializedName("width")
	private int width;
	
	@SerializedName("height")
	private int height;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
}
