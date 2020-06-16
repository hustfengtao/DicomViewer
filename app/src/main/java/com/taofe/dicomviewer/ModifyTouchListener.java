package com.taofe.dicomviewer;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ModifyTouchListener implements View.OnTouchListener {
    private WindowManager.LayoutParams layoutParams = null;
    private WindowManager windowManager = null;
    private int posX;
    private int posY;
    public ModifyTouchListener(WindowManager.LayoutParams layoutParams, WindowManager windowManager){
        this.layoutParams = layoutParams;
        this.windowManager = windowManager;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
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

                windowManager.updateViewLayout(v, layoutParams);
                break;
            }
        }
        return false;
    }
}