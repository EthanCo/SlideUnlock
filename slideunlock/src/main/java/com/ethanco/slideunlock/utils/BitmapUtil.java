package com.ethanco.slideunlock.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * @Description Bitmap 工具类
 * Created by EthanCo on 2016/10/20.
 */

public class BitmapUtil {
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
    public static void drawImage(Canvas canvas, Bitmap bitmap, float left, float top, float expectWidth, float expectHeight) {
        //Rect src = new Rect(0, 0, 230, 230); // 是对图片进行裁截(不是缩放)，若是空null则显示整个图片
        RectF dst = new RectF(left, top, expectWidth + left, expectHeight + top); // 缩放后显示的大小

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

    public static void drawImage(Canvas canvas, Bitmap bitmap, int x, int y) {
        // 绘制图像 将bitmap对象显示在坐标 x,y上
        canvas.drawBitmap(bitmap, x, y, null);
    }
}
