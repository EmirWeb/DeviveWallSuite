package com.xtremelabs.imgrec.model;

import com.google.gson.annotations.SerializedName;

public class DeviceScreen {

	@SerializedName("id")
	private int id;
	
	@SerializedName("xRelative")
	private double xRelative;
	
	@SerializedName("yRelative")
	private double yRelative;
	
	@SerializedName("widthRelative")
	private double widthRelative;
	
	@SerializedName("heightRelative")
	private double heightRelative;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getxRelative() {
		return xRelative;
	}

	public void setxRelative(double xRelative) {
		this.xRelative = xRelative;
	}

	public double getyRelative() {
		return yRelative;
	}

	public void setyRelative(double yRelative) {
		this.yRelative = yRelative;
	}

	public double getWidthRelative() {
		return widthRelative;
	}

	public void setWidthRelative(double widthRelative) {
		this.widthRelative = widthRelative;
	}

	public double getHeightRelative() {
		return heightRelative;
	}

	public void setHeightRelative(double heightRelative) {
		this.heightRelative = heightRelative;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(heightRelative);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + id;
		temp = Double.doubleToLongBits(widthRelative);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xRelative);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yRelative);
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
		DeviceScreen other = (DeviceScreen) obj;
		if (Double.doubleToLongBits(heightRelative) != Double.doubleToLongBits(other.heightRelative))
			return false;
		if (id != other.id)
			return false;
		if (Double.doubleToLongBits(widthRelative) != Double.doubleToLongBits(other.widthRelative))
			return false;
		if (Double.doubleToLongBits(xRelative) != Double.doubleToLongBits(other.xRelative))
			return false;
		if (Double.doubleToLongBits(yRelative) != Double.doubleToLongBits(other.yRelative))
			return false;
		return true;
	}
	
	
}
