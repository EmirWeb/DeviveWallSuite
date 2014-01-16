package com.xtremelabs.devicewallmarblerun.engine;

//import org.jbox2d.collision.PolygonDef;
//import org.jbox2d.collision.PolygonShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.util.Log;

import com.xtremelabs.devicewallmarblerun.Box2DDrawableWorld;
import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;
import com.xtremelabs.devicewallmarblerun.utils.ScreenProperties;

public class RectPhysicsObject extends PhysicsObject{
	
	PolygonShape polyShape = new PolygonShape();
	FixtureDef fDef = new FixtureDef();
	
	public RectPhysicsObject(BodyType type, Vec2 pos, float width, float height, float angle, int id, Box2DWorldEngine w){
		worldEngine = w;
		
		bodyDef.position = new Vec2(pos.x, pos.y);
		bodyDef.type = type;
		
		polyShape.setAsBox(width, height, new Vec2(0, 0), angle);
//		Vec2[] verts = {new Vec2(-width/2, -height/2), new Vec2(-width/2, height/2), 
//						new Vec2(width/2, height/2), new Vec2(width/2, -height/2),new Vec2(-width/2, -height/2)};
//		polyShape.set(verts, 5);
		fDef.shape = polyShape;
		fDef.density = 10;

		body = GameProperties.getWorld().createBody(bodyDef);
		fixture = body.createFixture(fDef);
		
		postInitialize(id);
		
		putToDrawables("{ \"" + JSONProperties.PROPERTIES + "\": { \"" + 
									JSONProperties.Rectangle.DIMENSIONS + "\": [" + width + ", " + height + "], " + 
									"\"" + JSONProperties.Rectangle.POSITION + "\": [" + pos.x + ", " + pos.y + "] } }");
	}
	

	@Override
	public void onContact(Vec2 position, Vec2 velocity) {

		
	}


	@Override
	public void anythingToSend() {
		super.anythingToSend();
		
		if(notClean || body.getLinearVelocity().x != 0 || body.getLinearVelocity().y != 0){
			
//			Vec2[] verts = polyShape.getVertices();
//			String str = "";
//			for(Vec2 v : verts){
//				str += "[" + v.x + ", " + v.y + "],";
//			}
			
			putToDrawables("{ \"" + JSONProperties.PROPERTIES + "\": {" + "\"" + JSONProperties.Rectangle.ANGLE + "\":" + body.getAngle() + ", " + 
					"\"" + JSONProperties.Rectangle.POSITION + "\": [" + body.getPosition().x + ", " + body.getPosition().y + "] } }");
			notClean = false;
		}
	}


	@Override
	public void postProcess() {
		// TODO Auto-generated method stub
		
	}
}



