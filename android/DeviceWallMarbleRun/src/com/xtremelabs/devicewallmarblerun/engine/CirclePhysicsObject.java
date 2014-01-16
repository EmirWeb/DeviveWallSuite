package com.xtremelabs.devicewallmarblerun.engine;

//import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;

import com.xtremelabs.devicewallmarblerun.Box2DDrawableWorld;
import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;
import com.xtremelabs.devicewallmarblerun.utils.ScreenProperties;



public class CirclePhysicsObject extends PhysicsObject{
	
	CircleShape circleShape = new CircleShape();
	FixtureDef fDef = new FixtureDef();
	public boolean setMove = false;
	Vec2 movePos, outVelocity;
	
	public CirclePhysicsObject(BodyType type, Vec2 pos, Vec2 velocity, float radius, float restitution, float density, int id, Box2DWorldEngine w){
		
		worldEngine = w;
		bodyDef = new BodyDef();
		bodyDef.position = new Vec2(pos.x, pos.y);
		bodyDef.type = type;
		bodyDef.linearVelocity = velocity;
				
		circleShape.m_radius = radius;
		fDef.density = density; 
		fDef.shape = circleShape;
		
		fDef.restitution = restitution;
		
		body = GameProperties.getWorld().createBody(bodyDef);
		
		if(body == null) return;
		
		fixture = body.createFixture(fDef); 

		postInitialize(id);
		
		putToDrawables("{ \"" + JSONProperties.PROPERTIES + 
											"\": { \"" + JSONProperties.Circle.RADIUS + "\": " + radius + ", " + 
											"\"" + JSONProperties.Circle.POSITION + "\": [" + pos.x + ", " + pos.y + "] } }");
	}
	
	public void setMove(PortalObject in, Vec2 velocity){
		PortalObject out;
		if(in.id == worldEngine.PORTAL_A_ID)
			out = worldEngine.portalB;
		else 
			out = worldEngine.portalA;
		
		Vec2 outPos = out.body.getPosition();
		
		float transAngle = out.angle - in.angle;
		Mat22 rot = Mat22.createRotationalTransform(transAngle);
		outVelocity = Mat22.mulTrans(rot, velocity);
		
		movePos = new Vec2(outPos.x + 1, outPos.y + 1);
		setMove = true;
		
		worldEngine.lastPortalShiftTime = System.currentTimeMillis();
	}
	
	public void moveTo(){
		body.setTransform(movePos, 0);
	
		body.setLinearVelocity(outVelocity);
		setMove = false;
	}


	public void setToMove(Vec2 ballStartPosition) {
		movePos = new Vec2(ballStartPosition.x + 1, ballStartPosition.y + 1);
		outVelocity = new Vec2(0, 0);
		setMove = true;
	}

	@Override
	public void onContact(Vec2 position, Vec2 velocity) {
		// TODO Auto-generated method stub
		
	}
	
	public void anythingToSend(){
		super.anythingToSend();
		putToDrawables("{ \"" + JSONProperties.PROPERTIES + 
				"\": { \"" + JSONProperties.Circle.RADIUS + "\": " + circleShape.m_radius + ", " + 
				"\"" + JSONProperties.Circle.POSITION + "\": [" + body.getPosition().x + ", " + body.getPosition().y + "] } }");
	}

	@Override
	public void postProcess() {
		if(setMove) moveTo();
	}

	
}

