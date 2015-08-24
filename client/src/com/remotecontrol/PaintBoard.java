package com.remotecontrol;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;

public class PaintBoard extends View {
	
	private Canvas mCanvas;
	private	Bitmap mBitmap;
	
	private Paint mPaint;
	
	private int lastX;
	private int lastY;
	
	private final Path mPath = new Path();

    private float mCurveEndX;
    private float mCurveEndY;

    private int mInvalidateExtraBorder = 10;
    
    static final float TOUCH_TOLERANCE = 8;

    private static final boolean RENDERING_ANTIALIAS = true;
    private static final boolean DITHER_FLAG = true;

    private int mCertainColor = 0xFF000000;
    private int mBackColor = 0xFFF6E980;
    private float mStrokeWidth = 4;
	
    private int mWidth;
    private int mHeight;
    
    private CR C_cr;
    private Vector< LinkedList<coord>> v_stroke;
    LinkedList<coord> l_coord;
    
    public PaintBoard(Context context) {
		super(context);
		v_stroke = new Vector< LinkedList<coord>>(10, 30);
		init();
	}
	
	public PaintBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		v_stroke = new Vector< LinkedList<coord>>(10, 30);
		init();
	}

   
	
	private void init()
	{
		mPaint = new Paint();
		mPaint.setAntiAlias(RENDERING_ANTIALIAS);
		mPaint.setColor(mCertainColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(mStrokeWidth);
		mPaint.setDither(DITHER_FLAG);

		
		lastX = -1;
		lastY = -1;
		
		C_cr = new CR();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		if (w > 0 && h > 0) {
			
			mWidth = w;
			mHeight = h;
			newImage();
		}
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}

	}



	public void newImage()
	{
		Bitmap img = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas();
		canvas.setBitmap(img);
		
		mBitmap = img;
		mCanvas = canvas;

		drawBackground(mCanvas);
		
		//changed = false;
		invalidate();
	}
	
	public void drawBackground(Canvas canvas)
	{
		if (canvas != null) {
			canvas.drawColor(mBackColor);
		}
	}



	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_UP:
				//changed = true;
				
				Rect rect = touchUp(event, false);
				if (rect != null) {
                    invalidate(rect);
                }

		        mPath.rewind();
		        
                return true;
                
			case MotionEvent.ACTION_DOWN:
								
				rect = touchDown(event);
				if (rect != null) {
                    invalidate(rect);
                }
                
				return true;
                
			case MotionEvent.ACTION_MOVE:
				rect = touchMove(event);
                if (rect != null) {
                    invalidate(rect);
                }

                return true;
		}

		return false;
	}
		
	private Rect touchDown(MotionEvent event) {

        int x = (int)event.getX();
        int y = (int)event.getY();

        lastX = x;
        lastY = y;

        Rect mInvalidRect = new Rect();
        mPath.moveTo(x, y);

        final int border = mInvalidateExtraBorder;
        mInvalidRect.set( x - border, y - border, x + border, y + border);

        mCurveEndX = x;
        mCurveEndY = y;

        mCanvas.drawPath(mPath, mPaint);

        /*
        if( v_stroke.size() > 0)
        {
        	if( v_stroke.get( v_stroke.size() - 1).size() < 2)
        	{
        		for( int n1 = 0 ; n1 < v_stroke.get( v_stroke.size() - 1).size() ; n1++)
        		{
        			v_stroke.get( v_stroke.size() - 1).clear();
        			v_stroke.remove( v_stroke.size() - 1);
        		}
        	}
        }
        */
        
        coord now_coord = new coord();
		l_coord = new LinkedList<coord>();
		v_stroke.add( l_coord);
		now_coord.x = x;
		now_coord.y = y;
		l_coord.add( now_coord);
        
        return mInvalidRect;
    }
	
	private Rect touchMove(MotionEvent event) {
        Rect rect = processMove(event);
        
        return rect;
    }	

    private Rect touchUp(MotionEvent event, boolean cancel) {	// 마지막 수정부분 후에 에러나면 확인할것
    	Rect rect = processMove(event);

        if( v_stroke.get( v_stroke.size() - 1).size() < 2)
        {
       		v_stroke.get( v_stroke.size() - 1).clear();
       		v_stroke.remove( v_stroke.size() - 1);
        }
    	
        return rect;
    }
    
    private Rect processMove(MotionEvent event) {

    	final int x = (int)event.getX();
        final int y = (int)event.getY();

        final float dx = Math.abs(x - lastX);
        final float dy = Math.abs(y - lastY);

        Rect mInvalidRect = new Rect();
       // if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        if( Math.sqrt((dx * dx) + (dy * dy)) > TOUCH_TOLERANCE) {
            final int border = mInvalidateExtraBorder;
            mInvalidRect.set((int) mCurveEndX - border, (int) mCurveEndY - border,
                    (int) mCurveEndX + border, (int) mCurveEndY + border);

            float cX = mCurveEndX = (x + lastX) / 2;
            float cY = mCurveEndY = (y + lastY) / 2;

            mPath.quadTo(lastX, lastY, cX, cY);

            // union with the control point of the new curve
            mInvalidRect.union((int) lastX - border, (int) lastY - border,
                    (int) lastX + border, (int) lastY + border);

            // union with the end point of the new curve
            mInvalidRect.union((int) cX - border, (int) cY - border,
                    (int) cX + border, (int) cY + border);

            lastX = x;
            lastY = y;

            mCanvas.drawPath(mPath, mPaint);
            
			coord now_coord = new coord();
			now_coord.x = (int)x;
			now_coord.y = (int)y;
			l_coord.add( now_coord);
        }

        return mInvalidRect;
    }
	
    public String callCR()
    {
    	
    	return C_cr.find_CR( v_stroke);
    }
    
    public void clearVector()
    {
		Enumeration< LinkedList<coord>> my_enum = v_stroke.elements();
		while( my_enum.hasMoreElements()){
			LinkedList<coord> temp = my_enum.nextElement();
			temp.clear();
		}
		v_stroke.clear();
    	C_cr.clearVector();
    }
}
