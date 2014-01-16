package com.xtremelabs.devicewall.protocol.image.data;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.Rectangle;

public class ImageData implements Data {
	@SerializedName("show_rect")
	private final Rectangle showRect;

	@SerializedName("image_url")
	private final String imageUrl;

	@SerializedName("image_width")
	private final int imageWidth;

	@SerializedName("image_height")
	private final int imageHeight;

	public String getImageUrl() {
		return imageUrl;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public Rectangle getShowRect() {
		return showRect;
	}

	@Override
	public JsonObject toJson() {
		return null;
	}

	public ImageData(Rectangle showRect, String imageUrl, int imageWidth, int imageHeight) {
		this.showRect = showRect;
		this.imageUrl = imageUrl;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}
}
