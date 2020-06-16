package com.taofe.dicomviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.appcompat.widget.AppCompatImageView;
import com.taofe.dicomviewer.HistogramUtil.Pair;

public class HistogramImageView extends AppCompatImageView {
    public final static int TYPE_CLOSE = 0;
    public final static int TYPE_OPEN = 1;
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private int yRange = 0;
    private int xRange = 0;
    private short[] pixelData = null;
    private int type = TYPE_CLOSE;
    private  ArrayList<Pair> list = new ArrayList<Pair>();
    private OnHistogramListener onHistogramListener = null;


    public HistogramImageView(Context context) {
        this(context, null);
    }

    public HistogramImageView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public HistogramImageView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!list.isEmpty()) {
            if (type == TYPE_CLOSE) {
                mPaint.setColor(Color.GREEN);
                mPaint.setStrokeWidth(1);
                float yRate = (float) mHeight / (float) yRange;
                float xRate = (float) mWidth / (float) xRange;
                for (Pair item : list) {
                    canvas.drawLine((float) item.getValue() * xRate, (float) mHeight, (float) item.getValue() * xRate, (float) mHeight - (float) item.getCounts() * yRate, mPaint);
                }
            }else if(type == TYPE_OPEN){
                mPaint.setColor(Color.GREEN);
                mPaint.setStrokeWidth(1);
                float yRate = (float) mHeight / (float) yRange;
                float xRate = (float) mWidth / (float) xRange;
                for (int i=0;i!=list.size()-1;i++){
                    canvas.drawLine((float)list.get(i).getCounts()*xRate, (float)mHeight - (float)list.get(i).getValue()*yRate,(float)list.get(i+1).getCounts()*xRate, (float)mHeight - (float)list.get(i+1).getValue()*yRate, mPaint);
                }
            }
        } else {
            canvas.drawColor(Color.WHITE);
        }
        mPaint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, mWidth, mHeight, mPaint);
        super.onDraw(canvas);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    public void setOnHistogramListener(OnHistogramListener onHistogramListener){
        this.onHistogramListener = onHistogramListener;
    }

    public void setPixelData(short[] data, int type){
        if (data != null) {
            pixelData = data.clone();
            this.type = type;
            if (!list.isEmpty()) {
                list.clear();
            }
            if (type == TYPE_CLOSE) {
                HistogramUtil hist = new HistogramUtil(pixelData);
                list = hist.retriveHistData();
                xRange = hist.getMax() - hist.getMin();
                yRange = hist.getMaxCount();
                if (onHistogramListener != null) {
                    onHistogramListener.OnHistogramPrepare(hist.getMin(), hist.getMax());
                }
            }else if(type == TYPE_OPEN){
                for (int i=0;i!=pixelData.length;i++){
                    list.add(new Pair(pixelData[i], i));
                }
                short[] minmax = new short[2];
                Arrays.sort(data);
                minmax[0]= data[0];
                minmax[1]= data[data.length-1];
                yRange = minmax[1] - minmax[0];
                xRange = pixelData.length;
                if (onHistogramListener != null) {
                    onHistogramListener.OnHistogramPrepare(0, pixelData.length);
                }
            }
            invalidate();
            pixelData = null;
        }
    }

    public void resetHistView(){
        if (!list.isEmpty()) {
            list.clear();
        }
        invalidate();
    }
}
