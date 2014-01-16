package com.xtremelabs.devicewallmarblerun.objecttypes;


import org.json.JSONObject;

import android.graphics.Canvas;

public class BallDrawing extends CircleDrawing{

	public BallDrawing(int paintid, JSONObject json) {
		super(paintid, json);
	}
	
	@Override
	public void drawMe(Canvas canvas) {
		super.drawMe(canvas);
	}

}
