package com.taofe.dicomviewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import java.util.ArrayList;
import static android.content.Context.WINDOW_SERVICE;

public class FloatWindowManager {
    final static int ALIGN_TOP = 1;
    final static int ALIGN_LEFT = 2;
    final static int ALIGN_RIGHT = 3;
    final static int ALIGN_BOTTOM = 4;
    private Context context;
    private WindowManager windowManager;
    private SystemGeometry sg;
    private ArrayList<WindowLayoutParam> list = new ArrayList<WindowLayoutParam>();

    private class WindowLayoutParam{
        private View v;
        private WindowManager.LayoutParams layoutParams;
        public WindowLayoutParam( View v, WindowManager.LayoutParams layoutParams){
            this.v = v;
            this.layoutParams = layoutParams;
        }
        public View getView() {
            return v;
        }
        public WindowManager.LayoutParams getLayoutParams() {
            return layoutParams;
        }
    }

    public FloatWindowManager(Context context){
        this.context = context;
        //system parameters
        sg = new SystemGeometry(context);
        windowManager = (WindowManager)context.getSystemService(WINDOW_SERVICE);
        //check permission
        checkSettingsPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
    }

    public WindowManager getWindowManager(){
        return windowManager;
    }

    public WindowManager.LayoutParams getLayoutParam(View v){
        for (WindowLayoutParam param :list){
            if (param.getView() == v){
                return param.layoutParams;
            }
        }
        return null;
    }

    public void addView(View v, int x, int y, int width, int height){
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT|Gravity.TOP;
        layoutParams.x = x;
        layoutParams.y = y;
        layoutParams.width = (int)(width*sg.getDensity());
        layoutParams.height = (int)(height*sg.getDensity());
        windowManager.addView(v, layoutParams);
        list.add(new WindowLayoutParam(v, layoutParams));
    }

    public void setParentAlign(View parent, View v, int align){
        Rect rt = new Rect();
        if (parent!=null) {
            parent.getGlobalVisibleRect(rt);
        }
        WindowManager.LayoutParams layoutParams = null;
        for (WindowLayoutParam param:list){
            if (param.getView() == v){
                layoutParams = param.getLayoutParams();
                break;
            }
        }
        if (layoutParams!=null) {
            switch (align){
                case ALIGN_TOP:{
                    layoutParams.x = rt.left + v.getPaddingLeft();
                    layoutParams.y = rt.top + v.getPaddingTop() - sg.getStatusBarHeight() + layoutParams.height;
                    break;
                }
                case ALIGN_LEFT:{
                    layoutParams.x = rt.left + v.getPaddingLeft() + layoutParams.width;
                    layoutParams.y = rt.top + v.getPaddingTop() - sg.getStatusBarHeight();
                    break;
                }
                case ALIGN_RIGHT:{
                    layoutParams.x = rt.right + v.getPaddingRight();
                    layoutParams.y = rt.top + v.getPaddingTop() - sg.getStatusBarHeight();
                    break;
                }
                case ALIGN_BOTTOM:{
                    layoutParams.x = rt.left + v.getPaddingLeft();
                    layoutParams.y = rt.bottom + v.getPaddingBottom() - sg.getStatusBarHeight();
                    break;
                }
            }
            windowManager.updateViewLayout(v, layoutParams);
        }
    }

    public void removeView(View v, boolean immediate){
        if (immediate){
            windowManager.removeViewImmediate(v);
        }else {
            windowManager.removeView(v);
        }
    }

    public void removeAllViews(boolean immediate){
        for (WindowLayoutParam param:list){
            if (param.getView() != null){
                removeView(param.getView(), immediate);
                break;
            }
        }
    }

    public void  hideAllFloatView(){
        for (WindowLayoutParam param:list) {
            ((DicomViewerActivity)context).notifyHideBorder(param.getView());
            param.getView().setVisibility(View.GONE);
        }
    }

    public void  toggleView(View v) {
        checkSettingsPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        if (v.getVisibility() == View.GONE){
            v.setVisibility(View.VISIBLE);
        }else{
            v.setVisibility(View.GONE);
        }
    }

    private void checkSettingsPermission(String perssion){
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(context)) {
                ((DicomViewerActivity)context).startActivityForResult(new Intent(perssion, Uri.parse("package:" + context.getPackageName())), 0);
            }
        }
    }
}
