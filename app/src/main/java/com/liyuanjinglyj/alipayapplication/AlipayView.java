package com.liyuanjinglyj.alipayapplication;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class AlipayView extends View {
    private Paint paint;//画笔工具
    private Path circlePath,dstPath;//圆路径
    private PathMeasure pathMeasure;//计算路径的参数
    private float mCurrentValue;//动画执行的进度
    private int X=500,Y=500,mRadius=250;//圆心坐标

    public AlipayView(Context context) {
        super(context);
    }

    public AlipayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE,null);//关闭硬件加速
        this.paint=new Paint(Paint.ANTI_ALIAS_FLAG);//初始化画笔，且设置为抗锯齿
        this.paint.setStrokeWidth(4);//设置画笔宽度
        this.paint.setStyle(Paint.Style.STROKE);//设置描边
        this.dstPath=new Path();//初始化
        this.circlePath=new Path();//初始化
        this.circlePath.addCircle(X,Y,mRadius,Path.Direction.CW);//顺时针画圆
        //下面三行代码画的是勾的路径，对照上面分析图
        this.circlePath.moveTo(X-mRadius/2,Y);
        this.circlePath.lineTo(X,Y+mRadius/2);
        this.circlePath.lineTo(X+mRadius/2,Y-mRadius/3);
        this.pathMeasure=new PathMeasure(this.circlePath,false);//不闭合

        ValueAnimator valueAnimator=ValueAnimator.ofFloat(0,2);//这里分两段动画，一段画圆，一段画勾
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentValue=(float)animation.getAnimatedValue();//获取动画进度
                invalidate();
            }
        });
        valueAnimator.setDuration(4000);//每次动画时间
        valueAnimator.start();//执行动画
    }
    private boolean mNext=false;//判断是否是对勾
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);//首先绘制背景色为白色
        if(this.mCurrentValue<1){
            //初始化函数中说过，mCurrentValue是记录动画的进度的，而小编将动画进度设置为2，（0，1）就是画圆圈，（1，2）就是画对勾
            //而且mCurrentValue的进度是随机的，并不一定获取到1，所以别拿等等与1来计算，只要大于1就执行画对勾就行
            float stop=this.pathMeasure.getLength()*this.mCurrentValue;//计算当前进度下路径的百分比，比如，0.25画到圆的4分之一，那么整个圆绘画进度长度就在这里计算得到。
            //前面说过dstPath是截取路径，比如一张图片长200，我截取一半就有100，同样通过pathMeasure.getSegment就可以截取circlePath的当前进度路径。
            this.pathMeasure.getSegment(0,stop,dstPath,true);
        }else{
            if(!mNext){
                this.mNext=true;
                //刚说过了，动画的进度值并不一定会获取到1，有可能直接从0.99跳到1.01，那么没绘制完成的部分，就需要绘制先绘制完成
                this.pathMeasure.getSegment(0,this.pathMeasure.getLength(),dstPath,true);
                this.pathMeasure.nextContour();//因为圆与对勾并没有闭合，所以算两个路径，这句代码就是切换到对勾路径上
            }
            float stop=this.pathMeasure.getLength()*(this.mCurrentValue-1);//每条进度都是按1算百分比的， 但动画设置的是2，所以减去圆的1，单独计算勾的路径百分比
            this.pathMeasure.getSegment(0,stop,dstPath,true);
        }
        canvas.drawPath(dstPath,paint);//把截取到的路径画出来
    }

    public AlipayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
