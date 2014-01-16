package com.xtremelabs.devicewallmarblerun.objecttypes;

import org.jbox2d.common.Vec2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;
import com.xtremelabs.devicewallmarblerun.utils.ScreenProperties;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader.TileMode;
import android.util.Log;

public class PortalDrawable extends DrawableObject {

	Paint glowPaint = new Paint();
	
	Path glowPath = new Path();
	float[] glowLines = new float[4];

	private final float PORTAL_LENGTH = 10;
	public Vec2 startPoint, endPoint;
	public Vec2 position;
	public float angle = 0;

	public long lastPortalDraw = 0;
	
	private static final int PORTAL_DRAW_TIME = 150;
	
	public PortalDrawable(int paintid, JSONObject json) {
		super(paintid, json);
		
	}

	@Override
	public void drawMe(Canvas canvas) {
		resetPaths();
		canvas.drawPath(glowPath, glowPaint);
		canvas.drawLines(glowLines, usePaint);
	}
	

	private void resetPaths(){
		Vec2 st, end;

		glowLines = new float[4];
		glowPaint = new Paint();
		glowPath = new Path();
		
		startPoint = new Vec2(- (float)((PORTAL_LENGTH/2)*Math.cos(angle)),  (float)((PORTAL_LENGTH/2)*Math.sin(angle)));
		endPoint = new Vec2((float)((PORTAL_LENGTH/2)*Math.cos(angle)), - (float)((PORTAL_LENGTH/2)*Math.sin(angle)));
		
		
		if(System.currentTimeMillis() - lastPortalDraw < PORTAL_DRAW_TIME){
			float length = (PORTAL_LENGTH)*(System.currentTimeMillis() - lastPortalDraw)/(float)(PORTAL_DRAW_TIME);
//			Log.d("YK", "Length: " + length);
			st = ScreenProperties.worldToScreen(new Vec2(- (float)((length/2)*Math.cos(angle)),  (float)((length/2)*Math.sin(angle))).add(position));
			end = ScreenProperties.worldToScreen(new Vec2((float)((length/2)*Math.cos(angle)), - (float)((length/2)*Math.sin(angle))).add(position));
		}else{
			st = ScreenProperties.worldToScreen(startPoint.add(position));
			end = ScreenProperties.worldToScreen(endPoint.add(position));
		}
		
		Vec2 base = ScreenProperties.worldToScreen(endPoint);
		Vec2 top = ScreenProperties.worldToScreen(base.x + (float)(15*Math.sin(angle)), base.y + (float)(15*Math.cos(angle)));
//		Log.i("", "Glowpaint?" + (glowPaint == null));
		glowPaint.setShader(new LinearGradient(base.x, base.y, top.x, top.y, 0xFFaaaadd, 0xFFaaaadd, TileMode.MIRROR));
		
		glowPath = new Path();
		glowPath.moveTo(st.x, st.y);
		glowPath.lineTo(end.x, end.y);
		glowPath.rLineTo((float)(15*Math.sin(angle)), -(float)(15*Math.cos(angle)));
		glowPath.rLineTo(st.x - end.x, st.y - end.y);
		
		glowLines[0] = st.x;
		glowLines[1] = st.y;
		glowLines[2] = end.x;
		glowLines[3] = end.y;
	}

	
	@Override
	public void setObject(JSONObject propertiesJson) {
		super.setObject(propertiesJson);
		
		try {
			if(propertiesJson.has(JSONProperties.Portal.POSITION)){
				JSONArray pos = propertiesJson.getJSONArray(JSONProperties.Portal.POSITION);
				position = new Vec2((float)pos.getDouble(0), (float)pos.getDouble(1));
			}
			if(propertiesJson.has(JSONProperties.Portal.MOVE_TIME)){
				lastPortalDraw = propertiesJson.getLong(JSONProperties.Portal.MOVE_TIME);
			}
			if(propertiesJson.has(JSONProperties.Portal.ANGLE)){
				angle = (float) propertiesJson.getDouble(JSONProperties.Portal.ANGLE);
			}
			if(propertiesJson.has(JSONProperties.COLOR)){
				int _color = propertiesJson.getInt(JSONProperties.COLOR);
				usePaint.setColor(_color);
			}
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		resetPaths();
	}

	@Override
	public void onContact(long timestamp, Vec2 loc) {
		super.onContact(timestamp, loc);
	}

	@Override
	public void drawReflection(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vec2 getPosition() {
		return ScreenProperties.worldToScreen(position);
	}


}
