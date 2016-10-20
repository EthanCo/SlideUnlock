package com.ethanco.slideunlock;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 滑动解锁
 * Created by EthanCo on 2016/10/18.
 */

public class SlideUnlock extends View {

    public static final int mRadius = 30;
    public static final String TAG = "Z-SlideUnlock";
    private Bitmap bitmapNormal;
    private Paint mBitPaint;
    private int mHeight;
    private int mWidth;
    private float mCenterX;
    private float mCenterY;
    private RectF pointRectF;
    private PointF centerPoint;
    PointF currPoint;
    private float farestDistance = 250; //最大距离
    private float preLockDistance = 200; //解锁距离

    public static final int NORMAL = 0;
    public static final int PRESS = 1;
    public static final int UNLOCK = 2;
    public static final int UNLOCKED = 3;
    private Bitmap bitmapPress;
    private Bitmap bitmapUnLock;
    private int normalKeyholeRes;
    private int pressKeyholeRes;
    private int unlockKeyholeSrc;

    @IntDef({NORMAL, PRESS, UNLOCK, UNLOCKED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LockStatus {
    }

    @LockStatus
    private int currStatus = NORMAL;

    public int getCurrStatus() {
        return currStatus;
    }

    public void setCurrStatus(@LockStatus int currStatus) {
        this.currStatus = currStatus;
    }

    private Bitmap getPointBitmap() {
        if (currStatus == NORMAL) {
            return bitmapNormal;
        } else if (currStatus == PRESS) {
            return bitmapPress;
        } else {
            return bitmapUnLock;
        }
    }

    public SlideUnlock(Context context, AttributeSet attrs) {
        super(context, attrs);

        initVar(context, attrs);
        init(context);
    }

    public SlideUnlock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar(context, attrs);
        init(context);
    }

    private void initVar(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlideUnLock);
        normalKeyholeRes = ta.getResourceId(R.styleable.SlideUnLock_normalKeyholeSrc, R.drawable.ic_launcher);
        pressKeyholeRes = ta.getResourceId(R.styleable.SlideUnLock_pressKeyholeSrc, R.drawable.ic_launcher);
        unlockKeyholeSrc = ta.getResourceId(R.styleable.SlideUnLock_unlockKeyholeSrc, R.drawable.ic_launcher);
        farestDistance = ta.getDimension(R.styleable.SlideUnLock_farestDistance, DisplayUtil.dip2px(getContext(), 250));
        preLockDistance = ta.getDimension(R.styleable.SlideUnLock_preLockDistance, DisplayUtil.dip2px(getContext(), 200));
        ta.recycle();
    }

    private void init(Context context) {
        initPaint();
        initBitmap();
    }

    private void initPaint() {
        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true); //对位图进行防锯齿
        mBitPaint.setDither(true);
    }

    private void initBitmap() {
        bitmapNormal = BitmapFactory.decodeResource(getResources(), normalKeyholeRes);
        bitmapPress = BitmapFactory.decodeResource(getResources(), pressKeyholeRes);
        bitmapUnLock = BitmapFactory.decodeResource(getResources(), unlockKeyholeSrc);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = currPoint.x - mRadius;
        float top = currPoint.y - mRadius;
        float expectWidth = mRadius * 2;
        float expectHeight = mRadius * 2;
        BitmapUtil.drawImage(canvas, getPointBitmap(), left, top, expectWidth, expectHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHeight = getHeight();
        mWidth = getWidth();
        mCenterX = getHeight() / 2.0F;
        mCenterY = getWidth() / 2.0F;
        centerPoint = new PointF(mCenterX, mCenterY);
        currPoint = new PointF(mCenterX, mCenterY);
        pointRectF = new RectF(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isKeyholeScope(event)) {
                    setCurrStatus(PRESS);
                    updateKeyhole(event.getX(), event.getY());
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                double distance = GeometryUtil.getDistanceBetween2Points(centerPoint, new PointF(event.getX(), event.getY()));
                if (distance <= farestDistance) {
                    if (distance > preLockDistance) {
                        setCurrStatus(UNLOCK);
                    } else {
                        if (getCurrStatus() != PRESS) setCurrStatus(PRESS);
                    }
                    updateKeyhole(event.getX(), event.getY());
                } else {
                    unlockCallback();
                }
                break;
            case MotionEvent.ACTION_UP:
                final PointF fixedPoint = new PointF(currPoint.x, currPoint.y);
                distance = GeometryUtil.getDistanceBetween2Points(centerPoint, currPoint);
                if (distance > preLockDistance) {
                    setCurrStatus(UNLOCKED);
                    invalidate();
                    unlockCallback();
                } else {
                    setCurrStatus(NORMAL);
                    startBackAnim(fixedPoint);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 触碰点在锁孔(按钮)范围之内
     *
     * @param event
     * @return
     */
    private boolean isKeyholeScope(MotionEvent event) {
        return event.getX() > pointRectF.left && event.getX() < pointRectF.right
                && event.getY() > pointRectF.top && event.getY() < pointRectF.bottom;
    }

    private void startBackAnim(final PointF fixedPoint) {
        ValueAnimator backAnim = ValueAnimator.ofFloat(0, 1);
        backAnim.setDuration(750);
        backAnim.setInterpolator(new BounceInterpolator());
        backAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = (float) animation.getAnimatedValue();
                PointF p = GeometryUtil.getPointByPercent(fixedPoint, centerPoint, percent);
                updateKeyhole(p.x, p.y);
            }
        });
        backAnim.start();
    }

    private void updateKeyhole(float x, float y) {
        currPoint.set(x, y);
        invalidate();
    }

    public interface OnUnlockListener {
        void onUnlock();
    }

    List<OnUnlockListener> unlockListeners = new ArrayList<>();

    public void addUnlockListeners(OnUnlockListener unlockListner) {
        if (!unlockListeners.contains(unlockListner)) {
            unlockListeners.add(unlockListner);
        }
    }

    public void unlockCallback() {
        for (OnUnlockListener unlockListener : unlockListeners) {
            unlockListener.onUnlock();
        }
    }
}
