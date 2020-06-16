package com.taofe.dicomviewer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import uk.co.senab.photoview.PhotoView;
import static java.lang.StrictMath.abs;

/**
 * Created by wing on 2016/12/22.
 */

public class DragPhotoView extends PhotoView {
    private Paint mPaint;
    // downX
    private float mDownX;
    // down Y
    private float mDownY;

    private float mTranslateY;
    private float mTranslateX;
    private float mScale = 1;
    private int mWidth;
    private int mHeight;
    private float mMinScale = 1.0f;
    private int mAlpha = 255;
    private final static int MAX_TRANSLATE_Y = 300;
    private final static int MAX_DOUBLE_CLICK_INTERVAL = 300;
    private final static long DURATION = 500;

    //is event on PhotoView
    private boolean isTapEvent = false;
    private boolean isSlideEvent = false;
    private boolean isDragEvent = false;
    private OnTapListener mTapListener;
    private OnExitListener mExitListener;
    private OnAnimatorListener mAnimatorListener;
    private AnimatorSet currentSet;

    private boolean enableExit = true;
    private boolean enableTap = true;
    private boolean isDragable = true;
    private boolean isZoomable = true;
    private boolean isAnimate = true;

    public DragPhotoView(Context context) {
        this(context, null);
    }

    public DragPhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public DragPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        TypedArray array = context.getTheme().obtainStyledAttributes(attr
                , R.styleable.DragPhotoView
                , defStyle, 0);
        enableExit = array.getBoolean(R.styleable.DragPhotoView_enableexit, true);
        enableTap = array.getBoolean(R.styleable.DragPhotoView_enabletap, true);
        isDragable = array.getBoolean(R.styleable.DragPhotoView_enabledrag, true);
        isZoomable = array.getBoolean(R.styleable.DragPhotoView_enablezoom, true);
        setZoomable(isZoomable);

        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setAlpha(mAlpha);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, mWidth, mHeight, mPaint);
        canvas.translate(mTranslateX, mTranslateY);
        canvas.scale(mScale, mScale, mWidth / 2, mHeight / 2);
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //only scale == 1 can drag
        if (getScale() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(isDragable) {
                        onActionDown(event);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(isDragable) {
                        onActionMove(event);
                    }
                    //slider
                    if (mTranslateY == 0 && mTranslateX != 0){
                        //如果不消费事件，则不作操作
                        isSlideEvent = true;
                        return super.dispatchTouchEvent(event);
                    }
                    //single finger drag  down
                    //2px currency
                    if(isDragable) {
                        if (abs(mTranslateY) > 0 && event.getPointerCount() == 1) {
                            //如果有上下位移 则不交给viewpager
                            isDragEvent = true;
                            //保证上划到到顶还可以继续滑动

                            float percent = abs(mTranslateY) / MAX_TRANSLATE_Y;
                            if (mScale >= mMinScale && mScale <= 1f) {
                                mScale = 1 - percent;

                                mAlpha = (int) (255 * (1 - percent));
                                if (mAlpha > 255) {
                                    mAlpha = 255;
                                } else if (mAlpha < 0) {
                                    mAlpha = 0;
                                }
                            }
                            if (mScale < mMinScale) {
                                mScale = mMinScale;
                            } else if (mScale > 1f) {
                                mScale = 1;
                            }
                            invalidate();

                            return true;
                        }
                        //防止下拉的时候双手缩放
                        if (abs(mTranslateY) >= 0 && mScale < 0.95) {
                            return true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (event.getPointerCount() == 1) {
                        if (isDragable) {
                            //tap
                            if (mTranslateX == 0 && mTranslateY == 0) {
                                isTapEvent = true;
                            }
                            onActionUp(event);
                        }
                        isTapEvent = false;
                        isSlideEvent = false;
                        isDragEvent = false;
                    }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void onActionUp(MotionEvent event) {
        //drag
        if (isDragEvent){
            if (abs(mTranslateY) > MAX_TRANSLATE_Y) {
                if (enableExit) {
                    setPhotoViewExit();
                }else{
                    prepareAnimation( 1,0, 0, 255);
                    startAnimation();
                }
            }else{
                prepareAnimation( 1,0, 0, 255);
                startAnimation();
            }
        } else if(isTapEvent){
            if (enableTap) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mTapListener != null) {
                            mTapListener.onTap(DragPhotoView.this);
                        }
                    }
                }, MAX_DOUBLE_CLICK_INTERVAL);
            }
        }else if(isSlideEvent){
        }
    }

    private void onActionDown(MotionEvent event) {
        mDownX = event.getX();
        mDownY = event.getY();
    }

    private void onActionMove(MotionEvent event) {
        float moveY = event.getY();
        float moveX = event.getX();
        mTranslateX = moveX - mDownX;
        mTranslateY = moveY - mDownY;
    }

    public void prepareAnimation(float scale, float transX, float transY, int alpha) {
        if (currentSet !=null){
            currentSet.cancel();
        }
        currentSet = new AnimatorSet();
        currentSet.play(getAlphaAnimation(alpha))
                .with(getScaleAnimation(scale))
                .with(getTranslateXAnimation(transX))
                .with(getTranslateYAnimation(transY));
        currentSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                currentSet = null;
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                currentSet = null;
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        if (mAnimatorListener!=null){
            mAnimatorListener.onAnimator(currentSet);
        }
    }

    public void startAnimation(){
        currentSet.start();
    }


    private ValueAnimator getAlphaAnimation(int alpha) {
        final ValueAnimator animator = ValueAnimator.ofInt(mAlpha, alpha);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAlpha = (int) valueAnimator.getAnimatedValue();
            }
        });

        return animator;
    }

    private ValueAnimator getTranslateYAnimation(float transY) {
        final ValueAnimator animator = ValueAnimator.ofFloat(mTranslateY, transY);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mTranslateY = (float) valueAnimator.getAnimatedValue();
            }
        });
        return animator;
    }

    private ValueAnimator getTranslateXAnimation(float transX) {
        final ValueAnimator animator = ValueAnimator.ofFloat(mTranslateX, transX);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mTranslateX = (float) valueAnimator.getAnimatedValue();
            }
        });
        return animator;
    }

    private ValueAnimator getScaleAnimation(float scale) {
        final ValueAnimator animatorY = ValueAnimator.ofFloat(mScale, scale);
        animatorY.setDuration(DURATION);
        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mScale = (float) valueAnimator.getAnimatedValue();
            }
        });

        animatorY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimate = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimate = false;
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return animatorY;
    }
    public float getMinScale() {
        return mMinScale;
    }

    public void setMinScale(float minScale) {
        mMinScale = minScale;
    }

    public void setDragable(boolean isDragable){
        this.isDragable = isDragable;
    }

    public void setZoomable(boolean isZoomable){
        super.setZoomable(isZoomable);
    }

    public void setOnTapListener(OnTapListener listener) {
        mTapListener = listener;
    }

    public void setOnExitListener(OnExitListener listener) {
        mExitListener = listener;
    }

    public void setOnAnimatorListener(OnAnimatorListener listener){mAnimatorListener = listener;}

    public interface OnAnimatorListener{
        void onAnimator(AnimatorSet anime);
    }

    public interface OnTapListener {
        void onTap(DragPhotoView view);
    }

    public interface OnExitListener {
        void onExit(DragPhotoView view, float translateX, float translateY, float w, float h);
    }

    public void setPhotoViewExit(){
        if (mExitListener != null) {
            mExitListener.onExit(this, mTranslateX, mTranslateY, mWidth, mHeight);
        }
    }

    public void setPhotoView(float transX, float transY, float scale, int alpha){
        mTranslateX = transX;
        mTranslateY = transY;
        mScale = scale;
        mAlpha = alpha;
        invalidate();
    }

    public void enableExitListener(boolean enable){
        enableExit = enable;
    }

    public void enableTapListener(boolean enable){
        enableTap = enable;
    }

}
