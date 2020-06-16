package com.taofe.dicomviewer;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class InnerTouchListener implements View.OnTouchListener{
    private View parent = null;
    private View view = null;
    private WindowManager.LayoutParams layoutParams = null;
    private WindowManager windowManager = null;
    private int posX;
    private int posY;
    private SystemGeometry sg;
    private Rect rt = new Rect();
    private RectF innerRect = new RectF();
    private OnInnerTouchListener onInnerTouchListener = null;

    public InnerTouchListener(Context context, View parent, View view, WindowManager.LayoutParams layoutParams, WindowManager windowManager){
        this.parent = parent;
        this.view = view;
        this.layoutParams = layoutParams;
        this.windowManager = windowManager;

        sg = new SystemGeometry(context);
    }

    public void setRelativePosition(int x, int y){
        if (parent!=null) {
            parent.getGlobalVisibleRect(rt);
            Log.d("rtParent", "left:"+ rt.left + "top:"+ rt.top + "right:"+ rt.right + "bottom:"+rt.bottom);

            int left = rt.left - view.getWidth()/2;
            int top = rt.top - sg.getStatusBarHeight() - view.getHeight() + view.getPaddingBottom()/(int)sg.getDensity();

            if (((FolderPhotoView)parent).getPhotoView()!=null) {
                RectF rtImage = ((FolderPhotoView) parent).getPhotoView().getDisplayRect();
                float innerLeft = left + rtImage.left;
                float innerTop = top +  rtImage.top;
                innerRect.set(innerLeft, innerTop, innerLeft + rtImage.width(), innerTop + rtImage.height());
                Log.d("rtInner", "left:"+ innerRect.left + "top:"+ innerRect.top + "right:"+ innerRect.right + "bottom:"+innerRect.bottom);
            }
        }
        if (layoutParams !=null){
            layoutParams.x = rt.left - view.getWidth()/2 + x;
            layoutParams.y = rt.top - sg.getStatusBarHeight() - view.getHeight() + view.getPaddingBottom()/(int)sg.getDensity() + y;
            if (onInnerTouchListener!=null){
                onInnerTouchListener.onTouch(layoutParams.x,layoutParams.y ,innerRect);
            }
            windowManager.updateViewLayout(view, layoutParams);
        }
    }

    public void setOnInnerTouchListener(OnInnerTouchListener innerTouchListener){
        this.onInnerTouchListener = innerTouchListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Rect rt = new Rect();
        if (parent!=null){
            parent.getGlobalVisibleRect(rt);
        }
        if (innerRect.contains(layoutParams.x, layoutParams.y)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    posX = (int) event.getRawX();
                    posY = (int) event.getRawY();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    int newPosX = (int) event.getRawX();
                    int newPosY = (int) event.getRawY();
                    int disX = newPosX - posX;
                    int disY = newPosY - posY;
                    posX = newPosX;
                    posY = newPosY;
                    layoutParams.x += disX;
                    layoutParams.y += disY;

                    if (layoutParams.x >= innerRect.right){
                        layoutParams.x = (int)(innerRect.right -1);
                    }else if (layoutParams.x <= innerRect.left){
                        layoutParams.x = (int)(innerRect.left +1);
                    }
                    if (layoutParams.y >= innerRect.bottom){
                        layoutParams.y = (int)(innerRect.bottom -1);
                    }else if (layoutParams.y <= innerRect.top){
                        layoutParams.y = (int)(innerRect.top +1);
                    }
                    if (onInnerTouchListener!=null) {
                        onInnerTouchListener.onTouch(layoutParams.x,layoutParams.y ,innerRect);
                    }
                    windowManager.updateViewLayout(v, layoutParams);
                    break;
                }
            }
        }
        return false;
    }
}
