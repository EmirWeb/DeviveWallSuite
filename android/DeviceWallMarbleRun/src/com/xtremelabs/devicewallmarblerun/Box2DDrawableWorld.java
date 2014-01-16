package com.xtremelabs.devicewallmarblerun;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import org.jbox2d.common.Vec2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.jbox2d.dynamics.contacts.ContactPoint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.xtremelabs.devicewallmarblerun.utils.*;
import com.xtremelabs.devicewallmarblerun.engine.Box2DWorldEngine;
import com.xtremelabs.devicewallmarblerun.engine.GameProperties;
import com.xtremelabs.devicewallmarblerun.objecttypes.*;


public class Box2DDrawableWorld {
	Box2DWorldEngine engine;
	public long timestamp = 0;
	
	private SparseArray<Pair<Vec2, Vec2>> scrollMarks = new SparseArray<Pair<Vec2, Vec2>>();
	
	private SparseArray<DrawableObject> drawablesMap = new SparseArray<DrawableObject>();
	private LinkedList<DrawableObject> drawablesList = new LinkedList<DrawableObject>();

	private static Paint backgroundPaint = new Paint();
	
	private Context mContext;
	
	public Box2DDrawableWorld(Context context, int width, int height, Vec2 fraction, Vec2 topCorner, Box2DWorldEngine eng){
		mContext = context;
		initializeSimulation(width, height, fraction, topCorner);
		engine = eng;
	}
	
	public static Comparator<DrawableObject> DrawablesComparator  = new Comparator<DrawableObject>() {
		
		public int compare(DrawableObject lhs, DrawableObject rhs) {
			return (int) (rhs.getPosition().sub(ScreenProperties.getWorldMiddle()).length() 
					- lhs.getPosition().sub(ScreenProperties.getWorldMiddle()).length());
		}
	};


	private void initializeSimulation(int width, int height, Vec2 fraction, Vec2 topCorner){

		ScreenProperties.initializeScreen(width, height, fraction, topCorner);
		backgroundPaint.setColor(Color.WHITE);
		
		
//		backgroundPaint.setShader(new LinearGradient(width/2, 0, width/2, height, 0xFFeeeeee, Color.WHITE, TileMode.CLAMP));
		
//		Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.white_tile);
//		backgroundPaint.setShader(new BitmapShader(bm, TileMode.REPEAT, TileMode.REPEAT));
		
	}
	
	
	public void receiveFromEngine(JSONObject json_from){
		String[] st = {JSONProperties.TIMESTAMP, JSONProperties.IDS};
		JSONObject json = null;
		try {
			json = new JSONObject(json_from, st);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
//		try {
//			if(json.getJSONObject(JSONProperties.IDS).names() != null)
//				Log.i("YK", json.toString());
//		} catch (JSONException e1) {
//			e1.printStackTrace();
//		}
		
		try {
			if(json.has(JSONProperties.TIMESTAMP)){
				timestamp = json.getLong(JSONProperties.TIMESTAMP);
			}
			
			if(json.has(JSONProperties.IDS)){
				JSONObject ids =  json.getJSONObject(JSONProperties.IDS);

				Iterator<?> keys = ids.keys();

		        while( keys.hasNext() ){
		            String key = (String)keys.next();
		            int id = Integer.parseInt(key);
//		            Log.d("", "JSON pulling: " + id);
		            
					
		            if( ids.get(key) instanceof JSONObject ){
		            	
		            	JSONObject idSet = (JSONObject) ids.get(key);
		            	
		            	if(idSet.has(JSONProperties.CONTACT_TIME)){ Log.e("","processing contact...");
		            		JSONArray arr = idSet.getJSONArray(JSONProperties.CONTACT_LOCATION);
		            		Vec2 loc = new Vec2((float)arr.getDouble(0), (float)arr.getDouble(1));
		            		drawablesMap.get(id).onContact(idSet.getLong(JSONProperties.CONTACT_TIME), loc);
		            	}

		            	if(idSet.has(JSONProperties.PROPERTIES)){
		            		JSONObject jsonProps = idSet.getJSONObject(JSONProperties.PROPERTIES);
		            		
		            		// create a new one with that ID
		            		if(drawablesMap.get(id, null) == null){
		            			DrawableObject drawObj = null;
		            			switch (id % GameProperties.ELEMENT_DIVIDER) {
		            			
									case GameProperties.GAME_ELEMENT_ACTIVE_BALL:
										drawObj = new CircleDrawing(id, jsonProps);
										break;
									case GameProperties.GAME_ELEMENT_PORTAL:
										drawObj = new PortalDrawable(id, jsonProps);
										break;
									case GameProperties.GAME_ELEMENT_GROUND:
									case GameProperties.GAME_ELEMENT_STABLE_WALL:
									case GameProperties.GAME_ELEMENT_MOVING_WALL:
										drawObj = new RectDrawing(id, jsonProps);
										break;
	
									default:
										break;
								}
		            			
		            			drawablesMap.put(id, drawObj);
		            			drawablesList.add(drawObj);
//		            			Log.i("", "is null? " + (drawablesMap.get(drawablesMap.keyAt(0))));
		            		}
		            		drawablesMap.get(id).setObject(jsonProps);
		            	}
		            }
		        }
			}
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	public void drawScrolls(Canvas canvas){
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(3f);
		
		synchronized (scrollMarks) {
			
			int key = 0;
			for(int i = 0; i < scrollMarks.size(); i++) {
			   key = scrollMarks.keyAt(i); //Log.i("scratch key", key + "");
			   Pair<Vec2, Vec2> scratch  = scrollMarks.get(key);
				canvas.drawLine(scratch.first.x, scratch.first.y, scratch.second.x, scratch.second.y, paint);
			}
			
		}
	}
	
	
	
	public void scrollDrawer(MotionEvent e1, MotionEvent e2){
		synchronized (scrollMarks) {
			scrollMarks.put(e1.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT, new Pair<Vec2, Vec2>(new Vec2(e1.getX(), e1.getY()), new Vec2(e2.getX(), e2.getY())));
		}
	}
	
	public void scrollCheck(int id, MotionEvent ev, boolean erase){
		if(erase){
			scrollMarks.remove(id);
			return;
		}
		
		Pair<Vec2, Vec2> scratch  = scrollMarks.get(id);
//		Log.i("scratch check", id + "scratch check");
		if(scratch != null){
			scrollMarks.remove(id);
			
			Vec2 start = ScreenProperties.screenToWorld(scratch.first), end = ScreenProperties.screenToWorld(scratch.second);
			
		 	String js = "{\"" + JSONProperties.Shot.SHOT_RELEASE + 
	    			"\": {\"" + JSONProperties.Shot.START_POINT + "\":[" + start.x + ", " + start.y + "], \"" + 
		 						JSONProperties.Shot.END_POINT + "\":[" + end.x + ", " + end.y + "]} }";
		 	
		 	JSONObject json = null;
		 	try {
				json = new JSONObject(js);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		 	sendToEngine(json);
		}
	}
	
	
	
	public void draw(Canvas canvas){
		
		Collections.sort(drawablesList, DrawablesComparator);
		
		canvas.drawPaint(backgroundPaint);
		Paint paint = new Paint();
		for(DrawableObject obj : drawablesList) {
		   obj.drawReflection(canvas);
//		   canvas.drawLine(obj.getPosition().x, 
//						   obj.getPosition().y, 
//						   ScreenProperties.getWorldMiddle().x, ScreenProperties.getWorldMiddle().y, paint);
		}
		
		for(DrawableObject obj : drawablesList) {
		   obj.drawMe(canvas);
		}
		drawScrolls(canvas);
	}

	public void sendToEngine(JSONObject json){
		if(json != null)
			engine.putToProcess(json);
	}
	
	public void reset(){
		drawablesMap = new SparseArray<DrawableObject>();
		scrollMarks = new SparseArray<Pair<Vec2, Vec2>>();
		drawablesList = new LinkedList<DrawableObject>();
	}

}