package com.xtremelabs.devicewallmarblerun.objecttypes;

import org.jbox2d.common.Vec2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xtremelabs.devicewallmarblerun.engine.GameProperties;
import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;
import com.xtremelabs.devicewallmarblerun.utils.ScreenProperties;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.util.Log;

public class RectDrawing extends DrawableObject {
	
	float width = 1;
	float height = 1;
	Vec2[] verticies = new Vec2[1];
	float BLOCK_HEIGHT = 0.5f;

	public Vec2 position = new Vec2();
	public Vec2 nearestPoint = new Vec2();
	public float angle = 0;
	
	private Vec2[] vertex = new Vec2[8];
	
	private int myGlowColor = 0;
	

	Path pathBack = new Path();
	Path pathSide = new Path();
	Path pathHeight = new Path();
	
	private static Paint faces = new Paint();
	private static Paint edges = new Paint();
	
	static{
		faces.setColor(Color.WHITE);
		faces.setStyle(Style.FILL);
		
		edges.setColor(Color.BLACK);
		edges.setStyle(Style.STROKE);
		edges.setStrokeWidth(2f);
	}
	
	{
		myGlowColor = 0xFF000000 | (int)(Math.random()*0xFFFFFF);
	}
	
	public RectDrawing(int paintid, JSONObject json) {
		super(paintid, json);
		usePaint = new Paint(faces);
	}

	@Override
	public void drawMe(Canvas canvas) {
		drawCube(0, BLOCK_HEIGHT, canvas, true);
		
	}
	
	@Override
	public void drawReflection(Canvas canvas){
		drawCube(BLOCK_HEIGHT, 2*BLOCK_HEIGHT, canvas, false);
	}
	
	private void drawCube(float near, float far, Canvas canvas, boolean aboveGround){
		int length = verticies.length;
		
		edges.setColor(aboveGround ? 0xFF555555 : 0xFFaaaaaa);
		
		pathBack.reset();
		pathSide.reset();
		pathHeight.reset();
		
		float 	mult = ScreenProperties.MULTIPLIER_CONSTANT/(ScreenProperties.NEAR_FIELD + near),
				mult2 = ScreenProperties.MULTIPLIER_CONSTANT/(ScreenProperties.NEAR_FIELD + far);

		vertex[0] = ScreenProperties.worldToScreen(ScreenProperties.rotate(verticies[0], angle).add(position).mul(mult));
		vertex[4] = ScreenProperties.worldToScreen(ScreenProperties.rotate(verticies[0], angle).add(position).mul(mult2));

		for (int i = 1; i < length; ++i) {
			vertex[i] = ScreenProperties.worldToScreen(ScreenProperties.rotate(verticies[i], angle).add(position).mul(mult));
			vertex[4+i] = ScreenProperties.worldToScreen(ScreenProperties.rotate(verticies[i], angle).add(position).mul(mult2));
		}
		
		float t = Integer.MAX_VALUE; 
		int index = 0;
		
		for(int i = 0; i < 4; i++){
			if(vertex[i].sub(ScreenProperties.getWorldMiddle()).length() < t){
				t = vertex[i].sub(ScreenProperties.getWorldMiddle()).length();
				index = i;
			}
		}
		
		nearestPoint = vertex[index];
//		Log.d("", "Picked index: " + index);
		
		pathBack.moveTo(vertex[0].x, vertex[0].y);
		pathBack.lineTo(vertex[1].x, vertex[1].y);
		pathBack.lineTo(vertex[2].x, vertex[2].y);
		pathBack.lineTo(vertex[3].x, vertex[3].y);
		pathBack.lineTo(vertex[0].x, vertex[0].y);

		if(index == 0 || index == 3){
			pathSide.moveTo(vertex[4].x, vertex[4].y);
			pathSide.lineTo(vertex[0].x, vertex[0].y);
			pathSide.lineTo(vertex[3].x, vertex[3].y);
			pathSide.lineTo(vertex[7].x, vertex[7].y);
			pathSide.lineTo(vertex[4].x, vertex[4].y);
			
		} else {	
			pathSide.moveTo(vertex[6].x, vertex[6].y);
			pathSide.lineTo(vertex[2].x, vertex[2].y);
			pathSide.lineTo(vertex[1].x, vertex[1].y);
			pathSide.lineTo(vertex[5].x, vertex[5].y);
			pathSide.lineTo(vertex[6].x, vertex[6].y);
		}
		
		if(aboveGround)canvas.drawPath(pathSide, usePaint);
		canvas.drawPath(pathSide, edges);
		
		if(index == 0 || index == 1){
			pathHeight.moveTo(vertex[4].x, vertex[4].y);
			pathHeight.lineTo(vertex[0].x, vertex[0].y);
			pathHeight.lineTo(vertex[1].x, vertex[1].y);
			pathHeight.lineTo(vertex[5].x, vertex[5].y);
			pathHeight.lineTo(vertex[4].x, vertex[4].y);
		} else {
			pathHeight.moveTo(vertex[6].x, vertex[6].y);
			pathHeight.lineTo(vertex[2].x, vertex[2].y);
			pathHeight.lineTo(vertex[3].x, vertex[3].y);
			pathHeight.lineTo(vertex[7].x, vertex[7].y);
			pathHeight.lineTo(vertex[6].x, vertex[6].y);
		}
		
		if(aboveGround)canvas.drawPath(pathHeight, usePaint);
		canvas.drawPath(pathHeight, edges);
		
		long lastDif = System.currentTimeMillis() - lastContactTime; //Log.w("", "Rect made contact,," + lastDif + " " + id + " " + (id%GameProperties.ELEMENT_DIVIDER != GameProperties.GAME_ELEMENT_GROUND));
		if(lastDif > 0 && lastDif < CONTACT_EXPIRY && id%GameProperties.ELEMENT_DIVIDER != GameProperties.GAME_ELEMENT_GROUND){
			Vec2 cont = ScreenProperties.worldToScreen(ScreenProperties.rotate(lastContactPlace, angle).add(position)); //Log.w("", "Contact hya");
			usePaint.setShader(new RadialGradient(cont.x, cont.y, lastDif + 0.01f, getContactCols(myGlowColor, (int)lastDif), contactColsPos, TileMode.CLAMP));
		} else if(usePaint.getShader() != null)
			usePaint = new Paint(faces);
		
		if(aboveGround)canvas.drawPath(pathSide, usePaint);
		canvas.drawPath(pathSide, edges);

		if(aboveGround)canvas.drawPath(pathHeight, usePaint);
		canvas.drawPath(pathHeight, edges);
		
		if(aboveGround){ 
			canvas.drawPath(pathBack, usePaint);
			canvas.drawPath(pathBack, edges);
		}
	}
	

	@Override
	public void setObject(JSONObject propertiesJson) {
		super.setObject(propertiesJson);
		
		try {
			if(propertiesJson.has(JSONProperties.Rectangle.POSITION)){
				JSONArray pos = propertiesJson.getJSONArray(JSONProperties.Rectangle.POSITION);
				position = new Vec2((float)pos.getDouble(0), (float)pos.getDouble(1));
			}
			if(propertiesJson.has(JSONProperties.Rectangle.ANGLE)){
				angle = (float) propertiesJson.getDouble(JSONProperties.Rectangle.ANGLE);
			}
			if(propertiesJson.has(JSONProperties.Rectangle.DIMENSIONS)){
				JSONArray pos = propertiesJson.getJSONArray(JSONProperties.Rectangle.DIMENSIONS);
				width = 2*(float)pos.getDouble(0);
				height = 2*(float)pos.getDouble(1);
				
				verticies = new Vec2[4];
				verticies[0] = new Vec2(-width/2, -height/2);
				verticies[1] = new Vec2(width/2, -height/2);
				verticies[2] = new Vec2(width/2, height/2);
				verticies[3] = new Vec2(-width/2, height/2);
			}
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void onContact(long timestamp, Vec2 loc) {
		super.onContact(timestamp, loc); Log.w("", "Rect made contact " + id);
	}

	@Override
	public Vec2 getPosition() {
		return nearestPoint;
	}

}
