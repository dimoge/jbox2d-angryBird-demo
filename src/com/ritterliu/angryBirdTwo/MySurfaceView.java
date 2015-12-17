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
	
	/**�ܵ������߳�*/
	private Thread th;
	/**�߳����б�־λ*/
	private boolean flag;
	
	private Canvas canvas;
	private Paint paint;
	
	private int screenW,screenH;
	
	/**Bird�࣬���Ի滭��С��*/
	Bird bird;
	
	/**touchEvent���Ż��������������ʱƵ����Ӧ*/
	byte[] lock = new byte[0];
	private final int timePause=50;
	
	/**������������*/
	World world;
//	AABB aabb;  //�°��JBox2D�Ѿ�����ҪAABB������
	Vec2 gravity;
	private final float RATE=30.0f; //������������Ļ�������ű���
	float timeStep=1f/60f;	
	
	/**�µ�JBox2D���ӵ��������Ƶ��������������չٷ�manual�ϵĲ������õ� */
	int velocityIterations = 10;	
	int positionIterations = 8;

	public MySurfaceView(Context context) {
		super(context);
		
		sfh=this.getHolder();
		sfh.addCallback(this);
		
		paint=new Paint();
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);

//		aabb=new AABB(); 	//�ɰ�JBox2D�Ĵ�������
//		aabb.lowerBound.set(-100, -100);
//		aabb.upperBound.set(100,100);
		
		/**������ʼ��*/
		gravity=new Vec2(0,10f);
		
		/**������������*/
		world=new World(gravity, true);
		
		/**�������������е���ײ����*/
		world.setContactListener(this);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		/**�õ���Ļ��С*/
		this.screenW=this.getWidth();
		this.screenH=this.getHeight();

		/**��ʼ��С��λ��*/
		AngryBirdActivity.startX=100;
		AngryBirdActivity.startY=screenH-100;
		/**��ʼ����Ƥ���*/
		AngryBirdActivity.touchDistance=0.2f*screenH;
		
		
		Bitmap bmpBird=BitmapFactory.decodeResource(this.getResources(), R.drawable.smallbird);
		
		bird=new Bird(AngryBirdActivity.startX,AngryBirdActivity.startY,bmpBird.getHeight()/2f,bmpBird,Type.redBird);		

		/** �������ܵı߿����� isStaticΪtrue�����������������Ǿ�ֹ�ģ�
		 * Type����Ϊground�����ⱻ����
		 * */
		createPolygon(5, 5, this.getWidth() - 10, 2, true,Type.ground);
		createPolygon(5, this.getHeight() - 10, this.getWidth() - 10, 2, true,Type.ground);
		createPolygon(5, 5, 2, this.getHeight() - 10, true,Type.ground);
		createPolygon(this.getWidth() - 10, 5, 2, this.getHeight() - 10, true,Type.ground);
		
		/**����6�����Σ�isStatic����Ϊfalse�����������������Ƕ�̬������������Ӱ�� */
		for(int i=0;i<6;i++)
		{
			createPolygon(screenW-150,screenH-50-20*i,20,20, false,Type.wood);
		}
		/**����һ�������ͣ�Ҳ�Ƕ�̬�� */
		createPolygon(screenW-180,screenH-50-20*6-10,80,10, false,Type.wood);
		
		/**�����߳�*/
		flag=true;
		th=new Thread(this);
		th.start();
		
	}
	
	/**����Բ�ε�body*/
	public Body createCircle(float x,float y,float r,boolean isStatic)
	{
		/**����body��״*/
	    CircleShape circle = new CircleShape();
	    /**�뾶��Ҫ����Ļ�Ĳ���ת�������������� */
	    circle.m_radius = r/RATE;
		
	    /**����FixtureDef */
		FixtureDef fDef=new FixtureDef();
		if(isStatic)
		{
			/**�ܶ�Ϊ0ʱ�������������в�������Ӱ�죬Ϊ��ֹ�� */
			fDef.density=0;
		}
		else
		{
			/**�ܶȲ�Ϊ0ʱ�������������л�������Ӱ�� */
			fDef.density=1;
		}
		/**����Ħ��������ΧΪ 0��1 */
		fDef.friction=1.0f;
		/**����������ײ�Ļظ�����ֵԽ������Խ�е��� */
		fDef.restitution=0.3f;
		/**�����״*/
		fDef.shape=circle;

	    /**����BodyDef */
		BodyDef bodyDef=new BodyDef();
		
		/**�˴�һ��Ҫ���ã���ʹdensity��Ϊ0��
		 * ���˴�����body.type����ΪBodyType.DYNAMIC,������ᾲֹ
		 * */
		bodyDef.type=isStatic?BodyType.STATIC:BodyType.DYNAMIC;
		/**����bodyλ�ã�Ҫ����Ļ�Ĳ���ת�������������� */
		bodyDef.position.set((x)/RATE, (y)/RATE);
		
		/**����body*/
		Body body=world.createBody(bodyDef);
		
		/**��� m_userData */
		body.m_userData=bird;
		
	//	body.createShape(fDef); //�ɰ�JBox2D�Ĵ�������
		
		/**Ϊbody����Fixture*/
		body.createFixture(fDef); 
		
	//	body.setMassFromShapes();	//�ɰ�JBox2D�Ĵ�������
		
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
				/**�ð�ɫ��仭��*/
				canvas.drawColor(Color.WHITE);
				/**����С��*/
				bird.draw(canvas, paint);

				/**���С��û�����䣬�����϶�����Ƥ��켣*/
				if(!bird.getIsReleased())
				{
					canvas.drawLine(AngryBirdActivity.startX, AngryBirdActivity.startY, bird.getX(), bird.getY(), paint);
				}

				/**�����������磬����Rect */
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
		world.step(timeStep, velocityIterations,positionIterations);// �����������ģ��

		/**�������������е�body������������������ֵ��������Ļ��
		 * �ı�bird��rect�Ĳ���
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
			else // body.m_userData==nullʱ����body���٣���ʾ������
			{
				world.destroyBody(body);
			}
			body = body.m_next;
		}
		
		/**����С����ֻ��һ�Σ�������󣬲������϶���*/
		if(bird.getIsReleased()&&!bird.getApplyForce())
		{
			/**����ʱ�Ŵ���һ��body*/
			Body birdBody=createCircle(bird.getX(),bird.getY(),bird.getR(),false);
			
			/**����bullet����Ϊtrue,�����ٶȹ���ʱ���ܻ��д�Խ���� */
			birdBody.setBullet(true);
			
			/**������������*/
			float forceRate=50f;
			
			/**������Ƥ��ȺͽǶ����÷�����*/
			float angle=(float) Math.atan2(bird.getY()-AngryBirdActivity.startY,bird.getX()-AngryBirdActivity.startX);
			float forceX=-(float) (Math.sqrt(Math.pow(bird.getX()-AngryBirdActivity.startX, 2))*Math.cos(angle));
			float forceY=-(float) (Math.sqrt(Math.pow(bird.getY()-AngryBirdActivity.startY, 2))*Math.sin(angle));
		
			Vec2 force=new Vec2(forceX*forceRate,forceY*forceRate);
			
			/**��bodyӦ�������� */
			birdBody.applyForce(force, birdBody.getWorldCenter());

			/**�����Ѿ����ù���������󣬲������϶��� */
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
			/**С��δ����ʱ�����*/
			if(bird.isPressed(event)&&!bird.getIsReleased())
			{
				bird.setIsPressed(true);
			}
		}
		else if(event.getAction()==MotionEvent.ACTION_MOVE)
		{
			/**С��δ����ʱ�϶� */
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

		/**��touchEvent���Ż�����ֹ�������ʱ����Ƶ������Ӧ */
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

		/**��ײ�¼��ļ�⣬�����ǵ��Գ����� */
		if(arg1.normalImpulses[0]>5)
		{
			if ( (arg0.getFixtureA().getBody().getUserData())instanceof MyRect)
			{

				MyRect rect=(MyRect)(arg0.getFixtureA().getBody().getUserData());

				/**ֻ���⼸�����ͻᱻ���� */
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
