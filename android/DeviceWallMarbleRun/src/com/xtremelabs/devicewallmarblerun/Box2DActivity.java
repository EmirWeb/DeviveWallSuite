package com.xtremelabs.devicewallmarblerun;

import org.jbox2d.common.Vec2;
import org.json.JSONException;
import org.json.JSONObject;

import com.xtremelabs.devicewallmarblerun.R;
import com.xtremelabs.devicewallmarblerun.Box2DDrawableWorld;
import com.xtremelabs.devicewallmarblerun.DrawableSurface;
import com.xtremelabs.devicewallmarblerun.engine.Box2DWorldEngine;
import com.xtremelabs.devicewallmarblerun.utils.JSONProperties;

import android.app.Activity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater.Filter;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;

public class Box2DActivity extends Activity implements OnClickListener{
	DrawableSurface mBox2DView;

	Box2DDrawableWorld drawOnCanvas;
	Box2DWorldEngine engine;

	private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 700;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.box2d_activity);
        
     // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
            	
            	boolean eraseScratch = gestureDetector.onTouchEvent(event);

//            	Log.i("scratch key", "--");
            	int action = event.getAction();
            	int actionCode = action & MotionEvent.ACTION_MASK;
            	int id = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;
           
            	if (actionCode == MotionEvent.ACTION_UP) {
            		mBox2DView.getDrawOnCanvas().scrollCheck(id, event, eraseScratch);
            	}
	            	
            	return true;
            }
        };

        
        mBox2DView = (DrawableSurface)findViewById (R.id.box2dview);
        mBox2DView.requestFocus();
        
        mBox2DView.setOnClickListener(this); 
        mBox2DView.setOnTouchListener(gestureListener);
    	
    	drawOnCanvas = mBox2DView.getDrawOnCanvas();
    	Log.i("draw?", "-" + (drawOnCanvas == null));
    }
    
    @Override
    public void onClick(View v) {
        Filter f = (Filter) v.getTag();
//        FilterFullscreenActivity.show(this, input, f);
    }
    
    public void resetSimulation(View v){
    	Log.i("YK", "Reset simulation");
    	JSONObject json = new JSONObject();
    	try {
			json.put(JSONProperties.RESET_CODE, true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	mBox2DView.getDrawOnCanvas().reset();
    	mBox2DView.getDrawOnCanvas().sendToEngine(json);
    }
    

	private class MyGestureDetector extends SimpleOnGestureListener {
		
		
        @Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			drawOnCanvas = mBox2DView.getDrawOnCanvas();
        	drawOnCanvas.scrollDrawer(e1, e2);
        	
			return false;
		}

		@Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
            	if(Math.hypot(velocityX, velocityX) > SWIPE_THRESHOLD_VELOCITY && Math.hypot(e1.getX() - e2.getX(), e1.getY() - e2.getY()) > SWIPE_MIN_DISTANCE){
            		Log.d("YK", "Vx: " + velocityX + "  Vy:" + velocityY + " e1x: " + e1.getX() + " e1y:" + e1.getY() + " e2x: " + e2.getX() + " e2y:" + e2.getY() );
            		drawOnCanvas = mBox2DView.getDrawOnCanvas();
            		
            		Vec2 pos = new Vec2((e1.getX() + e2.getX())/2, (e1.getY() + e2.getY())/2);
            		float angle = (float)Math.atan2(e1.getY() - e2.getY(), e1.getX() - e2.getX());
            		
            		if(drawOnCanvas != null){ //Log.i("","-");
            			JSONObject json = new JSONObject();
            	    	try {
            				json.put(JSONProperties.PORTAL, new Pair<Vec2, Float>(pos, Float.valueOf(angle)));
            			} catch (JSONException e) {
            				e.printStackTrace();
            			}
            	    	drawOnCanvas.sendToEngine(json);
            	    	return true;
            		}
            	}
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }
	
}
