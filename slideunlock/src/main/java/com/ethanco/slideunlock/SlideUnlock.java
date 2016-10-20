package com.ethanco.slideunlock;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;

import com.ethanco.slideunlock.utils.GeometryUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static com.ethanco.slideunlock.utils.BitmapUtil.drawImage;
import static com.ethanco.slideunlock.utils.GeometryUtil.getDistanceBetween2Points;
import static com.ethanco.slideunlock.utils.MeasureUtil.dp2px;
import static com.ethanco.slideunlock.utils.MeasureUtil.getViewHeight;
import static com.ethanco.slideunlock.utils.MeasureUtil.getViewWidth;

/**
 * @Description 滑动解锁
 * Created by EthanCo on 2016/10/18.
 */

public class SlideUnlock extends View {

    //View高
    private int mHeight;
    //View宽
    private int mWidth;
    //X轴中心点
    private float mCenterX;
    //Y轴中心点
    private float mCenterY;
    //keyhole半径
    private float keyholeRadius;
    //keyhole的范围
    private RectF keyholeRectF;
    //中心点
    private PointF centerPoint;
    //现在keyhole所在的中心点
    private PointF currPoint;
    //回弹动画
    private ValueAnimator springbackAnim;

    //最大距离
    private float farestDistance = 250;
    //解锁距离
    private float preLockDistance = 200;
    //现在的距离
    private float currDistance;

    //普通状态下Keyhole的Bitmap
    private Bitmap bitmapNormal;
    //按下状态下Keyhole的Bitmap
    private Bitmap bitmapPress;
    //可解锁状态Keyhole的Bitmap
    private Bitmap bitmapUnLock;


    //普通状态下Keyhole的图片资源ID
    private int normalKeyholeRes;
    //按下状态下Keyhole的图片资源ID
    private int pressKeyholeRes;
    //可解锁状态Keyhole的图片资源ID
    private int unlockKeyholeRes;

    //状态 枚举
    public static final int NORMAL = 0;   //普通
    public static final int PRESS = 1;    //按下
    public static final int UNLOCK = 2;   //可解锁
    public static final int UNLOCKED = 3; //已解锁

    @IntDef({NORMAL, PRESS, UNLOCK, UNLOCKED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LockStatus {
    }

    @LockStatus
    private int currStatus = NORMAL;  //现在的状态

    public SlideUnlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideUnlock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar(context, attrs);
    }

    private void initVar(Context context, AttributeSet attrs) {
        //初始化默认值
        initDefaultVar();
        //初始化自定义属性
        initAttrs(context, attrs);
        //初始化画笔
        initPaint();
        //初始化Bitmap
        initBitmap();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getViewHeight(getContext(), heightMeasureSpec, mHeight);
        int width = getViewWidth(getContext(), widthMeasureSpec, mWidth);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //初始化View尺寸变量
        initViewSizeVar();
        //初始化keyhole范围Rect
        initKeyholeRectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = currPoint.x - keyholeRadius;
        float top = currPoint.y - keyholeRadius;
        float expectWidth = keyholeRadius * 2;
        float expectHeight = keyholeRadius * 2;
        drawImage(canvas, getKeyholeBitmap(), left, top, expectWidth, expectHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//            case MotionEvent.ACTION_MOVE:
//            case MotionEvent.ACTION_UP:
//                updateKeyhole(x, y);
//                return true;
            case MotionEvent.ACTION_DOWN:

                //判断触碰点是否在keyhole范围之内
                if (isKeyholeScope(event)) {
                    cacelSpringbackAnim();
                    setCurrStatus(PRESS);
                    updateKeyhole(x, y);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                cacelSpringbackAnim();
                PointF tempPoint = new PointF(event.getX(), event.getY());
                currDistance = getDistanceBetween2Points(centerPoint, tempPoint);
                if (currDistance <= farestDistance) {
                    //检查状态并进行切换
                    switchStatus();
                    updateKeyhole(x, y);
                } else {
                    unlockCallback();
                }
                break;

            case MotionEvent.ACTION_UP:

                final PointF fixedPoint = new PointF(currPoint.x, currPoint.y);
                currDistance = getDistanceBetween2Points(centerPoint, currPoint);
                if (currDistance > preLockDistance) {
                    setCurrStatus(UNLOCKED);
                    invalidate();
                    unlockCallback();
                } else {
                    setCurrStatus(NORMAL);
                    //开始回退动画
                    startSpringbackAnim(fixedPoint);
                }
                break;

        }
        return super.onTouchEvent(event);
    }

    //============================= Z-具体的方法 ==============================/

    private void initViewSizeVar() {
        mHeight = getHeight();
        mWidth = getWidth();
        mCenterX = getWidth() / 2.0F;
        mCenterY = getHeight() / 2.0F;
        centerPoint = new PointF(mCenterX, mCenterY);
        currPoint = new PointF(mCenterX, mCenterY);
    }

    private void initKeyholeRectF() {
        float keyholeLeft = mCenterX - keyholeRadius;
        float keyholeTop = mCenterY - keyholeRadius;
        float keyholeRight = mCenterX + keyholeRadius;
        float keyholeBottom = mCenterY + keyholeRadius;
        keyholeRectF = new RectF(keyholeLeft, keyholeTop, keyholeRight, keyholeBottom);
    }

    public int getCurrStatus() {
        return currStatus;
    }

    //设置状态
    public void setCurrStatus(@LockStatus int currStatus) {
        this.currStatus = currStatus;
    }

    //根据现在的状态获取Keyhole显示的Bitmap
    private Bitmap getKeyholeBitmap() {
        if (currStatus == NORMAL) {
            return bitmapNormal;
        } else if (currStatus == PRESS) {
            return bitmapPress;
        } else {
            return bitmapUnLock;
        }
    }

    private void initDefaultVar() {
        mHeight = dp2px(getContext(), 500);
        mWidth = dp2px(getContext(), 500);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlideUnLock);
        normalKeyholeRes = ta.getResourceId(R.styleable.SlideUnLock_normalKeyholeSrc, 0);
        pressKeyholeRes = ta.getResourceId(R.styleable.SlideUnLock_pressKeyholeSrc, 0);
        unlockKeyholeRes = ta.getResourceId(R.styleable.SlideUnLock_unlockKeyholeSrc, 0);
        farestDistance = ta.getDimension(R.styleable.SlideUnLock_farestDistance, dp2px(getContext(), 250));
        preLockDistance = ta.getDimension(R.styleable.SlideUnLock_preLockDistance, dp2px(getContext(), 200));
        keyholeRadius = ta.getDimension(R.styleable.SlideUnLock_keyholeRadius, dp2px(getContext(), 30));
        ta.recycle();
    }

    private void initPaint() {
        //mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mBitPaint.setFilterBitmap(true); //对位图进行防锯齿
        //mBitPaint.setDither(true);
    }

    private void initBitmap() {
        bitmapNormal = BitmapFactory.decodeResource(getResources(), normalKeyholeRes);
        bitmapPress = BitmapFactory.decodeResource(getResources(), pressKeyholeRes);
        bitmapUnLock = BitmapFactory.decodeResource(getResources(), unlockKeyholeRes);
    }

    private void switchStatus() {
        if (currDistance > preLockDistance) {
            setCurrStatus(UNLOCK);
        } else {
            if (getCurrStatus() != PRESS) setCurrStatus(PRESS);
        }
    }

    private boolean isKeyholeScope(MotionEvent event) {
        return event.getX() > keyholeRectF.left && event.getX() < keyholeRectF.right
                && event.getY() > keyholeRectF.top && event.getY() < keyholeRectF.bottom;
    }

    private void startSpringbackAnim(final PointF fixedPoint) {
        springbackAnim = ValueAnimator.ofFloat(0, 1);
        springbackAnim.setDuration(750);
        springbackAnim.setInterpolator(new BounceInterpolator());
        springbackAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = (float) animation.getAnimatedValue();
                PointF p = GeometryUtil.getPointByPercent(fixedPoint, centerPoint, percent);
                updateKeyhole(p.x, p.y);
            }
        });
        springbackAnim.start();
    }

    //取消正在运行的动画
    private void cacelSpringbackAnim() {
        if (springbackAnim != null && springbackAnim.isRunning()) {
            springbackAnim.cancel();
        }
    }

    //更新keyhole位置
    private void updateKeyhole(float x, float y) {
        currPoint.set(x, y);
        invalidate();
    }

    //============================= Z-开放的接口 ==============================/

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