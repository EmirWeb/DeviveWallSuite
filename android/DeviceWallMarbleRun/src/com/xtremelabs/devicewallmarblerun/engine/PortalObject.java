package com.xtremelabs.devicewallmarblerun.engine;

import java.util.ArrayList;

//import org.jbox2d.collision.PolygonDef;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader.TileMode;
import android.util.Pair;

import com.xtremelabs.devicewallmarblerun.Box2DDrawableWorld;
import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;
import com.xtremelabs.devicewallmarblerun.utils.ScreenProperties;


public class PortalObject extends PhysicsObject{
	
	private final float PORTAL_LENGTH = 10;
	public Vec2 startPoint, endPoint;
	public float angle = 0;
	
	PolygonShape polyShape = new PolygonShape();
	FixtureDef fDef = new FixtureDef();
	
	
	public PortalObject(Vec2 pos, float angle_in, int id, Box2DWorldEngine w){
		worldEngine = w;
		
		this.angle = (float) Math.toRadians(angle_in);
		
		startPoint = new Vec2(- (float)((PORTAL_LENGTH/2)*Math.cos(angle)), (float)((PORTAL_LENGTH/2)*Math.sin(angle)));
		endPoint = new Vec2((float)((PORTAL_LENGTH/2)*Math.cos(angle)), -(float)((PORTAL_LENGTH/2)*Math.sin(angle)));
		
		bodyDef.position = new Vec2(pos.x, pos.y);
		bodyDef.type = BodyType.STATIC;
		fDef.isSensor = true;
		
		ArrayList<Vec2> verts = new ArrayList<Vec2>();
		verts.add(endPoint);
		verts.add(startPoint);
		
		polyShape.set(verts.toArray(new Vec2[]{}), verts.size());
		fDef.shape = polyShape;
		
		body = GameProperties.getWorld().createBody(bodyDef);
		fixture = body.createFixture(fDef);
		
		if(id == worldEngine.PORTAL_A_ID)
			worldEngine.portalA = this;
		else
			worldEngine.portalB = this;
		
		worldEngine.lastPortalPut = id;
		
		postInitialize(id);
		
		putToDrawables("{ \"" + JSONProperties.PROPERTIES + "\": { \"" + JSONProperties.Portal.ANGLE + "\": " + angle + ", " + 
						"\"" + JSONProperties.Portal.POSITION + "\": [" + pos.x + ", " + pos.y + "] } }");
	}
	
	public void moveTo(Pair<Vec2, Float> pair){
		//reverse Y since it comes in upside down
		this.angle = pair.second;
		
		startPoint = new Vec2(- (float)((PORTAL_LENGTH/2)*Math.cos(angle)),  (float)((PORTAL_LENGTH/2)*Math.sin(angle)));
		endPoint = new Vec2((float)((PORTAL_LENGTH/2)*Math.cos(angle)), - (float)((PORTAL_LENGTH/2)*Math.sin(angle)));
		
		ArrayList<Vec2> verts = new ArrayList<Vec2>();
		verts.add(endPoint);
		verts.add(startPoint);
		
		polyShape.set(verts.toArray(new Vec2[]{}), verts.size());
		
		Vec2 pos = ScreenProperties.screenToWorld(pair.first);
		body.destroyFixture(fixture);
		fixture = body.createFixture(fDef);
		body.setTransform(new Vec2(pos.x, pos.y), 0);
		
//		lastPortalDraw = System.currentTimeMillis();
		
		fixture.m_userData = this;
		
		putToDrawables("{ \"" + JSONProperties.PROPERTIES + "\": { \"" + JSONProperties.Portal.ANGLE + "\": " + angle + ", " + 
									"\"" + JSONProperties.Portal.POSITION + "\": [" + pos.x + ", " + pos.y + "], \"" + 
									JSONProperties.Portal.MOVE_TIME + "\": " + System.currentTimeMillis() + " } }");
	}
	
	

	@Override
	public void onContact(Vec2 position, Vec2 velocity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void anythingToSend() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postProcess() {
		// TODO Auto-generated method stub
		
	}
	
}


