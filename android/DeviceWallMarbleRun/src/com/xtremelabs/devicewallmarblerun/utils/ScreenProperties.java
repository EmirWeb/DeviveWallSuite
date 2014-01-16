package com.xtremelabs.devicewallmarblerun.utils;

import org.jbox2d.common.Vec2;

public class ScreenProperties {

	private static Vec2 topLeftCorner;
	private static Vec2 fraction;
	

	private static int height, width;
	private static float transX = 0;
	private static float transY = 0;
	public static final float WORLD_SCALE = 20;
	private static float scaleFactor = 20.0f;
	private static float yFlip = -1.0f; //flip y coordinate
	
	public static float NEAR_FIELD = 5f;
	public static float MULTIPLIER_CONSTANT = NEAR_FIELD/1f;
	public static final Vec2 worldMiddle = new Vec2();
	
	
	private static ScreenProperties scrProp;
	

	public static Vec2 rotate(Vec2 vec, float angle){
		return new Vec2((float)(vec.x*Math.cos(angle) - vec.y*Math.sin(angle)), (float)(vec.x*Math.sin(angle) + vec.y*Math.cos(angle)));
	}
	
	public static void initializeScreen(int _width, int _height, Vec2 _fraction, Vec2 topleft){
		
		height = _height;
		width = _width;
		
		transX = width/2;
		transY = height/2;
		
		topLeftCorner = new Vec2(WORLD_SCALE*topleft.x, WORLD_SCALE*topleft.y);
		fraction = new Vec2(_fraction.x, _fraction.y);
		
		scaleFactor = WORLD_SCALE/_fraction.x;
		
		worldMiddle.set(width/2, height/2);
	}
	
	public static ScreenProperties getGamePropsInstance(){
		if(scrProp == null)
			scrProp = new ScreenProperties();
		
		return scrProp;
	}
	
	public static Vec2 getWorldMiddle(){
		return worldMiddle;
	}
	

	public static float getScale(){
		return scaleFactor;
	}
	

	public static Vec2 worldToScreen(Vec2 world) {

//		float x = map(world.x, 0f, fraction.x, transX, transX+scaleFactor);
//		float y = map(world.y, 0f, fraction.y, transY, transY+scaleFactor);
		float x = map(world.x - topLeftCorner.x, topLeftCorner.x, topLeftCorner.x + fraction.x, transX, transX+scaleFactor);
		float y = map(world.y - topLeftCorner.y, topLeftCorner.y, topLeftCorner.x + fraction.x, transY, transY+scaleFactor);
		if (yFlip == -1.0f) y = map(y,topLeftCorner.y, topLeftCorner.y + height, topLeftCorner.y + height, topLeftCorner.y);
		return new Vec2(x, y);

	}
	public static Vec2 worldToScreen(float x, float y) {
		return worldToScreen(new Vec2(x,y));
	}
	
	public static Vec2 screenToWorld(Vec2 screen) {
		
		float x = map(screen.x, transX, transX+scaleFactor, 0f, 1f);
		float y = screen.y;
		if (yFlip == -1.0f) y = map(y,height,0f,0f,height);
		y = map(y, transY, transY+scaleFactor, 0f, 1f);
		
		return new Vec2(x,y);
		
	}
	public static Vec2 screenToWorld(float x, float y) {
		return screenToWorld(new Vec2(x,y));
	}
	
	private static float map(float value, float istart, float istop, float ostart, float ostop)
    {
    	return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }
	
}
