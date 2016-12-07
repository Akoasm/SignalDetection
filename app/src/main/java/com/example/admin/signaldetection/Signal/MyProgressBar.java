package com.example.admin.signaldetection.Signal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by 王小川 on 2016/11/29.
 */
public class MyProgressBar extends View {
    Paint p;//画笔
    int backgroundColor;//背景色
    int srcColor;//前景色
    float max=100;
    double nowprogress;//当前进度
    RectF rectF;
    int viewWidth,viewHeight;
    public MyProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        p=new Paint();
        max=50;
        backgroundColor = Color.parseColor("#eaf5fb");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         viewWidth = MeasureSpec.getSize(widthMeasureSpec);
         viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        rectF = new RectF(0, 0, viewWidth, viewHeight);
        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        p.setColor(backgroundColor);//设置背景
        canvas.drawRoundRect(rectF,20,20,p);
        p.setColor(srcColor);//设置前景
        Log.i("msg",viewWidth+"\t"+nowprogress+"\t"+max+"");
        RectF rectf2=new RectF(0,0,(float)( viewWidth*nowprogress/max),viewHeight);

//        Log.i("msg",viewWidth*nowprogress/x+"");
        canvas.drawRoundRect(rectf2,20,20,p);//绘制表层
    }
    //改颜色的
    public void setProgress(float progress){
        nowprogress=progress;
        if(progress<=5){
            srcColor=Color.parseColor("#e84c3d");
        }else if(progress<=25){
            srcColor=Color.parseColor("#3099d3");
        }else {
            srcColor=Color.parseColor("#1bbc9b");
        }
        postInvalidate();
    }
}
