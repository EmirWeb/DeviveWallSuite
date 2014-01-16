package com.xtremelabs.imgrec.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class VirtualScreen {

	@SerializedName("width")
	private double width;
	
	@SerializedName("height")
	private double height;
	
	@SerializedName("screens")
	private List<DeviceScreen> screens;

	
	public void addScreen(DeviceScreen screen){
		if(screens == null){
			screens = new ArrayList<DeviceScreen>();
		}
		
		screens.add(screen);
	}
	
	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public List<DeviceScreen> getScreens() {
		return screens;
	}

	public void setScreens(List<DeviceScreen> screens) {
		this.screens = screens;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((screens == null) ? 0 : screens.hashCode());
		temp = Double.doubleToLongBits(width);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VirtualScreen other = (VirtualScreen) obj;
		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return false;
		if (screens == null) {
			if (other.screens != null)
				return false;
		} else if (!screens.equals(other.screens))
			return false;
		if (Double.doubleToLongBits(width) != Double.doubleToLongBits(other.width))
			return false;
		return true;
	}
}
