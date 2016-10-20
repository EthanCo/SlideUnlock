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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
    boolean isTouchDown = false;
    private float farestDistance = 250; //最大距离
    private float lockDistance = 200; //解锁距离

    public static final int NORMAL = 0;
    public static final int PRESS = 1;
    public static final int UNLOCK = 2;
    public static final int UNLOCKED = 3;
    private Bitmap bitmapPress;
    private Bitmap bitmapUnLock;
    private int normalImageRes;
    private int pressImageRes;
    private int unlockImageSrc;

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
        normalImageRes = ta.getResourceId(R.styleable.SlideUnLock_normalImageSrc, R.drawable.ic_launcher);
        pressImageRes = ta.getResourceId(R.styleable.SlideUnLock_pressImageSrc, R.drawable.ic_launcher);
        unlockImageSrc = ta.getResourceId(R.styleable.SlideUnLock_unlockImageSrc, R.drawable.ic_launcher);
        farestDistance = ta.getDimension(R.styleable.SlideUnLock_farestDistance, DisplayUtil.dip2px(getContext(), 250));
        lockDistance = ta.getDimension(R.styleable.SlideUnLock_lockDistance, DisplayUtil.dip2px(getContext(), 200));
        ta.recycle();
    }

    private void init(Context context) {
        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true); //对位图进行防锯齿
        mBitPaint.setDither(true);

        initBitmap();
    }

    private void initBitmap() {
        bitmapNormal = BitmapFactory.decodeResource(getResources(), normalImageRes);
        bitmapPress = BitmapFactory.decodeResource(getResources(), pressImageRes);
        bitmapUnLock = BitmapFactory.decodeResource(getResources(), unlockImageSrc);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        BitmapUtil.drawImage(canvas, getPointBitmap(), currPoint.x - mRadius, currPoint.y - mRadius, mRadius * 2, mRadius * 2);
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
                if (event.getX() > pointRectF.left && event.getX() < pointRectF.right) {
                    if (event.getY() > pointRectF.top && event.getY() < pointRectF.bottom) {
                        isTouchDown = true;
                        setCurrStatus(PRESS);
                        updateDragCenter(event.getX(), event.getY());
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                double distance = GeometryUtil.getDistanceBetween2Points(centerPoint, new PointF(event.getX(), event.getY()));
                if (distance <= farestDistance) {
                    if (distance > lockDistance) {
                        setCurrStatus(UNLOCK);
                    } else {
                        if (getCurrStatus() != PRESS) {
                            setCurrStatus(PRESS);
                        }
                    }
                    updateDragCenter(event.getX(), event.getY());
                } else {
                    Toast.makeText(getContext(), "开锁状态", Toast.LENGTH_SHORT).show();
                }
                break;
            case MotionEvent.ACTION_UP:
                final PointF fixedPoint = new PointF(currPoint.x, currPoint.y);
                distance = GeometryUtil.getDistanceBetween2Points(centerPoint, currPoint);
                if (distance > lockDistance) {
                    setCurrStatus(UNLOCKED);
                    Toast.makeText(getContext(), "开锁", Toast.LENGTH_SHORT).show();
                    invalidate();
                } else {
                    setCurrStatus(NORMAL);
                    ValueAnimator backAnim = ValueAnimator.ofFloat(0, 1);
                    backAnim.setDuration(750);
                    backAnim.setInterpolator(new BounceInterpolator());
                    backAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float percent = (float) animation.getAnimatedValue();
                            PointF p = GeometryUtil.getPointByPercent(fixedPoint, centerPoint, percent);
                            Log.i(TAG, "onAnimationUpdate x:" + p.x + " y:" + p.y);
                            updateDragCenter(p.x, p.y);
                        }
                    });
                    backAnim.start();

                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
        }
        return super.onTouchEvent(event);
    }

    private void updateDragCenter(float x, float y) {
        currPoint.set(x, y);
        invalidate();
    }
}
