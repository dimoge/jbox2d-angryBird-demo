package com.ritterliu.angryBirdTwo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class MyRect {
	float x, y, w, h,angle;
	
	Type type;

	public MyRect(float x, float y, float w, float h,Type type) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		this.type=type;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	public float getX()
	{
		return this.x;
	}
	
	public float getY()
	{
		return this.y;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}
	
	public float getWidth()
	{
		return this.w;
	}
	
	public float getHeight()
	{
		return this.h;
	}
	
	public Type getType()
	{
		return this.type;
	}
	
	public void draw(Canvas canvas, Paint paint) {
		canvas.save();
		canvas.rotate(angle, x+w/2, y+h/2);
		canvas.drawRect(new RectF(x , y  , x + w, y + h), paint);
		canvas.restore();
	}
}
