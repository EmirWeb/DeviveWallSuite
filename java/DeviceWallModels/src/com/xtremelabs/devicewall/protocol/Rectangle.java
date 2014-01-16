package com.xtremelabs.devicewall.protocol;

import com.google.gson.annotations.SerializedName;


public class Rectangle {
	@SerializedName("x")
	private int x;
	
	@SerializedName("y")
	private int y;
	
	@SerializedName("width")
	private int width;
	
	@SerializedName("height")
	private int height;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

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

	public Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
