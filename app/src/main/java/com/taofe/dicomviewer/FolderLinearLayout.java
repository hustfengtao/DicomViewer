package com.taofe.dicomviewer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FolderLinearLayout extends LinearLayout {
    private ValueAnimator heightAnimation = null;
    private boolean isExpand = true;
    private int animationDuration = 0;
    private View view;
    private int maxHeight = 0;
    private int minHeight = 0;
    private OnAnimeUpdateListener onAnimeUpdateListener = null;
    private Paint mPaint;
    private boolean drawBorder = false;

    public FolderLinearLayout(Context context) {
        this(context, null);
    }

    public FolderLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolderLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs
                , R.styleable.FolderLinearLayout
                , defStyle, 0);
        isExpand = array.getBoolean(R.styleable.FolderLinearLayout_expend, true);
        animationDuration = array.getInt(R.styleable.FolderLinearLayout_animationduration, 500);

        initView();
    }

    private void initView() {
        view = this;
        getViewHeight();
        mPaint = new Paint();
    }

    private void getViewHeight(){
        view.post(new Runnable() {
            @Override
            public void run() {
                maxHeight = view.getMeasuredHeight();
                if (!isExpand){
                    setViewHeight(minHeight);
                }else{
                    setViewHeight(maxHeight);
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setAlpha(255);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0,  this.getWidth(), this.getHeight(), mPaint);
        if(drawBorder) {
            mPaint.setColor(Color.GREEN);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(5);
            canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), mPaint);
        }
        super.onDraw(canvas);
    }

    protected void drawBorder(boolean draw){
        this.drawBorder = draw;
        invalidate();
    }

    public void setMaxHeight(int maxHeight){
        this.maxHeight = maxHeight;
    }

    public void setMinHeight(int minHeight){
        this.minHeight = minHeight;
    }

    public void setViewHeight(int height){
        final ViewGroup.LayoutParams params = this.getLayoutParams();
        params.height = height;
        this.requestLayout();
    }

    private void animateToggle(int animationDuration) {
        heightAnimation = isExpand ?
                ValueAnimator.ofFloat(minHeight, maxHeight) : ValueAnimator.ofFloat(maxHeight, minHeight);
        heightAnimation.setDuration(animationDuration / 2);
        heightAnimation.setStartDelay(animationDuration / 2);

        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                setViewHeight((int) val);
                if (isExpand){
                    if (val == (float)maxHeight){
                        Log.d("folderview", "expand animation finish");
                        if (onAnimeUpdateListener!=null){
                            onAnimeUpdateListener.onFinish();
                        }
                    }
                }else {
                    if (val == (float)minHeight){
                        Log.d("folderview", "collapse animation finish");
                        if (onAnimeUpdateListener!=null){
                            onAnimeUpdateListener.onFinish();
                        }
                    }
                }
                if (onAnimeUpdateListener!=null){
                    onAnimeUpdateListener.onUpdate();
                }
            }
        });

        heightAnimation.start();
    }
    public void setOnAnimeUpdateListener(OnAnimeUpdateListener onAnimeUpdateListener){
        this.onAnimeUpdateListener = onAnimeUpdateListener;
    }

    public void setAnimationDuration(int duration){
        animationDuration = duration;
    }

    public void setExpand(boolean isExpand){
        this.isExpand = !isExpand;
        toggleExpand();
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void expand(){
        isExpand = true;
        animateToggle(animationDuration);
    }

    public void collapse(){
        isExpand = false;
        animateToggle(animationDuration);
    }

    public void toggleExpand() {
        if (isExpand) {
            collapse();
        } else {
            expand();
        }
    }
}
