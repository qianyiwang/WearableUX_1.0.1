package net.qianyiw.wearableux_101;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ProgressBar;

import java.util.ArrayList;

/**
 * Created by wangqianyi on 2016-10-18.
 */
public class MyProgressBar extends ProgressBar {

    Boolean touchDown = false, touchUp = false;
    int x1, y1, x2;
    ArrayList xPosition1 = new ArrayList();
    ArrayList xPosition2 = new ArrayList();
    public ArrayList<PointF> graphics = new ArrayList<PointF>();

    public MyProgressBar(Context context) {
        super(context);
    }

    public MyProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        Paint p = new Paint();
//            p.setStrokeWidth(10);
//            p.setColor(Color.RED);
//            for (int i=0;i<xPosition1.size();i++){
//                canvas.drawLine((float) xPosition1.get(i), getHeight()/2, (float)xPosition2.get(i), getHeight()/2,p);
//            }
        p.setColor(Color.RED);
//        p.setStrokeJoin(Paint.Join.ROUND);
//        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(10);
        for (PointF point : graphics) {
//            canvas.drawPoint(point.x, point.y, p);
            canvas.drawLine(point.x, point.y-150, point.x, point.y, p);
        }

    }

    public void setDown(){
        this.touchDown = true;
        x1 = getProgress();
//        y1 = getHeight()/2;
        y1 = getHeight();
//        Log.v("x1", String.valueOf(x1/4));
//        xPosition1.add((float)x1/4);
//        this.touchDown = true;
        graphics.add(new PointF(x1/4,y1));

        invalidate(); //re-paint area
    }
    public void setUp(){
//        this.touchDown = false;
//        x2 = getProgress();
//        Log.v("x2", String.valueOf(x2/4));
//        xPosition2.add((float)x2/4);
//        this.touchUp = true;
    }
}
