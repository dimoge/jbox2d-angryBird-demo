package com.ritterliu.angryBirdTwo;

import android.app.Activity;
import android.os.Bundle;

public class AngryBirdActivity extends Activity {
	
	/**发射小鸟的橡皮筋最大长度*/
	public static final float RubberBandLength=50;
	
	/**待发射小鸟的初始位置*/
	public static float startX;
	public static float startY;
	
	/**点击小鸟的有效作用范围，真机调试时因为相对手指而言小鸟较小，
	 * 所以设置点中小鸟周边一定的范围，即可拖动小鸟*/
	public static float touchDistance;
	
	MySurfaceView surfaceView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        surfaceView=new MySurfaceView(this);
        setContentView(surfaceView);
    }
}