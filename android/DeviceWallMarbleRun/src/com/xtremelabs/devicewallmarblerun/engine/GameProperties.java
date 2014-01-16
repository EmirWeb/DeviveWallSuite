package com.xtremelabs.devicewallmarblerun.engine;

import java.util.ArrayList;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import android.util.Log;

import com.xtremelabs.devicewallmarblerun.objecttypes.DrawableObject;

public class GameProperties {

	public static final int GAME_ELEMENT_PORTAL = 1;
	public static final int GAME_ELEMENT_STABLE_WALL = 2;
	public static final int GAME_ELEMENT_MOVING_WALL = 3;
	public static final int GAME_ELEMENT_GROUND = 4;
	public static final int GAME_ELEMENT_BUMPER = 5;
	public static final int GAME_ELEMENT_COIN = 6;
	public static final int GAME_ELEMENT_ACTIVE_BALL = 7;
	
	public static final int ELEMENT_DIVIDER = 100;
	
	public static final long PORTAL_GRACE_TIME = 500;

	public static final int GROUND_GREEN = 0xFF004400;

	
//	protected AABB m_worldAABB;
	private static Vec2 gravity = new Vec2(0.0f,0f);// -10.0f);
	
	private static GameProperties gProp;
	
	private static World m_world;
	
	public ArrayList<PhysicsObject> objectsList = new ArrayList<PhysicsObject>();
	
	
	public GameProperties() {
		m_world = new World(gravity, true);// TODO Auto-generated constructor stub
	}
	
	public static GameProperties getGamePropsInstance(){
		if(gProp == null)
			gProp = new GameProperties();
		
		return gProp;
	}
	
	public static World getWorld(){
//		Log.i("YK", "World=" + m_world);
		getGamePropsInstance();
		return m_world;
	}
	
	public static void reset(){
		m_world = new World(gravity, true);
	};
}
