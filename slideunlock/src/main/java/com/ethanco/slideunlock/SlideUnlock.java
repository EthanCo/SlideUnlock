package com.ethanco.slideunlock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * @Description 滑动解锁
 * Created by EthanCo on 2016/10/18.
 */

public class SlideUnlock extends View {

    private Bitmap bitmap;
    private Paint mBitPaint;

    public SlideUnlock(Context context) {
        super(context);
        init(context);
    }

    public SlideUnlock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlideUnlock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);

        initBitmap();
    }

    private void initBitmap() {
        //Bitmap bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.img_test)).getBitmap();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_test);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        //canvas.drawBitmap(bitmap, 0, 0, null);
        drawImage(canvas, bitmap, 0, 0, 200, 200);
    }

    /**
     * 绘制一个Bitmap
     *
     * @param canvas
     * @param bitmap
     * @param left         起始绘制的左侧坐标
     * @param top          起始绘制的顶部坐标
     * @param expectWidth  期望的宽度
     * @param expectHeight 期望的高度
     */
    public void drawImage(Canvas canvas, Bitmap bitmap, int left, int top, int expectWidth, int expectHeight) {
        //Rect src = new Rect(0, 0, 230, 230); // 是对图片进行裁截(不是缩放)，若是空null则显示整个图片
        Rect dst = new Rect(left, top, expectWidth + left, expectHeight + top); // 缩放后显示的大小

        canvas.drawBitmap(bitmap, null, dst, null);
    }

    /**
     * 绘制一个Bitmap
     *
     * @param canvas 画布
     * @param bitmap 图片
     * @param x      屏幕上的x坐标
     * @param y      屏幕上的y坐标
     */

    public void drawImage(Canvas canvas, Bitmap bitmap, int x, int y) {
        // 绘制图像 将bitmap对象显示在坐标 x,y上
        canvas.drawBitmap(bitmap, x, y, null);
    }
}
