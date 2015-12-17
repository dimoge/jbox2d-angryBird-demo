package com.ritterliu.angryBirdTwo;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import com.ritterliu.angryBird.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class MySurfaceView extends SurfaceView implements Callback,Runnable,ContactListener{

	private SurfaceHolder sfh;
	
	/**总的运行线程*/
	private Thread th;
	/**线程运行标志位*/
	private boolean flag;
	
	private Canvas canvas;
	private Paint paint;
	
	private int screenW,screenH;
	
	/**Bird类，用以绘画出小鸟*/
	Bird bird;
	
	/**touchEvent的优化，避免真机调试时频繁响应*/
	byte[] lock = new byte[0];
	private final int timePause=50;
	
	/**物理世界声明*/
	World world;
//	AABB aabb;  //新版的JBox2D已经不需要AABB区域了
	Vec2 gravity;
	private final float RATE=30.0f; //物理世界与屏幕环境缩放比列
	float timeStep=1f/60f;	
	
	/**新的JBox2D增加到两个控制迭代，参数均按照官方manual上的参数设置的 */
	int velocityIterations = 10;	
	int positionIterations = 8;

	public MySurfaceView(Context context) {
		super(context);
		
		sfh=this.getHolder();
		sfh.addCallback(this);
		
		paint=new Paint();
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);

//		aabb=new AABB(); 	//旧版JBox2D的创建方法
//		aabb.lowerBound.set(-100, -100);
//		aabb.upperBound.set(100,100);
		
		/**重力初始化*/
		gravity=new Vec2(0,10f);
		
		/**创建物理世界*/
		world=new World(gravity, true);
		
		/**增加物理世界中的碰撞监听*/
		world.setContactListener(this);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		/**得到屏幕大小*/
		this.screenW=this.getWidth();
		this.screenH=this.getHeight();

		/**初始化小鸟位置*/
		AngryBirdActivity.startX=100;
		AngryBirdActivity.startY=screenH-100;
		/**初始化橡皮筋长度*/
		AngryBirdActivity.touchDistance=0.2f*screenH;
		
		
		Bitmap bmpBird=BitmapFactory.decodeResource(this.getResources(), R.drawable.smallbird);
		
		bird=new Bird(AngryBirdActivity.startX,AngryBirdActivity.startY,bmpBird.getHeight()/2f,bmpBird,Type.redBird);		

		/** 创建四周的边框，设置 isStatic为true，即在物理世界中是静止的，
		 * Type设置为ground，避免被击毁
		 * */
		createPolygon(5, 5, this.getWidth() - 10, 2, true,Type.ground);
		createPolygon(5, this.getHeight() - 10, this.getWidth() - 10, 2, true,Type.ground);
		createPolygon(5, 5, 2, this.getHeight() - 10, true,Type.ground);
		createPolygon(this.getWidth() - 10, 5, 2, this.getHeight() - 10, true,Type.ground);
		
		/**创建6个方形，isStatic设置为false，即在物理世界中是动态，收外力作用影响 */
		for(int i=0;i<6;i++)
		{
			createPolygon(screenW-150,screenH-50-20*i,20,20, false,Type.wood);
		}
		/**创建一个长条型，也是动态的 */
		createPolygon(screenW-180,screenH-50-20*6-10,80,10, false,Type.wood);
		
		/**启动线程*/
		flag=true;
		th=new Thread(this);
		th.start();
		
	}
	
	/**创建圆形的body*/
	public Body createCircle(float x,float y,float r,boolean isStatic)
	{
		/**设置body形状*/
	    CircleShape circle = new CircleShape();
	    /**半径，要将屏幕的参数转化到物理世界中 */
	    circle.m_radius = r/RATE;
		
	    /**设置FixtureDef */
		FixtureDef fDef=new FixtureDef();
		if(isStatic)
		{
			/**密度为0时，在物理世界中不受外力影响，为静止的 */
			fDef.density=0;
		}
		else
		{
			/**密度不为0时，在物理世界中会受外力影响 */
			fDef.density=1;
		}
		/**设置摩擦力，范围为 0～1 */
		fDef.friction=1.0f;
		/**设置物体碰撞的回复力，值越大，物体越有弹性 */
		fDef.restitution=0.3f;
		/**添加形状*/
		fDef.shape=circle;

	    /**设置BodyDef */
		BodyDef bodyDef=new BodyDef();
		
		/**此处一定要设置，即使density不为0，
		 * 若此处不将body.type设置为BodyType.DYNAMIC,物体亦会静止
		 * */
		bodyDef.type=isStatic?BodyType.STATIC:BodyType.DYNAMIC;
		/**设置body位置，要将屏幕的参数转化到物理世界中 */
		bodyDef.position.set((x)/RATE, (y)/RATE);
		
		/**创建body*/
		Body body=world.createBody(bodyDef);
		
		/**添加 m_userData */
		body.m_userData=bird;
		
	//	body.createShape(fDef); //旧版JBox2D的创建方法
		
		/**为body创建Fixture*/
		body.createFixture(fDef); 
		
	//	body.setMassFromShapes();	//旧版JBox2D的创建方法
		
		return body;
	}
	
	public Body createPolygon(float x,float y,float width,float height,boolean isStatic,Type type)
	{
		PolygonShape polygon =new PolygonShape();
		
		polygon.setAsBox(width/2/RATE, height/2/RATE);
		
		FixtureDef fDef=new FixtureDef();
		if(isStatic)
		{
			fDef.density=0;
		}
		else
		{
			fDef.density=1;
		}
		fDef.friction=1.0f;
		fDef.restitution=0.0f;
		
		fDef.shape=polygon;

		BodyDef bodyDef=new BodyDef();
		
		bodyDef.type=isStatic?BodyType.STATIC:BodyType.DYNAMIC;//new
		
		bodyDef.position.set((x+width/2)/RATE,(y+height/2)/RATE );
		
		Body body=world.createBody(bodyDef);

		body.m_userData=new MyRect(x,y,width,height,type);
	
	//	body.createShape(polygonDef);
	//	body.setMassFromShapes();
		body.createFixture(fDef);
		
		return body;

	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		flag=false;
	}

	
	public void draw()
	{
		try
		{
			canvas=sfh.lockCanvas();
			if(canvas!=null)
			{
				/**用白色填充画布*/
				canvas.drawColor(Color.WHITE);
				/**画出小鸟*/
				bird.draw(canvas, paint);

				/**如果小鸟还没被发射，画出拖动的橡皮筋轨迹*/
				if(!bird.getIsReleased())
				{
					canvas.drawLine(AngryBirdActivity.startX, AngryBirdActivity.startY, bird.getX(), bird.getY(), paint);
				}

				/**遍历物理世界，画出Rect */
				Body body = world.getBodyList();
				for (int i = 1; i < world.getBodyCount(); i++) {
					if ((body.m_userData) instanceof MyRect) {
						MyRect rect = (MyRect) (body.m_userData);
						rect.draw(canvas, paint);
					}
					body = body.m_next;
				}
				
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if(canvas!=null)
			{
				sfh.unlockCanvasAndPost(canvas);
			}
		}
		
	}
	
	public void logic()
	{
		world.step(timeStep, velocityIterations,positionIterations);// 物理世界进行模拟

		/**遍历物理世界中的body，将物理世界仿真出的值反馈给屏幕，
		 * 改变bird和rect的参数
		 * */
		Body body = world.getBodyList();	
		for (int i = 1; i < world.getBodyCount(); i++) {
			if ((body.m_userData) instanceof MyRect) {
				MyRect rect = (MyRect) (body.m_userData);
				rect.setX(body.getPosition().x * RATE - (rect.getWidth()/2));
				rect.setY(body.getPosition().y * RATE - (rect.getHeight()/2));
				rect.setAngle((float)(body.getAngle()*180/Math.PI));
			}
			else if ((body.m_userData) instanceof Bird) {
					Bird bird = (Bird) (body.m_userData);
					bird.setX(body.getPosition().x * RATE );
					bird.setY(body.getPosition().y * RATE );
					bird.setAngle((float)(body.getAngle()*180/Math.PI));
			}
			else // body.m_userData==null时，将body销毁，表示被击毁
			{
				world.destroyBody(body);
			}
			body = body.m_next;
		}
		
		/**发射小鸟，且只有一次，发射过后，不能再拖动了*/
		if(bird.getIsReleased()&&!bird.getApplyForce())
		{
			/**发射时才创建一个body*/
			Body birdBody=createCircle(bird.getX(),bird.getY(),bird.getR(),false);
			
			/**设置bullet属性为true,否则速度过快时可能会有穿越现象 */
			birdBody.setBullet(true);
			
			/**发射力量控制*/
			float forceRate=50f;
			
			/**根据橡皮筋长度和角度设置发射力*/
			float angle=(float) Math.atan2(bird.getY()-AngryBirdActivity.startY,bird.getX()-AngryBirdActivity.startX);
			float forceX=-(float) (Math.sqrt(Math.pow(bird.getX()-AngryBirdActivity.startX, 2))*Math.cos(angle));
			float forceY=-(float) (Math.sqrt(Math.pow(bird.getY()-AngryBirdActivity.startY, 2))*Math.sin(angle));
		
			Vec2 force=new Vec2(forceX*forceRate,forceY*forceRate);
			
			/**对body应用作用力 */
			birdBody.applyForce(force, birdBody.getWorldCenter());

			/**设置已经作用过力，发射后，不能再拖动了 */
			bird.setApplyForce(true);
		}

	}
	
	
	@Override
	public void run() {
		while(flag)
		{
			long start=System.currentTimeMillis();
			draw();
			logic();
			long end=System.currentTimeMillis();
			
			try
			{
				if(end-start<50)
				{
					Thread.sleep(50-(end-start));
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		{
			/**小鸟未发射时点击它*/
			if(bird.isPressed(event)&&!bird.getIsReleased())
			{
				bird.setIsPressed(true);
			}
		}
		else if(event.getAction()==MotionEvent.ACTION_MOVE)
		{
			/**小鸟未发射时拖动 */
			if(bird.getIsPressed())
			{
				bird.move(event);
			}
		}
		else if(event.getAction()==MotionEvent.ACTION_UP)
		{
			if(bird.getIsPressed())
			{
				bird.setIsReleased(true);
				bird.setIsPressed(false);
			}

		}

		/**对touchEvent的优化，防止真机调试时过于频繁的响应 */
		synchronized(lock)
		{
			try
			{
				lock.wait(timePause);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return true;
		
	}

	@Override
	public void beginContact(Contact arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endContact(Contact arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact arg0, ContactImpulse arg1) {
		// TODO Auto-generated method stub

		/**碰撞事件的检测，参数是调试出来的 */
		if(arg1.normalImpulses[0]>5)
		{
			if ( (arg0.getFixtureA().getBody().getUserData())instanceof MyRect)
			{

				MyRect rect=(MyRect)(arg0.getFixtureA().getBody().getUserData());

				/**只有这几种类型会被击毁 */
				if(rect.getType()==Type.stone
				||rect.getType()==Type.wood
				||rect.getType()==Type.pig
				||rect.getType()==Type.glass)
				{
					arg0.getFixtureA().getBody().m_userData=null;
				}
			}
			
			if ( (arg0.getFixtureB().getBody().getUserData())instanceof MyRect)
			{
				
				MyRect rect=(MyRect)(arg0.getFixtureB().getBody().getUserData());

				if(rect.getType()==Type.stone
				||rect.getType()==Type.wood
				||rect.getType()==Type.pig
				||rect.getType()==Type.glass)
				{
					arg0.getFixtureB().getBody().m_userData=null;
				}
			}
		
		}
	
	}

	@Override
	public void preSolve(Contact arg0, Manifold arg1) {
		// TODO Auto-generated method stub
		
	}

}
