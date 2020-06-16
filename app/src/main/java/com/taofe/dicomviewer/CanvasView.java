package com.taofe.dicomviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;

public class CanvasView extends View {
    private Paint mPaint;
    private boolean isDrawing = false;
    private PointF ptStart = new PointF();
    private PointF ptEnd = new PointF();
    private ArrayList<Shape> shapeArrayList = new ArrayList<Shape>();
    private OnDrawerChangeListener onDrawerChangeListener = null;
    private int pointWidth = 10;

    public CanvasView(Context context) {
        this(context, null);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
    }

    public void setOnDrawerChangeListener(OnDrawerChangeListener onDrawerChangeListener) {
        this.onDrawerChangeListener = onDrawerChangeListener;
    }

    public void addDrawer(int type){
        PointF ptS = new PointF();
        PointF ptE = new PointF();
        Shape shape = new Shape(ptS, ptE, type);
        shape.setSelected(true);

        for (Shape s:shapeArrayList){
            s.setSelected(false);
        }

        shapeArrayList.add(shape);
        isDrawing = true;
    }

    public void deleteDrawer(){
        if (getShape() == null){
            shapeArrayList.clear();
        }else {
            Iterator<Shape> iterator = shapeArrayList.iterator();
            while (iterator.hasNext()) {
                Shape shape = iterator.next();
                if (shape.isSelect()) {
                    iterator.remove();
                }
            }
        }
        invalidate();
        if (onDrawerChangeListener!=null){
            onDrawerChangeListener.onDrawerChange();
        }
    }

    public Shape getShape(){
        for (Shape shape:shapeArrayList){
            if (shape.isSelect()){
                return shape;
            }
        }
        return null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (oldw != 0 && oldh != 0) {
            for (Shape shape:shapeArrayList){
                shape.getPtStart().x = (shape.getPtStart().x*w/oldw);
                shape.getPtStart().y = (shape.getPtStart().y*h/oldh);
                shape.getPtEnd().x = (shape.getPtEnd().x*w/oldw);
                shape.getPtEnd().y = (shape.getPtEnd().y*h/oldh);
            }
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setAlpha(255);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), mPaint);

        for (Shape shape:shapeArrayList){
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.GREEN);
            mPaint.setStrokeWidth(1);
            switch (shape.getType()) {
                case Shape.SHAPE_LINE: {
                    canvas.drawLine(shape.getPtStart().x, shape.getPtStart().y, shape.getPtEnd().x, shape.getPtEnd().y, mPaint);
                    if (shape.isSelect()) {
                        mPaint.setStyle(Paint.Style.FILL);
                        if (shape.getPointSelect()!=Shape.POINT_NONE){
                            mPaint.setColor(Color.RED);
                        }
                        canvas.drawRect(shape.getPtStart().x - pointWidth, shape.getPtStart().y - pointWidth, shape.getPtStart().x + pointWidth, shape.getPtStart().y + pointWidth, mPaint);
                        canvas.drawRect(shape.getPtEnd().x - pointWidth, shape.getPtEnd().y - pointWidth, shape.getPtEnd().x + pointWidth, shape.getPtEnd().y + pointWidth, mPaint);
                    }
                    break;
                }
                case Shape.SHAPE_RECTANGLE: {
                    canvas.drawRect(shape.getPtStart().x, shape.getPtStart().y, shape.getPtEnd().x, shape.getPtEnd().y, mPaint);
                    if (shape.isSelect()) {
                        mPaint.setStyle(Paint.Style.FILL);
                        if (shape.getPointSelect()!= Shape.POINT_NONE){
                            mPaint.setColor(Color.RED);
                        }
                        canvas.drawRect(shape.getPtStart().x - pointWidth, shape.getPtStart().y - pointWidth, shape.getPtStart().x + pointWidth, shape.getPtStart().y + pointWidth, mPaint);
                        canvas.drawRect(shape.getPtEnd().x - pointWidth, shape.getPtStart().y - pointWidth, shape.getPtEnd().x + pointWidth, shape.getPtStart().y + pointWidth, mPaint);
                        canvas.drawRect(shape.getPtStart().x - pointWidth, shape.getPtEnd().y - pointWidth, shape.getPtStart().x + pointWidth, shape.getPtEnd().y + pointWidth, mPaint);
                        canvas.drawRect(shape.getPtEnd().x - pointWidth, shape.getPtEnd().y - pointWidth, shape.getPtEnd().x + pointWidth, shape.getPtEnd().y + pointWidth, mPaint);
                    }
                    break;
                }
                case Shape.SHAPE_OVAL: {
                    canvas.drawOval(shape.getPtStart().x, shape.getPtStart().y, shape.getPtEnd().x, shape.getPtEnd().y, mPaint);
                    if (shape.isSelect()) {
                        mPaint.setStyle(Paint.Style.FILL);
                        if (shape.getPointSelect()!= Shape.POINT_NONE){
                            mPaint.setColor(Color.RED);
                        }
                        canvas.drawRect(shape.getPtStart().x - pointWidth, shape.getPtStart().y - pointWidth, shape.getPtStart().x + pointWidth, shape.getPtStart().y + pointWidth, mPaint);
                        canvas.drawRect(shape.getPtEnd().x - pointWidth, shape.getPtStart().y - pointWidth, shape.getPtEnd().x + pointWidth, shape.getPtStart().y + pointWidth, mPaint);
                        canvas.drawRect(shape.getPtStart().x - pointWidth, shape.getPtEnd().y - pointWidth, shape.getPtStart().x + pointWidth, shape.getPtEnd().y + pointWidth, mPaint);
                        canvas.drawRect(shape.getPtEnd().x - pointWidth, shape.getPtEnd().y - pointWidth, shape.getPtEnd().x + pointWidth, shape.getPtEnd().y + pointWidth, mPaint);
                    }
                    break;
                }
            }
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                ptStart.x = (int)event.getX();
                ptStart.y = (int)event.getY();
                if (isDrawing) {
                    shapeArrayList.get(shapeArrayList.size() - 1).setPtStart(new PointF(event.getX(), event.getY()));
                }else{
                    boolean isSelectPoint = false;
                    for (Shape s:shapeArrayList){
                        if (s.isSelect()){
                            switch (s.getType()){
                                case Shape.SHAPE_LINE:{
                                    RectF rtS = new RectF(s.getPtStart().x -pointWidth, s.getPtStart().y -pointWidth, s.getPtStart().x+pointWidth, s.getPtStart().y+pointWidth);
                                    RectF rtE = new RectF(s.getPtEnd().x -pointWidth, s.getPtEnd().y -pointWidth, s.getPtEnd().x+pointWidth, s.getPtEnd().y+pointWidth);
                                    if (rtS.contains(event.getX(), event.getY())){
                                        isSelectPoint = true;
                                        s.setPointSelect(Shape.POINT_TOPLEFT);
                                    }
                                    if (rtE.contains(event.getX(), event.getY())){
                                        isSelectPoint = true;
                                        s.setPointSelect(Shape.POINT_BOTTOMRIGHT);
                                    }
                                    break;
                                }
                                case Shape.SHAPE_OVAL:
                                case Shape.SHAPE_RECTANGLE:{
                                    RectF rtLeftTop = new RectF(s.getPtStart().x -pointWidth, s.getPtStart().y -pointWidth, s.getPtStart().x+pointWidth, s.getPtStart().y+pointWidth);
                                    RectF rtRightTop = new RectF(s.getPtEnd().x -pointWidth, s.getPtStart().y -pointWidth, s.getPtEnd().x+pointWidth, s.getPtStart().y+pointWidth);
                                    RectF rtBottomLeft = new RectF(s.getPtStart().x -pointWidth, s.getPtEnd().y -pointWidth, s.getPtStart().x+pointWidth, s.getPtEnd().y+pointWidth);
                                    RectF rtBottomRight = new RectF(s.getPtEnd().x -pointWidth, s.getPtEnd().y -pointWidth, s.getPtEnd().x+pointWidth, s.getPtEnd().y+pointWidth);
                                    if (rtLeftTop.contains(event.getX(), event.getY())){
                                        isSelectPoint = true;
                                        s.setPointSelect(Shape.POINT_TOPLEFT);
                                    }
                                    if (rtRightTop.contains(event.getX(), event.getY())){
                                        isSelectPoint = true;
                                        s.setPointSelect(Shape.POINT_TOPRIGHT);
                                    }
                                    if (rtBottomLeft.contains(event.getX(), event.getY())){
                                        isSelectPoint = true;
                                        s.setPointSelect(Shape.POINT_BOTTOMLEFT);
                                    }
                                    if (rtBottomRight.contains(event.getX(), event.getY())){
                                        isSelectPoint = true;
                                        s.setPointSelect(Shape.POINT_BOTTOMRIGHT);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if (!isSelectPoint) {
                        for (Shape shape : shapeArrayList) {
                            shape.setSelected(false);
                            switch (shape.getType()) {
                                case Shape.SHAPE_LINE: {
                                    float a = (ptStart.y - shape.getPtStart().y) / (shape.getPtEnd().y - shape.getPtStart().y);
                                    float b = (ptStart.x - shape.getPtStart().x) / (shape.getPtEnd().x - shape.getPtStart().x);
                                    boolean ptInRange = false;
                                    if (ptStart.x >= shape.getPtStart().x && ptStart.x <= shape.getPtEnd().x){
                                        ptInRange = true;
                                    }
                                    if (ptStart.x >= shape.getPtEnd().x && ptStart.x <= shape.getPtStart().x){
                                        ptInRange = true;
                                    }
                                    if (Math.abs(b - a) <= 0.15 && ptInRange) {
                                        shape.setSelected(true);
                                        shape.setPointSelect(Shape.POINT_NONE);
                                    }
                                    break;
                                }
                                case Shape.SHAPE_OVAL: {
                                    float a = (shape.getPtEnd().x - shape.getPtStart().x) * (shape.getPtEnd().x - shape.getPtStart().x) / 4;
                                    float b = (shape.getPtEnd().y - shape.getPtStart().y) * (shape.getPtEnd().y - shape.getPtStart().y) / 4;
                                    float x = ptStart.x - (shape.getPtStart().x + shape.getPtEnd().x) / 2;
                                    float y = ptStart.y - (shape.getPtStart().y + shape.getPtEnd().y) / 2;
                                    float re = (x * x) / a + (y * y) / b;
                                    if (re <= 1) {
                                        shape.setSelected(true);
                                        shape.setPointSelect(Shape.POINT_NONE);
                                    }
                                    break;
                                }
                                case Shape.SHAPE_RECTANGLE: {
                                    RectF rt = new RectF(shape.getPtStart().x, shape.getPtStart().y, shape.getPtEnd().x, shape.getPtEnd().y);
                                    if (rt.contains(ptStart.x, ptStart.y)) {
                                        shape.setSelected(true);
                                        shape.setPointSelect(Shape.POINT_NONE);
                                    }
                                    break;
                                }
                            }
                        }
                        if (onDrawerChangeListener!=null){
                            onDrawerChangeListener.onDrawerChange();
                        }
                    }
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                ptEnd.x = (int)event.getX();
                ptEnd.y = (int)event.getY();
                if(isDrawing) {
                    shapeArrayList.get(shapeArrayList.size() - 1).setPtEnd(new PointF(event.getX(), event.getY()));
                }else{
                    for (Shape shape:shapeArrayList) {
                        if (shape.isSelect()) {
                            float x = ptEnd.x - ptStart.x;
                            float y = ptEnd.y - ptStart.y;
                            if (shape.getPointSelect()!= Shape.POINT_NONE) {
                                switch (shape.getPointSelect()){
                                    case Shape.POINT_TOPLEFT:{
                                        shape.setPtStart(new PointF(shape.getPtStart().x + x, shape.getPtStart().y + y));
                                        break;
                                    }
                                    case Shape.POINT_TOPRIGHT:{
                                        shape.setPtStart(new PointF(shape.getPtStart().x, shape.getPtStart().y + y));
                                        shape.setPtEnd(new PointF(shape.getPtEnd().x + x, shape.getPtEnd().y));
                                        break;
                                    }
                                    case Shape.POINT_BOTTOMLEFT:{
                                        shape.setPtStart(new PointF(shape.getPtStart().x + x, shape.getPtStart().y));
                                        shape.setPtEnd(new PointF(shape.getPtEnd().x, shape.getPtEnd().y + y));
                                        break;
                                    }
                                    case Shape.POINT_BOTTOMRIGHT:{
                                        shape.setPtEnd(new PointF(shape.getPtEnd().x + x, shape.getPtEnd().y + y));
                                        break;
                                    }
                                }
                            }else {
                                PointF ptS = new PointF(shape.getPtStart().x + x, shape.getPtStart().y + y);
                                PointF ptE = new PointF(shape.getPtEnd().x + x, shape.getPtEnd().y + y);
                                if (!isOutofRange(ptS, ptE)) {
                                    shape.setPtStart(ptS);
                                    shape.setPtEnd(ptE);
                                }
                            }
                        }
                    }
                    ptStart.x = (int)event.getX();
                    ptStart.y = (int)event.getY();
                }
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP:{
                if (isDrawing) {
                    isDrawing = false;
                }
                if (onDrawerChangeListener!=null){
                    onDrawerChangeListener.onDrawerChange();
                }
                break;
            }
        }
        return true;
    }

    private boolean isOutofRange(PointF ptStart, PointF ptEnd){
        Rect rtView = new Rect();
        getLocalVisibleRect(rtView);
        if (rtView.contains((int)ptStart.x, (int)ptStart.y)&&
                rtView.contains((int)ptEnd.x, (int)ptEnd.y)){
            return false;
        }
        return true;
    }
}
