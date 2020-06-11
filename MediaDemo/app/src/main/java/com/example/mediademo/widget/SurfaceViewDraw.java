package com.example.mediademo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.mediademo.R;

public class SurfaceViewDraw extends SurfaceView implements SurfaceHolder.Callback2 {

    SurfaceHolder surfaceHolder;

    Paint paint;

    Bitmap bitmap;

    public SurfaceViewDraw(Context context) {
        this(context, null);
    }

    public SurfaceViewDraw(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SurfaceViewDraw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //得到控制器
        surfaceHolder = getHolder();
        //设置动态监听
        surfaceHolder.addCallback(this);

        paint = new Paint();

        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.chat_v_icon);

    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //重要的一点要说明.在这里可以在线程里面用canvas绘制图片,所以为什么SurfaceView比较时候绘制图片和图形

        // 先锁定当前surfaceView的画布
        Canvas canvas = holder.lockCanvas();
        //开始画
        canvas.drawBitmap(bitmap, new Matrix(), paint);
        //解锁画布
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
