package com.xtremelabs.devicewallmarblerun.objecttypes;

import org.jbox2d.common.Vec2;
import org.json.JSONException;
import org.json.JSONObject;

import com.xtremelabs.devicewallmarblerun.engine.GameProperties;
import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;

public abstract class DrawableObject {
	
	int id = 0;
	
	Paint ballPaint = new Paint();
	static Paint groundPaint = new Paint();
	static Paint lightGreenPaint = new Paint();
	static Paint defaultPaint = new Paint();
	static Paint portalPaint = new Paint();

	protected long CONTACT_EXPIRY = 1000;
	public final int[] contactCols = {0xFF99aaaa, Color.WHITE, 0xFF99aaaa, Color.WHITE};
	public final float[] contactColsPos = {0, 0.25f, 0.5f, 1f};
	
	public Paint basePaint, usePaint;
	
	public long lastContactTime = 0;
	public Vec2 lastContactPlace = new Vec2(0, 0);
	
	private long timestamp = 0;
	
	static{


	    lightGreenPaint.setStyle(Style.FILL);
	    lightGreenPaint.setColor(0xFF006622);
		
		groundPaint.setStyle(Style.FILL);
		groundPaint.setColor(GameProperties.GROUND_GREEN);

		portalPaint.setStyle(Style.STROKE);
		portalPaint.setStrokeWidth(3f);
		portalPaint.setColor(0xFFFFFFFF);
	}
	
	{
	    ballPaint.setStyle(Style.FILL);
//	    ballPaint.setShader(new RadialGradient(20, 20, 20, Color.WHITE, 0xFF000000 | (int)(0xFFFFFF*Math.random())/*0xFF442244*/, TileMode.CLAMP));
	    ballPaint.setShader(new RadialGradient(20, 20, 20, Color.WHITE, 0xFF000000 | (int)(0xFFFFFF*Math.random())/*0xFF442244*/, TileMode.CLAMP));
	}
	
	protected int[] getContactCols(int col, int time){
		
		float fraction = (float)time/(float)CONTACT_EXPIRY;
		int r = Color.red(col), g = Color.green(col), b = Color.blue(col);
		
		int newCol = Color.rgb(r + (int)(fraction*(0xFF - r)), g + (int)(fraction*(0xFF - g)), b + (int)(fraction*(0xFF - b)));
		int[] contactCols = {newCol, Color.WHITE, newCol, Color.WHITE};
		return contactCols;
	}
	
	public DrawableObject(int paintid, JSONObject json) {
		id = paintid;
		setPaint(paintid);
		setObject(json);
	}
	
	private void setPaint(int paintid){
		
		switch(paintid % GameProperties.ELEMENT_DIVIDER){
			case GameProperties.GAME_ELEMENT_GROUND:
			case GameProperties.GAME_ELEMENT_STABLE_WALL:
				basePaint = new Paint(groundPaint);
				usePaint = new Paint(groundPaint);
				break;
			case GameProperties.GAME_ELEMENT_MOVING_WALL:
				basePaint = new Paint(lightGreenPaint);
				usePaint = new Paint(lightGreenPaint);
				break;
			case GameProperties.GAME_ELEMENT_PORTAL:
				basePaint = new Paint(portalPaint);
				usePaint = new Paint(portalPaint);
				break;
			case GameProperties.GAME_ELEMENT_ACTIVE_BALL:
				basePaint = new Paint(ballPaint);
				usePaint = new Paint(ballPaint);
				break;
		}
	}
	
	public void setObject(JSONObject propertiesJson){
		if(propertiesJson.has(JSONProperties.TIMESTAMP)){
			try {
				timestamp = propertiesJson.getLong(JSONProperties.TIMESTAMP);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
	
	public abstract void drawMe(Canvas canvas);
	
	public abstract void drawReflection(Canvas canvas);
	
	public abstract Vec2 getPosition();
	
	public void onContact(long timestamp, Vec2 loc){
		lastContactTime = timestamp;
		lastContactPlace = loc;
	}
	
}
