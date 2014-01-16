package com.xtremelabs.devicewallmarblerun.utils;

import org.jbox2d.common.Vec2;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class DrawingUtils {

	
	public static void drawCircle(Canvas mCanvas, Paint mPaint, Vec2 center, float radius, Vec2 absolutePosition) {
		
		center = ScreenProperties.worldToScreen(center.add(absolutePosition));
		radius *= ScreenProperties.getScale();
		mCanvas.drawCircle(center.x, center.y, radius, mPaint);
	}
	
	
	public static void drawPolygon(Canvas mCanvas, Paint mPaint, Vec2[] vertices, int vertexCount, Vec2 absolutePosition) {
		if(vertices.length < 2) return;
		Path path = new Path();
		path.moveTo(vertices[vertexCount - 1].x, vertices[vertexCount - 1].y);
		
		for (int i = 0; i < vertexCount; ++i) {
			Vec2 a = ScreenProperties.worldToScreen(vertices[i].add(absolutePosition));
			path.lineTo(a.x, a.y);
		}
		mCanvas.drawPath(path, mPaint);
	}
	
	public static void drawSegment(Canvas mCanvas, Paint mPaint, Vec2 p1, Vec2 p2, Vec2 absolutePosition) {
		
		p1 = ScreenProperties.worldToScreen(p1.add(absolutePosition));
		p2 = ScreenProperties.worldToScreen(p2.add(absolutePosition));

		mCanvas.drawLine(p1.x, p1.y, p2.x, p2.y, mPaint);
	}

}
