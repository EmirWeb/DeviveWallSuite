package com.xtremelabs.devicewallmarblerun.utils;

public class JSONProperties {
	
	//Common
	public static final String IDS = "ids"; 
	public static final String TIMESTAMP = "timestamp"; 
	public static final String PROPERTIES = "properties"; 
	public static final String COLOR = "color"; 
	public static final String PORTAL = "portal"; 
	public static final String RESET_CODE = "reset"; 
	public static final String CONTACT_TIME = "contact_time"; 
	public static final String CONTACT_LOCATION = "contact_location"; 
	
	
	public static class Shot{
		public static final String SHOT_RELEASE = "shot_release"; 
		public static final String START_POINT = "start_point"; 
		public static final String END_POINT = "end_point"; 
	}
	
	public static class Circle{
		public static final String RADIUS = "radius"; 
		public static final String POSITION = "position"; 
		public static final String ANGLE = "angle"; 
	}
	
	public static class Rectangle{
		public static final String POSITION = "position";
		public static final String DIMENSIONS = "dimensions";
		public static final String ANGLE = "angle"; 
	}
	
	public static class Portal{
		public static final String MOVE_TIME = "move_time"; 
		public static final String POSITION = "position";
		public static final String ANGLE = "angle";
	}

}
