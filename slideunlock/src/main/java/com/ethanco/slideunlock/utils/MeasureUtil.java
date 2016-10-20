package com.ethanco.slideunlock.utils;

import android.content.Context;
import android.view.View;

/**
 * @Description 测量工具类
 * Created by EthanCo on 2016/10/20.
 */

public class MeasureUtil {
    public static int getViewHeight(Context context, int heightMeasureSpec, int defaultHeight) {
        int result = 0;
        int mode = View.MeasureSpec.getMode(heightMeasureSpec);
        int size = View.MeasureSpec.getSize(heightMeasureSpec);
        if (mode == View.MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = dp2px(context, defaultHeight);
            if (mode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    public static int getViewWidth(Context context, int widthMeasureSpec, int defaultWidth) {
        int result = 0;
        int mode = View.MeasureSpec.getMode(widthMeasureSpec);
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        if (mode == View.MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = dp2px(context, defaultWidth);
            if (mode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @return
     */
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
