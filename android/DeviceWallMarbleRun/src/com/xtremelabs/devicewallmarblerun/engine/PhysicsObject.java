package com.xtremelabs.devicewallmarblerun.engine;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.json.JSONException;
import org.json.JSONObject;

import com.xtremelabs.devicewallmarblerun.Box2DDrawableWorld;
import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public abstract class PhysicsObject{
	
	Box2DWorldEngine worldEngine;

	
	protected BodyDef bodyDef = new BodyDef();
	public int id = 0;
	protected Shape shape;
	protected Fixture fixture;
	public Body body;
	
	protected boolean notClean = false;
	
	protected Vec2 lastContactPlace = new Vec2(0, 0);
	protected long lastContactTime = 0;
	
	public void postInitialize(int id){
		this.id = id;
		
		GameProperties.getGamePropsInstance().objectsList.add(this);
		fixture.m_userData = this;
	}
	
	
	public abstract void onContact(Vec2 position, Vec2 velocity);
	public abstract void postProcess();
	
	
	public void anythingToSend(){
		
	}
	
	void putToDrawables(String st){
		worldEngine.putToDrawables(id, st);
	}
	
}
