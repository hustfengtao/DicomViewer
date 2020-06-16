package com.taofe.dicomviewer;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class SystemGeometry {
    private int screenWidth;
    private int screenHeight;
    private int width;
    private int height;
    private float density;
    private Context mContext;

    public SystemGeometry(Context context){
        mContext = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;         // 屏幕宽度（像素）
        height = dm.heightPixels;       // 屏幕高度（像素）
        density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
// 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        screenHeight = (int) (height / density);// 屏幕高度(dp)
    }

    public int getScreenWidth(){return screenWidth;}

    public int getScreenHeight(){return screenHeight;}

    public int getWidth(){return width;}

    public int getHeight(){return height;}

    public float getDensity() { return density; }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavgationHeight() {
        int result = 0;
        int resourceId=0;
        int rid = mContext.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid!=0){
            resourceId = mContext.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            result = mContext.getResources().getDimensionPixelSize(resourceId);
            return result;
        }else
            return 0;
    }
}
