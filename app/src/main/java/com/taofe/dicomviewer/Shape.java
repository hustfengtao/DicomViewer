package com.taofe.dicomviewer;

import android.graphics.PointF;

public class Shape {
    public final static int SHAPE_LINE = 0;
    public final static int SHAPE_RECTANGLE = 1;
    public final static int SHAPE_OVAL = 2;

    public final static int POINT_NONE = -1;
    public final static int POINT_TOPLEFT = 0;
    public final static int POINT_TOPRIGHT = 1;
    public final static int POINT_BOTTOMLEFT = 2;
    public final static int POINT_BOTTOMRIGHT = 3;

    private PointF ptStart;
    private PointF ptEnd;
    private int type;
    private boolean select = false;
    private int pointSelect = -1;

    public Shape(){
    }

    public Shape(PointF ptStart, PointF ptEnd, int type){
        this.ptStart = ptStart;
        this.ptEnd = ptEnd;
        this.type = type;
    }

    public void setPtStart(PointF pt){
        ptStart = pt;
    }
    public void setPtEnd(PointF pt){
        ptEnd = pt;
    }
    public void setSelected(boolean select){
        this.select = select;
    }

    public void setType(int type){
        this.type = type;
    }

    public void setPointSelect(int pointSelect){
        this.pointSelect = pointSelect;
    }

    public PointF getPtStart(){
        return ptStart;
    }

    public PointF getPtEnd(){
        return ptEnd;
    }

    public int getType(){
        return type;
    }

    public boolean isSelect() {
        return select;
    }

    public int getPointSelect(){
        return pointSelect;
    }
}
