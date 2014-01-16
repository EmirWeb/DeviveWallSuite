package com.xtremelabs.devicewallmarblerun;

import java.lang.Thread.State;

import org.jbox2d.common.Vec2;

import com.xtremelabs.devicewallmarblerun.engine.Box2DWorldEngine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.Toast;

public class DrawableSurface extends SurfaceView implements SurfaceHolder.Callback{

	SurfaceHolder holder;

	Box2DDrawableWorld drawOnCanvas;
	Box2DWorldEngine engine;
	
	private static PhysicsThread _thread;
	
	public DrawableSurface(Context context) {
		super(context);
		init(context);
	}
	public DrawableSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public DrawableSurface(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	
	
	private void init(Context context){
		getHolder().addCallback(this);
        _thread = new PhysicsThread(getHolder(), context);
        setFocusable(true);
        
	}
	
	public Box2DDrawableWorld getDrawOnCanvas(){
		return drawOnCanvas;
	}
	
	
	private Vec2 topCorner = new Vec2(0f, 0f);
	private Vec2 fraction = new Vec2(1f, 1f);
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

//		if(engine == null)
//			engine = new Box2DWorldEngine();
//		if(drawOnCanvas == null)
//			drawOnCanvas = new Box2DDrawableWorld(getContext(), width, height, fraction, topCorner, engine);
//        engine.setDrawable(drawOnCanvas);
//        engine.initialize();
        
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		if(engine == null)
			engine = new Box2DWorldEngine();
		if(drawOnCanvas == null)
			drawOnCanvas = new Box2DDrawableWorld(getContext(), getWidth(), getHeight(), fraction, topCorner, engine);
        engine.setDrawable(drawOnCanvas);
        engine.initialize();
		
		_thread.setRunning(true);
		try{
			if (_thread.getState() == Thread.State.TERMINATED) {
	            _thread = new PhysicsThread(getHolder(), getContext());
	            _thread.setRunning(true);
	            _thread.start();
	        }
	        else {
	        	_thread.setRunning(true);
	        	_thread.start();
	        }
		} catch(IllegalThreadStateException e){
            Log.d("", "Caught..: " + e.getMessage());
			
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        _thread.setRunning(false);
        while (retry) {
            try {
                _thread.join();
                retry = false;
                Log.d("", "Destroying thread: " + _thread.isAlive());
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
		
	}
	
	class PhysicsThread extends Thread {
		
	    String TAG="PhysicsThread";
	    private SurfaceHolder _surfaceHolder;
	    private boolean _run = false;
	 
	    /**
	     * State-tracking constants.
	     */
	 
	    public static final int STATE_PLAY = 0;
	    public static final int STATE_RUNNING = 1;
	    public int mState=STATE_PLAY;
	 
	    Resources mRes;
	 
	    /** Handle to the application context, used to e.g. fetch Drawables. */
	 
	    float droidx=200,droidy=200;
	 
	    public PhysicsThread(SurfaceHolder surfaceHolder, Context context) {
	        _surfaceHolder = surfaceHolder;
	        mRes = context.getResources();
	       // create droid and load bitmap
	 
	    }
	 
	    public void setRunning(boolean run) {
	        _run = run;
	    }
	 
	    @Override
	    public void run() {
	        Canvas c;
	        while (_run) {
	            c = null;
	            try {
	                c = _surfaceHolder.lockCanvas(null);
	                synchronized (_surfaceHolder) {
	                    drawFrame(c);
	                }
	            } finally {
	                // do this in a finally so that if an exception is thrown
	                // during the above, we don't leave the Surface in an
	                // inconsistent state
	                if (c != null) {
	                    _surfaceHolder.unlockCanvasAndPost(c);
	                }
	            }
	        }
	    }
	 
	    public void drawFrame(Canvas canvas) {
	    	if(canvas != null && drawOnCanvas != null){
	    		drawOnCanvas.draw(canvas);
	    		engine.processFrame();
	    		engine.sendToDrawables();
	    	}
	 
	    }
	 
	}
	

}
