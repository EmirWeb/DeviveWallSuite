package com.xtremelabs.devicewallmarblerun.engine;

import java.util.Stack;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.contacts.Contact;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

//import com.xtremelabs.devicewallmarblerun.Box2DDrawableWorld.CollisionContacts;
import com.xtremelabs.devicewallmarblerun.Box2DDrawableWorld;
import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;
import com.xtremelabs.devicewallmarblerun.utils.ScreenProperties;

public class Box2DWorldEngine {
	
	Box2DDrawableWorld drawableWorld;

	private Vec2 ballStartPosition = new Vec2(0, 2f);
	
	private int ID = 1;
	public int PORTAL_A_ID = 22; 
	public int PORTAL_B_ID = 44; 
	
	public long lastPortalShiftTime = 0;
	public int lastPortalPut = PORTAL_B_ID;
	
	
	public PortalObject portalA;
	public PortalObject portalB;
	
	long elapsedTime = 0;
	long startTime = 0;

	public Stack<Pair<Vec2, Float>> pendingPortalRequests = new Stack<Pair<Vec2, Float>>();
	
	JSONObject sendingJSONObject = new JSONObject();
//	JSONObject sendingJSONObjectIds = new JSONObject();
	SparseArray<String> sendingJSONStrings = new SparseArray<String>();
    
	BodyDef ground = new BodyDef();
	
	public void setDrawable(Box2DDrawableWorld drawable){
		drawableWorld = drawable;
		Log.d("", "Drawable world set");
	}
	
	
	public Box2DWorldEngine(){

	}
	
	public void initialize(){
		GameProperties.reset();
				
		new RectPhysicsObject(BodyType.STATIC, new Vec2(-35f, 0.0f), 5.0f, 25f, 0, getNewId(GameProperties.GAME_ELEMENT_GROUND), this);
		new RectPhysicsObject(BodyType.STATIC, new Vec2(35f, 0.0f), 5.0f, 25f, 0, getNewId(GameProperties.GAME_ELEMENT_GROUND), this);
	    new RectPhysicsObject(BodyType.STATIC, new Vec2(0.0f, -20.0f), 30.0f, 5.0f, 0, getNewId(GameProperties.GAME_ELEMENT_GROUND), this);
	    new RectPhysicsObject(BodyType.STATIC, new Vec2(0.0f, 20.0f), 30.0f, 5.0f, 0, getNewId(GameProperties.GAME_ELEMENT_GROUND), this);
	    
	    new RectPhysicsObject(BodyType.DYNAMIC, new Vec2(-1.5f, -8f), 1.2f, 1.2f, 0, getNewId(GameProperties.GAME_ELEMENT_STABLE_WALL), this);
	    new RectPhysicsObject(BodyType.DYNAMIC, new Vec2(8f, -4f), 1f, 4f, 0, getNewId(GameProperties.GAME_ELEMENT_STABLE_WALL), this);
	    new RectPhysicsObject(BodyType.DYNAMIC, new Vec2(-8f, -5f), 3f, 5f, 0, getNewId(GameProperties.GAME_ELEMENT_STABLE_WALL), this);

	    new RectPhysicsObject(BodyType.DYNAMIC, new Vec2(-8f, 2.3f), 1f, 2f, 0, getNewId(GameProperties.GAME_ELEMENT_STABLE_WALL), this);

	    new CirclePhysicsObject(BodyType.DYNAMIC, ballStartPosition, new Vec2(0f, 5f), 1.2f, 0.95f, 5.0f, getNewId(GameProperties.GAME_ELEMENT_ACTIVE_BALL), this);

		new PortalObject(new Vec2(6, 6), 25, PORTAL_A_ID = getNewId(GameProperties.GAME_ELEMENT_PORTAL), this);
		new PortalObject(new Vec2(-13.5f, 6), -45, PORTAL_B_ID = getNewId(GameProperties.GAME_ELEMENT_PORTAL), this);
		lastPortalPut = PORTAL_B_ID;
		
	    GameProperties.getWorld().setContactListener(new CollisionContacts());
	    
	    startTime = System.currentTimeMillis();
	    
	    Log.i("", "Finished initializing engine.");
	}
	
	public void processFrame(){
		
		if(System.currentTimeMillis() < startTime + 1000) return;
		GameProperties.getWorld().step(0.07f, 10, 8);
		
		for(PhysicsObject object : GameProperties.getGamePropsInstance().objectsList)
			object.postProcess();
		
		
		for(int i = 0; i < 2 && !pendingPortalRequests.isEmpty(); i++){
//			Log.d("YK", "Found something in pending portal");
			Pair<Vec2, Float> pair = pendingPortalRequests.pop(); 
			
			int id = (lastPortalPut == PORTAL_A_ID) ? PORTAL_B_ID : PORTAL_A_ID;
			if(id == PORTAL_A_ID)
				portalA.moveTo(pair);
			else
				portalB.moveTo(pair);
			lastPortalPut = id;
		}
		
		pendingPortalRequests.clear();
		
		for(PhysicsObject object : GameProperties.getGamePropsInstance().objectsList)
			object.anythingToSend();
//		Log.e("", "process frame...");
		
	}
	
	public int getNewId(int type){
		return (ID++)*GameProperties.ELEMENT_DIVIDER + type;
	}
	
	

	
	private class CollisionContacts implements ContactListener{
		@Override
		public void beginContact(Contact arg0) {
			Manifold mani = arg0.getManifold();

			PhysicsObject phys1 = (PhysicsObject)arg0.getFixtureA().getUserData();
			PhysicsObject phys2 = (PhysicsObject)arg0.getFixtureB().getUserData();
			
			Log.w("TAG", "Contact!" + phys1.id);
			putToDrawables(phys1.id, "{\"" + JSONProperties.CONTACT_TIME + "\": " + System.currentTimeMillis() + 
											", \"" + JSONProperties.CONTACT_LOCATION + "\": [" + mani.localPoint.x + ", " + mani.localPoint.y + "]}");
			
			PhysicsObject ballPhys = null, portalPhys = null;
			
			if(System.currentTimeMillis() - lastPortalShiftTime > GameProperties.PORTAL_GRACE_TIME)
				
				if((ballPhys = phys1).id % GameProperties.ELEMENT_DIVIDER == GameProperties.GAME_ELEMENT_ACTIVE_BALL || 
						(ballPhys = phys2).id % GameProperties.ELEMENT_DIVIDER == GameProperties.GAME_ELEMENT_ACTIVE_BALL){
					
					if((portalPhys = phys1).id % GameProperties.ELEMENT_DIVIDER == GameProperties.GAME_ELEMENT_PORTAL || 
							(portalPhys = phys2).id % GameProperties.ELEMENT_DIVIDER == GameProperties.GAME_ELEMENT_PORTAL){
						
						Log.w("TAG", "Portal hit!");
						
						putToDrawables(portalPhys.id, "{\"" + JSONProperties.PROPERTIES + "\":" +
													"{\"" + JSONProperties.COLOR + "\": " + Integer.valueOf(0xFFFF9999).intValue() + "} }");
						
						((CirclePhysicsObject)ballPhys).setMove((PortalObject)portalPhys, ((CirclePhysicsObject)ballPhys).body.getLinearVelocity());
					}
				}
			
		}


		@Override
		public void endContact(Contact arg0) {
			
			PhysicsObject phys = (PhysicsObject)arg0.getFixtureA().getUserData();
			
			if(phys != null && phys.id != GameProperties.GAME_ELEMENT_GROUND){
//				phys.basePaint = new Paint(phys.basePaint);
			}
			
			phys = (PhysicsObject)arg0.getFixtureB().getUserData();
			
			if(phys != null && phys.id != GameProperties.GAME_ELEMENT_GROUND){
//				phys.basePaint = new Paint(phys.basePaint);
			}
		}


		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	public void shootNewMarble(Vec2 start, Vec2 end){
		new CirclePhysicsObject(BodyType.DYNAMIC, end, start.sub(end), 1.2f, 0.95f, 5.0f, getNewId(GameProperties.GAME_ELEMENT_ACTIVE_BALL), this);
	}

	
	public void putToProcess(JSONObject json){

		if(json.has(JSONProperties.RESET_CODE)) reset();
		
		if(json.has(JSONProperties.PORTAL)){
			try {
				if(json.get(JSONProperties.PORTAL) instanceof Pair<?, ?>){
					pendingPortalRequests.push((Pair<Vec2, Float>)json.get(JSONProperties.PORTAL));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if(json.has(JSONProperties.Shot.SHOT_RELEASE)){
			try {
				Vec2 stVec = null, endVec = null;
				JSONObject js = json.getJSONObject(JSONProperties.Shot.SHOT_RELEASE);
				if(js.has(JSONProperties.Shot.START_POINT)){
					JSONArray str_arr = js.getJSONArray(JSONProperties.Shot.START_POINT);
					stVec = new Vec2((float)str_arr.getDouble(0), (float)str_arr.getDouble(1));
				}
				if(js.has(JSONProperties.Shot.END_POINT)){
					JSONArray end_arr = js.getJSONArray(JSONProperties.Shot.END_POINT);
					endVec = new Vec2((float)end_arr.getDouble(0), (float)end_arr.getDouble(1));
				}
				
				if(stVec != null && endVec != null)
					shootNewMarble(stVec, endVec);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} 
	}
	
	public void putToDrawables(int id, String jsonString){
			String currJson = sendingJSONStrings.get(id);
			sendingJSONStrings.put(id, currJson == null ? jsonString : currJson + ", " + jsonString);
	}
	
	public void sendToDrawables(){
		if(sendingJSONObject.names() != null)
			Log.i("", "Send to drawables: " + sendingJSONObject.toString());
		
		JSONObject totalJSON = new JSONObject();
		try {
			sendingJSONObject.put(JSONProperties.TIMESTAMP, System.currentTimeMillis());
			
			int key = 0;
			for(int i = 0; i < sendingJSONStrings.size(); i++) {
			   key = sendingJSONStrings.keyAt(i);
			   
			   JSONObject json = new JSONObject(sendingJSONStrings.get(key));
			   totalJSON.put(Integer.valueOf(key).toString(), json);
			}
			sendingJSONObject.put(JSONProperties.IDS, totalJSON);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		
		if(drawableWorld != null)
			drawableWorld.receiveFromEngine(sendingJSONObject);
		
		sendingJSONObject = new JSONObject();
		sendingJSONStrings = new SparseArray<String>();
	}

	public void reset() {
		initialize();
	}

}
