package com.xtremelabs.devicewallmarblerun.objecttypes;

import org.jbox2d.common.Vec2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;

import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;
import com.xtremelabs.devicewallmarblerun.utils.ScreenProperties;

public class CircleDrawing extends DrawableObject{
	
	static Paint ballPaintShadow = new Paint();
//	int[] cols = {0xFFFFFFFF, 0xFF662244, 0xFF442244};
	int[] cols = new int[3];// = {0xFFFFFFFF, 0xFF000000 | (int)(Math.random()*0xFFFFFF), 0xFF000000 | (int)(0xFF333333) };
	
	{
		ballPaintShadow.setColor(0xFF558888);
		ballPaintShadow.setStyle(Style.FILL);
		ballPaintShadow.setShader(new RadialGradient(20, 20, 20, 0xFF555555, 0xFF998599, TileMode.CLAMP));
		
		cols[0] = 0xFFFFFFFF;
		cols[1] = 0xFF000000 | (int)(Math.random()*0xFFFFFF);
		cols[2] = Color.rgb(Color.red(cols[1])/3, Color.green(cols[1])/3, Color.blue(cols[1])/3);
	}
	

	public CircleDrawing(int paintid, JSONObject json) {
		super(paintid, json);
	}


	public float angle = 0;
	public Vec2 position = new Vec2();
	public float radius;
	
	public float BALL_HEIGHT = 0.5f;
	
	@Override
	public void drawMe(Canvas canvas) {
		drawCircle(BALL_HEIGHT, canvas, true);
	}
	
	public void drawCircle(float height, Canvas canvas, boolean aboveGround){
		float[] pos = {0, 0.7f, 1f};
		float rot = (float) Math.toRadians(angle);

		Vec2 center = ScreenProperties.worldToScreen(position.mul(ScreenProperties.MULTIPLIER_CONSTANT/(ScreenProperties.NEAR_FIELD + height)));// Log.d("YK", "Rotation: " + rot);
//		
		usePaint.setShader(new RadialGradient(center.x + (float)(10*Math.cos(rot)), center.y + (float)(10*Math.sin(rot)), 20, cols, pos, TileMode.CLAMP));
		canvas.drawCircle(center.x, center.y, radius*ScreenProperties.getScale(), aboveGround ? usePaint : ballPaintShadow);
	}


	@Override
	public void setObject(JSONObject propertiesJson) {
		super.setObject(propertiesJson);
		
		try {
			if(propertiesJson.has(JSONProperties.Circle.RADIUS))
				radius = (float) propertiesJson.getDouble(JSONProperties.Circle.RADIUS);
//				BALL_HEIGHT = 0.5f + radius;
			if(propertiesJson.has(JSONProperties.Circle.POSITION)){
				JSONArray pos = propertiesJson.getJSONArray(JSONProperties.Circle.POSITION);
				position = new Vec2((float)pos.getDouble(0), (float)pos.getDouble(1));
			}
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}


	@Override
	public void onContact(long timestamp, Vec2 loc) {
		super.onContact(timestamp, loc);
	}


	@Override
	public void drawReflection(Canvas canvas) {
		drawCircle(BALL_HEIGHT+0.2f, canvas, false);
	}

	@Override
	public Vec2 getPosition() {
		return ScreenProperties.worldToScreen(position);
	}

}
